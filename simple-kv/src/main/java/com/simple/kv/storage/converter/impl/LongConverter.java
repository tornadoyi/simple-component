package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class LongConverter extends DefaultConverter<Long> {

	@Override
	public Long convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value instanceof String) {
			return NumberUtil.getLong((String) value);
		}
		if (value instanceof Character) {
			return (long)((char)((Character)value));
		}
		return null;
	}

}
