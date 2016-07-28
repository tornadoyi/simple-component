package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class BooleanConverter extends DefaultConverter<Boolean> {

	@Override
	public Boolean convert(Object value, Object... params) {
		if(value == null) {
			return false;
		}
		if(value instanceof Boolean) {
			return (Boolean)value;
		}
		if(value instanceof String || value instanceof Number) {
			return NumberUtil.getBoolean(value.toString());
		}
		return false;
	}

}
