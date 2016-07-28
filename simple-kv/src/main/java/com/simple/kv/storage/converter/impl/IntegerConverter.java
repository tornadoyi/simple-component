package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class IntegerConverter extends DefaultConverter<Integer> {

	@Override
	public Integer convert(Object value, Object... params) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			return NumberUtil.getInteger((String) value);
		}
		if (value instanceof Character) {
			return (int)((char)((Character)value));
		}
		return null;
	}

}
