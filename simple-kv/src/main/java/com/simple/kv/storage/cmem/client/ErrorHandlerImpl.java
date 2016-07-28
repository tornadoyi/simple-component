/**
 * $Id$
 * Copyright(C) 2010-2016 happyelements.com. All rights reserved.
 */
package com.simple.kv.storage.cmem.client;

import com.simple.kv.storage.cmem.client.memcached.ErrorHandler;
import com.simple.kv.storage.cmem.client.memcached.MemcachedClient;

/**
 *
 * @author <a href="mailto:well.cheng@happyelements.com">well.cheng</a>
 * @version 1.0
 * @since 1.0
 */
public class ErrorHandlerImpl implements ErrorHandler {
	@Override
	public void handleErrorOnGet(MemcachedClient client, Throwable error, String cacheKey) {
		throw new RuntimeException(error);
	}

	@Override
	public void handleErrorOnGet(MemcachedClient client, Throwable error, String[] cacheKeys) {
		throw new RuntimeException(error);

	}

	@Override
	public void handleErrorOnSet(MemcachedClient client, Throwable error, String cacheKey) {
		throw new RuntimeException(error);
	}

	@Override
	public void handleErrorOnDelete(MemcachedClient client, Throwable error, String cacheKey) {
		throw new RuntimeException(error);
	}
	
	@Override
	public void handleErrorOnInit(MemcachedClient client, Throwable error) {

	}

	@Override
	public void handleErrorOnFlush(MemcachedClient client, Throwable error) {
		
	}

	@Override
	public void handleErrorOnStats(MemcachedClient client, Throwable error) {

	}

}
