package com.simple.kv.storage;

/**
 * 持久化操作的类型，目前支持set/get/delete及它们的批量操作，以及cas，该常量主要用在标记持久化方法的注解{@link com.happyelements.rdcenter.storage.anno.Storage}中，
 * 持久化方法存在于持久化接口或持久化类中 <h1>持久化方法的key及value参数说明</h1> <li>需要key参数的方法，key参数用{@link com.happyelements.rdcenter.storage.anno.Pkey}修饰</li>
 * <li>需要value参数的方法，value参数用{@link com.happyelements.rdcenter.storage.anno.PValue}修饰</li>
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public enum OperationType {

	/** 存储操作，需要key及value参数，或是由实现了KeyParamAware接口的对象来同时传递key及value */
	SET(true, false),

	/** 批量存储操作，使用Map来传递多个key及value，也可以使用由实现了KeyParamAware接口的元素组成的集合来传参 */
	BATCH_SET(true, true),
	
	/** 增加操作，一般用于用户初始化时：if ( xxx == null ) { call add for init } else { call set for update } */
	ADD(true, false),

	/** 获取操作，需要key参数，返回值的类型须与value的类型匹配 */
	GET,

	/** 批量获取操作，使用集合传递多个key，使用Map作为返回值来表示key及value的映射，也可以使用由实现了KeyParamAware接口的元素组成的集合作为返回值，返回值的容量小于等于key参数的容量（不一定所有的key都有相应value） */
	BATCH_GET(false, true),

	/** 删除操作，需要key参数 */
	DELETE,

	/** 批量删除操作， 使用集合传递多个key */
	BATCH_DELETE(false, true),

	/**
	 * <li>和cas相匹配的操作，即取值的同时获得cas version</li> <li>参数要求和{@link #GET}操作相同，返回值为{@link com.happyelements.rdcenter.storage.CasResult}，包含value和cas version</li>
	 */
	GET_FOR_CAS,

	/**
	 * <li>compare and set，除了和{@link #SET}操作相同的参数要求，还需要使用注解{@link com.happyelements.rdcenter.storage.anno.CasVersion}来修饰cas version参数</li> <li>返回值为{@link com.happyelements.rdcenter.storage.CasStatus}
	 * ，如果version与旧值version一致则新值被存储（旧值不存在的情况下，也存储新值），否则新值不被存储，上层程序需要继续进行{@link #GET_FOR_CAS}操作来进行重试</li>
	 */
	CAS(true, false),

	;

	private boolean needValue;

	private boolean batch;

	/**
	 * @return 返回这个操作是否需要传递value参数
	 */
	public boolean needValue() {
		return needValue;
	}

	/**
	 * @return 返回这个操作是否为批量操作
	 */
	public boolean isBatch() {
		return batch;
	}

	private OperationType() {
	}

	private OperationType(boolean needValue, boolean batch) {
		this.needValue = needValue;
		this.batch = batch;
	}

}
