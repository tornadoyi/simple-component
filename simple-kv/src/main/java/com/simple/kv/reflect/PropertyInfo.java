package com.simple.kv.reflect;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;


/**
 * bean中的属性信息
 *
 */
public class PropertyInfo {

	private static final Logger logger = Logger.getLogger(PropertyInfo.class);

	private String name;
	private TypeInfo type;
	private Method writeMethod;
	private Method readMethod;
	private SpecialProperty annoSpecialProperty;

	/**
	 * @return 属性名
	 */
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	/**
	 * @return 类型信息
	 */
	public TypeInfo getType() {
		return type;
	}

	void setType(TypeInfo type) {
		this.type = type;
	}

	/**
	 * @return getter方法
	 */
	public Method getWriteMethod() {
		return writeMethod;
	}

	void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
	}

	/**
	 * @return setter方法
	 */
	public Method getReadMethod() {
		return readMethod;
	}

	void setReadMethod(Method readMethod) {
		this.readMethod = readMethod;
	}

	/**
	 * @return 对属性的注解，表明对bean中某个属性的特殊处理，可以放在getter方法、setter方法的其中一项上，如果这二项都存在该注解，则优先选取getter
	 */
	public SpecialProperty getSpecialProperty() {
		return annoSpecialProperty;
	}

	void setAnnoSpecialProperty(SpecialProperty specialProperty) {
		this.annoSpecialProperty = specialProperty;
	}

	/**
	 * @return 是否可读
	 */
	public boolean canRead() {
		return readMethod != null;
	}

	/**
	 * @return 是否可写
	 */
	public boolean canWrite() {
		return writeMethod != null;
	}

	/**
	 * 读取属性值
	 * 
	 * @param bean
	 *            对象实例
	 * @return 属性值
	 */
	@SuppressWarnings("unchecked")
	public <T> T readValue(Object bean) {
		if (readMethod != null) {
			try {
				return (T) readMethod.invoke(bean);
			} catch (Exception e) {
				logger.error("read property value fail", e);
			}
		}
		return null;
	}

	/**
	 * 写入属性值
	 * 
	 * @param bean
	 *            对象实例
	 * @param value
	 *            属性值
	 */
	public void writeValue(Object bean, Object value) {
		if (writeMethod != null) {
			try {
				writeMethod.invoke(bean, value);
			} catch (Exception e) {
				logger.error("write property value fail", e);
			}
		}
	}

}
