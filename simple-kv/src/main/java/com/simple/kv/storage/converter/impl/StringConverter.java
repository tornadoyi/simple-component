package com.simple.kv.storage.converter.impl;


public class StringConverter extends DefaultConverter<String> {
	
	@Override
	public String convert(Object value, Object... params) {
		return value != null ? value.toString() : null;
	}

}
