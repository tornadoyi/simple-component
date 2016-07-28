package com.simple.kv.storage.converter.impl;

public class CharacterConverter extends DefaultConverter<Character> {

	@Override
	public Character convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Character) {
			return (Character) value;
		}
		if (value instanceof Number) {
			return (char) ((Number) value).intValue();
		}
		if (value instanceof String) {
			String s = (String) value;
			if (s.length() > 0) {
				return s.charAt(0);
			}
		}
		return null;
	}
	
}
