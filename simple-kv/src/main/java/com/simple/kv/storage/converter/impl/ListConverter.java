package com.simple.kv.storage.converter.impl;

import java.util.AbstractSequentialList;
import java.util.List;

import com.simple.base.util.CollectionUtil;

public class ListConverter extends CollectionConverter<List<?>> {

	@Override
	protected List<?> getCollectionImplement(Class<?> cls) {
		if(cls != null && AbstractSequentialList.class.isAssignableFrom(cls)) {
			return CollectionUtil.newLinkedList();
		} else {
			return CollectionUtil.newArrayList();
		}
	}
	
}
