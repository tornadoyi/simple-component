package com.simple.kv.storage.converter.impl;

import com.simple.base.util.NumberUtil;

public class ShortConverter extends DefaultConverter<Short> {

	@Override
	public Short convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		}
		if (value instanceof String) {
			return NumberUtil.getShort((String) value);
		}
		if (value instanceof Character) {
			return (short)((char)((Character)value));
		}
		return null;
	}


}
