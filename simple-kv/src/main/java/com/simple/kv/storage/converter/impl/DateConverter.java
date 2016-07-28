package com.simple.kv.storage.converter.impl;

import java.util.Date;

import com.simple.base.util.DateTimeUtil;
import com.simple.kv.storage.converter.IConverter;

public class DateConverter implements IConverter<Date> {

	/**
	 * <pre>
	 * 额外参数：
	 * 0 - 格式化信息(String)，默认为yyyy-MM-dd HH:mm:ss
	 * </pre>
	 */
	@Override
	public Date convert(Object value, Object... params) {
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return (Date) value;
		}
		if (value instanceof Number) {
			return new Date(((Number) value).longValue());
		}
		if (value instanceof String) {
			String fmt;
			if (params != null && params.length > 0 && params[0] instanceof String) {
				fmt = (String) params[0];
			} else {
				fmt = DateTimeUtil.YMD_HMS;
			}
			return DateTimeUtil.parse((String) value, fmt);
		}
		return null;
	}

	/**
	 * <pre>
	 * 额外参数：
	 * 0 - 格式化信息(String)，默认为yyyy-MM-dd HH:mm:ss
	 * </pre>
	 */
	@Override
	public String toString(Date value, Object... params) {
		if (value == null) {
			return "null";
		}
		String fmt;
		if (params != null && params.length > 0 && params[0] instanceof String) {
			fmt = (String) params[0];
		} else {
			fmt = DateTimeUtil.YMD_HMS;
		}
		return DateTimeUtil.format((Date) value, fmt);
	}

}
