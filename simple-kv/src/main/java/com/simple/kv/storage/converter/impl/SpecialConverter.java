package com.simple.kv.storage.converter.impl;

import java.util.Arrays;

import com.simple.kv.storage.converter.IConverter;


public class SpecialConverter implements IConverter<Object> {

	private IConverter<?> converter;
	private Object[] args;
	
	public SpecialConverter(IConverter<?> converter, Object... args) {
		this.converter = converter;
		this.args = args;
	}

	@Override
	public Object convert(Object value, Object... params) {
		return converter.convert(value, params != null && params.length > 0 ? params : args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value, Object... params) {
		return ((IConverter<Object>)converter).toString(value, params != null && params.length > 0 ? params : args);
	}

	@Override
	public String toString() {
		return "SpecialConverter [converter=" + converter + ", args=" + Arrays.toString(args) + "]";
	}
	
}
