package com.simple.kv.storage.converter.impl;

import java.util.Set;
import java.util.SortedSet;

import com.simple.base.util.CollectionUtil;

public class SetConverter extends CollectionConverter<Set<?>> {

	@Override
	protected Set<?> getCollectionImplement(Class<?> cls) {
		if (cls != null && SortedSet.class.isAssignableFrom(cls)) {
			return CollectionUtil.newTreeSet();
		} else {
			return CollectionUtil.newHashSet();
		}
	}

}
