package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class ByteConverter extends DefaultConverter<Byte> {

	@Override
	public Byte convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if(value instanceof Number) {
			return ((Number) value).byteValue();
		}
		if(value instanceof String) {
			return NumberUtil.getByte((String)value);
		}
		if (value instanceof Character) {
			return (byte)((char)((Character)value));
		}
		return null;
	}

}
