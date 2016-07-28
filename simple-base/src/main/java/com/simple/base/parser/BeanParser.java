/**
 * 
 */
package com.simple.base.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年5月25日
 */
public class BeanParser {
	
	
	private static final Map<Class<?>, BeanParser> cache = new HashMap<Class<?>, BeanParser>();
	
	private Map<String, FieldStruct> bstMap;
	private Map<String, Method> methodMap;
	
	private BeanParser(Map<String, FieldStruct> bstMap, Map<String, Method> methodMap) {
	        this.bstMap = bstMap;
	        this.methodMap = methodMap;
	}
	    
	 
	public static BeanParser getBeanParser(Class<?> cls) {
		if (cls == null)
			return null;
		return (cache.containsKey(cls)) ? cache.get(cls) : create(cls);
	}
	
	
	
	

	private static BeanParser create(Class<?> cls) {
		synchronized (cls) {
			if (cache.containsKey(cls))
				return cache.get(cls);
			BeanParser parser = BeanParser.parseBean(cls);
			cache.put(cls, parser);
			return parser;
		}
	}
	

    /**
     * 取得所有Bean属性名称的Set
     * @return Bean属性名称的Set
     */
    public Set<String> fieldNameSet() {
    	return bstMap.keySet();
    }
	
	/**
	 * 获取FieldStruct
	 * @param fieldName
	 * @return
	 */
    public  FieldStruct getFieldStruct(String fieldName) {
        if (!bstMap.containsKey(fieldName)) return null;
        return bstMap.get(fieldName);
    }
    
    public Object doMethod(Object instance, String methodName, Object... args ){
    	Method method = this.methodMap.get(methodName);
    	if(method == null){
    		throw new RuntimeException("方法不存在[" + methodName + "]");
    	}
    	try {
			return method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("方法不存在[" + methodName + "]",e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("方法不存在[" + methodName + "]",e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("方法不存在[" + methodName + "]",e);
		}
    	
    }

  
    private static BeanParser parseBean(Class<?> beanClass) {
    	 Map<String, FieldStruct> fields = parseAllFields(beanClass);
         Map<String, Method> methods = parseAllMethods(beanClass);
         Map<String, Method> declaredMethods = parseDeclaredMethod(beanClass);
         Map<String, FieldStruct> bstMap = getBstMap(fields, methods);
         return new BeanParser(bstMap, declaredMethods);
    }
    
    private static Map<String, FieldStruct> getBstMap(Map<String, FieldStruct> fields, Map<String, Method> methods) {
        Map<String, FieldStruct> ret = new HashMap<String, FieldStruct>();
        for (Map.Entry<String, FieldStruct> entry : fields.entrySet()) {
            String name = entry.getKey();
            FieldStruct bs = entry.getValue();
            Method setMethod = findMethod(methods, "set", name, bs.type);
            Method getMethod = findMethod(methods, "get", name, bs.type);
            if (setMethod == null && getMethod == null) continue;
            bs.setMethod(setMethod);
            bs.getMethod(getMethod);
            ret.put(name, bs);
        }
        return ret;
    }
    
    
    
    private static Map<String, FieldStruct> parseAllFields(Class<?> cls) {
        Map<String, FieldStruct> ret = new HashMap<String, FieldStruct>();
        
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            //静态的，final的或者本地的属性将被忽略
            if (isUnsetModifier(field.getModifiers())) continue;
            //如果属性标记了配置忽略属性，则解析配置时忽略它
            ConfigIgnored configIgnored = field.getAnnotation(ConfigIgnored.class);
            if (configIgnored != null && configIgnored.ignore()) continue;
            String name = field.getName();
            FieldStruct bs = new FieldStruct(field);
            bs.setInstanceType(cls);
            ret.put(name, bs);
        }
       
        
        if(cls.getSuperclass() != Object.class){
        	ret.putAll(parseAllFields(cls.getSuperclass()));;
        }
        return ret;
    }
    
    
    private static Map<String, Method> parseDeclaredMethod(Class<?> cls){
    	Map<String, Method> ret = new HashMap<String, Method>();
         Method[] methods = cls.getDeclaredMethods();
         for (Method method : methods) {
        	 ret.put(method.getName(), method);
         }
         return ret;
    }
    
    
    private static Map<String, Method> parseAllMethods(Class<?> cls) {
        Map<String, Method> ret = new HashMap<String, Method>();
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            ret.put(method.getName(), method);
        }
        
        if(cls.getSuperclass() != Object.class){
        	ret.putAll(parseAllMethods(cls.getSuperclass()));;
        }
        return ret;
    }
    
    
    
    private static Method findMethod(Map<String, Method> methods, String head, String name, Class<?> paramType) {
        String methodName = getMethodName(head, name);
        if (!methods.containsKey(methodName)) return null;
        Method method = methods.get(methodName);
        if ("set".equals(head) && (method.getReturnType() == void.class)) {
            Class<?>[] types = method.getParameterTypes();
            if (types != null && types.length == 1 && types[0] == paramType) {
                return method;
            }
        } else if ("get".equals(head) && (method.getReturnType() == paramType) && method.getParameterTypes().length == 0) {
            return method;
        }
        return null;
    }
    
    private static String getMethodName(String head, String name) {
        return head + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static boolean isUnsetModifier(int modifiers) {
        return Modifier.isFinal(modifiers) || Modifier.isNative(modifiers)
              || Modifier.isStatic(modifiers);
    }
    
    public static class FieldStruct {
        String name;
        Class<?> type;
        Class<?> rawType;
        Class<?> instanceType;
        Method setMethod;
        Method getMethod;

        FieldStruct(Field field) {
            this.name = field.getName();
            this.type = field.getType();

            if (this.type == List.class) {
               this.rawType = getParameterizedType(field.getGenericType());
            } else if (this.type.isArray()) {
                this.rawType = type.getComponentType();
            } else {
                this.rawType = this.type;
            }
        }

        public Class<?> getType() {
            return type;
        }

        void setMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        void getMethod(Method getMethod) {
            this.getMethod = getMethod;
        }

        public Object fieldValue(Object instance) {
            if (instance == null) return null;
            try {
                Field f = instance.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(instance);
            } catch (Exception e) {
                throw new RuntimeException("调用属性值发生异常[" + name + "]", e);
            }
        }
        
        
        public <T extends Annotation> T getAnnotation(Class<T> t){
        	try {
        		Field f = instanceType.getDeclaredField(name);
                return f.getAnnotation(t);
			} catch (Exception e) {
				e.printStackTrace();
				 throw new RuntimeException("调用注解发生异常[" + name + "]", e);
			}
        }
        
        public <T extends Annotation> T getGetMethodAnnotation(Object instance,Class<T> t){
        	if(instance == null) return null;
        	if(getMethod == null) return null;
        	try {
                return getMethod.getAnnotation(t);
			} catch (Exception e) {
				e.printStackTrace();
				 throw new RuntimeException("调用注解发生异常[" + name + "]", e);
			}
        }
        
        public boolean hasSetMethod() {
            return setMethod != null;
        }

        public boolean hasGetMethod() {
            return getMethod != null;
        }

        /**
         * 设置属性值
         * @param instance 待设置对象实例
         * @param value 属性值
         */
        public void set(Object instance, Object value) {
            if (instance == null || setMethod == null) return;
            try {
                setMethod.invoke(instance, new Object[]{value});
            } catch (Exception e) {
                throw new RuntimeException("调用set方法发生异常[" + name + "]", e);
            }
        }

        /**
         * 取得属性值
         * @param instance 待取得属性实例
         * @return 属性值
         */
        public Object get(Object instance) {
            if (instance == null || getMethod == null) return null;
            try {
                return getMethod.invoke(instance, new Object[]{});
            } catch (Exception e) {
            	e.printStackTrace();
                throw new RuntimeException("调用get方法发生异常[" + name + "]", e);
            }
        }

        /**
         * 取得数据原数据类型类对象。
         * <pre>
         * 例：List<String> 返回String.class
         *    int[] 返回int.class
         * </pre>
         * @return 数据原数据类型类对象
         */
        public Class<?> getRawType() {
            return rawType;
        }

      
		public Class<?> getInstanceType() {
			return instanceType;
		}

		public void setInstanceType(Class<?> instanceType) {
			this.instanceType = instanceType;
		}
        
        
	    public static Class<?> getParameterizedType(Type type) {
	        Class<?> ret = null;
	        if (type instanceof ParameterizedType) {
	            ParameterizedType parameterizedType = (ParameterizedType) type;
	            for (Type typeArg : parameterizedType.getActualTypeArguments()) {
	                if (Class.class.isAssignableFrom(typeArg.getClass())) {
	                    ret = (Class<?>) typeArg;
	                } else {
	                    ret = (Class<?>) ((ParameterizedType) typeArg).getRawType();
	                }

	            }
	        }
	        return ret;
	    }
        
    }
}
