package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class DoubleConverter extends DefaultConverter<Double> {

	@Override
	public Double convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		if (value instanceof String) {
			return NumberUtil.getDouble((String) value);
		}
		return null;
	}

}
