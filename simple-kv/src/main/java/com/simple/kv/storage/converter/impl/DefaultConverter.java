package com.simple.kv.storage.converter.impl;

import com.simple.kv.storage.converter.IConverter;

public class DefaultConverter<T> implements IConverter<T> {


	@SuppressWarnings("unchecked")
	@Override
	public T convert(Object value, Object... params) {
		return (T)value;
	}

	@Override
	public String toString(Object value, Object... params) {
		return value != null ? value.toString() : "null";
	}

}
