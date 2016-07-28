/**
 * 
 */
package com.simple.dao.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simple.base.parser.BeanParser;
import com.simple.dao.DaoParamType;
import com.simple.dao.annotation.Param;
import com.simple.dao.annotation.SQL;
import com.simple.dao.bean.ArgParam;
import com.simple.dao.core.DaoModel;
import com.simple.dao.core.DaoProxy;
import com.simple.dao.core.OperationType;
import com.simple.dao.core.SqlOperation;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class SQLMethodParser {

    private static final Set<Class<?>> SIMPLE_CLASS_TYPE = new HashSet<Class<?>>();

    static {
        SIMPLE_CLASS_TYPE.add(int.class);
        SIMPLE_CLASS_TYPE.add(Integer.class);
        SIMPLE_CLASS_TYPE.add(long.class);
        SIMPLE_CLASS_TYPE.add(Long.class);
        SIMPLE_CLASS_TYPE.add(float.class);
        SIMPLE_CLASS_TYPE.add(Float.class);
        SIMPLE_CLASS_TYPE.add(double.class);
        SIMPLE_CLASS_TYPE.add(Double.class);
        SIMPLE_CLASS_TYPE.add(boolean.class);
        SIMPLE_CLASS_TYPE.add(Boolean.class);
        SIMPLE_CLASS_TYPE.add(String.class);
    }
	
    /**
     * 解析dao接口类中的所有方法。
     * @param daoClass 指定的dao接口类
     * @return 返回解析好了的方法映射表
     */
    public static Map<Method, SqlOperation> parseMethod(Class<?> daoClass, DaoProxy proxy) {
        Map<Method, SqlOperation> ret = new HashMap<Method, SqlOperation>();

        synchronized (daoClass) {
            //取得方法元数据注释、并解析
            for (Method method : daoClass.getMethods()) {
                ret.put(method, parseSqlOperation(method, proxy));
            }
        }

        return ret;
    }
    

    /**
     * 通过解析方法构建一个新的sql操作实例。
     * @param method 指定被解析的方法
     * @return 返回解析后的sql操作类型
     */
    private static SqlOperation parseSqlOperation(Method method, DaoProxy proxy) {
        SQL annoDao = method.getAnnotation(SQL.class);
        if (null == annoDao) {
            return null;
        }

        OperationType type = SimpleSqlParser.getOperationType(annoDao.value());

        if (null != type) {
            return type.getOperation(method, proxy);
        }

        return null;
    }
    
    
    /**
     * 解析方法的参数注解，统计各参数的名称以及类型信息。
     * @param method
     * @return
     */
    public static Map<String, ArgParam> getArgParams(Method method) {
        Map<String, ArgParam> argMap = new HashMap<String, ArgParam>();
        Class<?>[] paramClasses = method.getParameterTypes();
        Type[] paramTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        //只循环查找被标注的参数，如果没有注解的参数会被跳过
        for (int argIndex = 0; argIndex < paramAnnotations.length; argIndex ++) {
            Annotation[] annos = paramAnnotations[argIndex];
            for (Annotation anno : annos) {
                //非AnnoDaoParam注解的一律忽略
                if (!(anno instanceof Param)) continue;

                Param param = (Param) anno;

                //先判断参数的class类型
                Class<?> classType = paramClasses[argIndex];
                if (classType.isArray()) {
                    //数组类型
                    parseArrayBean(classType, param, argMap, argIndex);
                } else if (List.class.isAssignableFrom(classType)) {
                    //集合类型
                    parseListParam(classType, paramTypes[argIndex], argMap, param, argIndex);
                } else if (DaoModel.class.isAssignableFrom(classType)) {
                    //bean对象类型
                    parseBeanParam(classType, argMap, argIndex, param.type());
                } else if (SIMPLE_CLASS_TYPE.contains(classType) || Date.class.isAssignableFrom(classType)) {
                    //简单类型
                    argMap.put(param.code(), new ArgParam(param.code(), argIndex, classType, param.type()));
                }
            }
        }
        return argMap;
    }
    
    
    /**
     * 解析数组类型的方法参数。
     * @param classType 参数的实际类型
     * @param param 注解参数的实例
     * @param argMap 参数映射表
     * @param argIndex 参数序号
     */
    private static void parseArrayBean(Class<?> classType, Param param,
            Map<String, ArgParam> argMap, int argIndex) {
        //数组类型参数需要取其元素类型
        Class<?> realType = classType.getComponentType();
        String paramName = param.code();
        DaoParamType paramType = param.type();

        //如果集合元素为bean对象，则解析bean对象并将所有属性添加到map中
        BeanParser beanParser = null;
        if (DaoModel.class.isAssignableFrom(realType)) {
            beanParser = BeanParser.getBeanParser(realType);
        }

        if (null == beanParser) {
            argMap.put(paramName, new ArgParam(paramName, argIndex, classType, paramType));
            if (param.alias() != null && param.alias() != "") {
                argMap.put(param.alias(), new ArgParam(paramName, argIndex, classType, paramType));
            }

        } else {
            for (String fieldName : beanParser.fieldNameSet()) {
                argMap.put(fieldName, new ArgParam(fieldName, argIndex, classType, paramType, beanParser));
            }
        }
    }
    
    
    /**
     * 解析集合类型的方法参数。
     * @param cls 方法参数的实际类型
     * @param genericType 集合类型的声明TYPE（用于获取集合的元素类型）
     * @param argMap 参数映射表
     * @param param 注解参数实例
     * @param argIndex 参数序号
     */
    private static void parseListParam(Class<?> cls, Type genericType, Map<String, ArgParam> argMap,
            Param param, int argIndex) {
        //集合类型参数需要取其泛型的实际类型
        Class<?> realType = getParameterizedType(genericType);
        String paramName = param.code();
        DaoParamType paramType = param.type();

        //如果集合元素为bean对象，则解析bean对象并将所有属性添加到map中
        BeanParser beanParser = null;
        if (DaoModel.class.isAssignableFrom(realType)) {
            beanParser = BeanParser.getBeanParser(realType);
        }

        if (null == beanParser) {
            //如果集合的元素不是bean对象，则直接添加一个sql参数
            argMap.put(paramName, new ArgParam(paramName, argIndex, cls, paramType));
            //如果是集合类型，而且元素不是bean，则可能是in参数，应该制定一个别名
            if (param.alias() != null && param.alias().trim().length() > 0) {
                argMap.put(param.alias(), new ArgParam(paramName, argIndex, cls, paramType));
            }
        } else {
            //如果集合的元素是bean对象，则应该将bean的所有属性添加到参数中
            for (String fieldName : beanParser.fieldNameSet()) {
                argMap.put(fieldName, new ArgParam(fieldName, argIndex, cls, paramType, beanParser));
            }
        }
    }
    
    
    /**
     * 从声明类型中解析出泛型定义的实际集合元素类型。
     * 例如：从List<String>的定义中解析出String类型。
     * @param type 方法参数或者返回值的声明类型
     * @return 返回此集合类型的泛型元素类型
     */
    private static Class<?> getParameterizedType(Type type) {
        Class<?> ret = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArg : parameterizedType.getActualTypeArguments()) {
                ret = (Class<?>)typeArg;
            }
        }
        return ret;
    }
    
    
    /**
     * 解析bean类型的方法参数。
     * @param cls 参数的实际类型
     * @param argMap 参数映射表
     * @param argIndex 参数的序号
     * @param paramType 注解的参数类型
     */
    private static void parseBeanParam(Class<?> cls, Map<String, ArgParam> argMap,
            int argIndex, DaoParamType paramType) {
        //bean对象需要解析其所有属性
        BeanParser beanParser = BeanParser.getBeanParser(cls);

        for (String fieldName : beanParser.fieldNameSet()) {
            argMap.put(fieldName, new ArgParam(fieldName, argIndex, cls, paramType, beanParser));
        }
    }
	
}
