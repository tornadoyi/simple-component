package com.simple.kv.storage.serialization;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.serialization.anno.HeSerializableField;

/**
 * 可序列化对象中的字段信息
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
class HeSerializableObjectFieldInfo {

	private static final Logger logger = Logger.getLogger(HeSerializableObjectFieldInfo.class);

	private String name;
	private TypeInfo typeInfo;
	private Field field;
	private HeSerializableField annoSerializableField;

	/**
	 * @return 字段名
	 */
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	/**
	 * @return 字段类型信息
	 */
	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	void setTypeInfo(TypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

	void setField(Field field) {
		field.setAccessible(true);
		this.field = field;
	}

	/**
	 * @return 字段的注解，提供了序列化的相关信息
	 */
	public HeSerializableField getAnnoSerializableField() {
		return annoSerializableField;
	}

	void setAnnoSerializableField(HeSerializableField annoSerializableField) {
		this.annoSerializableField = annoSerializableField;
	}

	/**
	 * 读取字段值
	 * 
	 * @param obj
	 *            对象实例
	 * @return 字段值
	 */
	@SuppressWarnings("unchecked")
	public <T> T readValue(Object obj) {
		try {
			return (T) field.get(obj);
		} catch (Exception e) {
			logger.error("read field value fail", e);
			return null;
		}
	}

	/**
	 * 写入字段值
	 * 
	 * @param obj
	 *            对象实例
	 * @param value
	 *            字段值
	 */
	public void writeValue(Object obj, Object value) {
		try {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(obj, value);
		} catch (Exception e) {
			logger.error("write field value fail " + (field.getDeclaringClass().getName()) + " " + field.getName() + " " + field.getType().getName() + " "
					+ (value != null ? value.getClass().getName() : null), e);
		}
	}

}
