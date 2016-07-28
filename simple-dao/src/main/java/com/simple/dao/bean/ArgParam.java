package com.simple.dao.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.simple.base.parser.BeanParser;
import com.simple.base.parser.BeanParser.FieldStruct;
import com.simple.dao.DaoParamType;


/**
 * 方法参数的相关信息bean。指定了当前参数的序号（用于查找到实际参数值），参数的类型以及提供给SQL语句用的参数名称。
 *
 
 * @version 1.0 2009-10-27 16:05:33
 * @since 1.0
 */
public class ArgParam {

    //SQL中使用到的参数名称
    protected String name;
    //方法参数的序号
    protected int argIndex;
    //实际方法定义中的参数的class类型
    protected Class<?> classType;
    //注解定义的参数的类型
    protected DaoParamType paramType;
    //当前参数是否是集合类型（数组或者List）
    protected boolean isCollection;
    //如果是集合类型，当前标识是否是数组
    protected boolean isArray;
    //如果是bean类型，则应该指定bean的解析
    protected BeanParser beanParser;
    //如果beanParser不为NULL时，应该指定当前属性在bean的定义中的属性结构，用于从bean中获取值
    protected FieldStruct fieldStruct;

    /**
     * @see ArgParam#ArgParam(String, int, Class, DaoParamType, BeanParser)
     * @param name 参数的引用名
     * @param index 参数的序号
     * @param classType 参数的实际class类型
     * @param paramType 参数的注解类型
     */
    public ArgParam(String name, int argIndex, Class<?> type, DaoParamType paramType) {
        this(name, argIndex, type, paramType, null);
    }

    /**
     * 构建一个新的参数bean对象。
     *
     * @param name 参数的引用名
     * @param index 参数的序号
     * @param classType 参数的实际class类型
     * @param paramType 参数的注解类型
     * @param parser 如果为bean的属性参数，则需要指定bean的解析类
     */
    public ArgParam(String name, int index, Class<?> classType, DaoParamType paramType, BeanParser parser) {
        this.name = name;
        this.argIndex = index;
        this.classType = classType;
        this.isArray = classType.isArray();
        this.isCollection = isArray || List.class.isAssignableFrom(classType);
        this.paramType = paramType;

        if (null != parser) {
            this.beanParser = parser;
            this.fieldStruct = parser.getFieldStruct(name);
        }
    }

    public String getName() {
        return name;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public Class<?> getType() {
        return classType;
    }

    public DaoParamType getParamType() {
        return paramType;
    }

    public boolean isCollection() {
        return isCollection;
    }

    /**
     * 如果当前参数对应的方法的实际参数是数组或者List，则返回包含了所有实际值的List对象，否则返回NULL。
     * @param value 从实际的方法参数中获得的参数值，一般使用 args[getArgIndex()]获得（args为方法调用时的参数数组）
     * @return 如果该参数确实是集合（数组或者List）类型，则返回List值；否则返回<code>NULL</code>
     */
    @SuppressWarnings("unchecked")
    public List<Object> getListValue(Object value) {
        //如果不是集合类型，则返回NULL
        if (!isCollection) {
            return null;
        }

        List<Object> paramList = new ArrayList<Object>();

        if (value.getClass().isArray()) {
            //解析数组元素
            for (int i = 0, length = Array.getLength(value); i < length; i++) {
                paramList.add(Array.get(value, i));
            }
        } else {
            //强转List类型
            paramList = (List<Object>) value;
        }

        return paramList;
    }

    /**
     * 获得当前参数的实际值。
     * 此方法只返回非集合类型参数的数值，先调用{@link ArgParam#isCollection()}方法确定是否是集合类型，
     * 如果是集合类型请调用{@link ArgParam#getListValue(Object)}。
     * @param arg 实际方法调用时传入的参数值
     * @return 如果此参数是集合类型则返回NULL；否则返回实际值（如果是bean的属性值，则会直接返回该属性值，而非bean对象）
     */
    @SuppressWarnings("unchecked")
    public Object getValue(Object arg) {
    	
        if (arg == null || (arg.getClass().isArray() && !(arg instanceof byte[])) || arg instanceof List) {
            return null;
        }

        if (fieldStruct != null) {
            return fieldStruct.get(arg);
        } else {
            return arg;
        }
    }

    public boolean isBeanProperty() {
        return fieldStruct != null;
    }

}
