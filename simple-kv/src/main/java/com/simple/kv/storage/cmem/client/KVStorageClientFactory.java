/**
 * created by haitao.yao @ May 11, 2011
 */
package com.simple.kv.storage.cmem.client;


/**
 * @author haitao.yao @ May 11, 2011
 * 
 */
public class KVStorageClientFactory {
    
    public static final KVStorageClient getStorageClient() {
        return getStorageClient("default");
    }
    
	public static final KVStorageClient getStorageClient(String name) {
		return KVStorageClientImpl.getInstance(name);
	}
}
