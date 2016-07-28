package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class FloatConverter extends DefaultConverter<Float> {

	@Override
	public Float convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		if (value instanceof String) {
			return NumberUtil.getFloat((String) value);
		}
		return null;
	}

}
