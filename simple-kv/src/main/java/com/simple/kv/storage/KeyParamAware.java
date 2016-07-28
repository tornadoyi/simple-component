package com.simple.kv.storage;

/**
 * <li>存储对象可以实现此接口，从中获取及设置keyParam</li> 
 * <li>对于那些实现了该接口的参数对象，在set等操作时，可以不再需要单独的key参数，而将@pkey也标注在此参数上；
 * 在batch get操作时，不但可以返回Map&lt;Key,Value&gt;，还可以返回Collection&lt;Value&gt;</li>
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public interface KeyParamAware<T> {

	/**
	 * 读取对象中的keyParam，联合key请用元组返回，具体参见元组工厂类{@link com.simple.base.util.tuple.Tuple}
	 * 
	 * @return
	 */
	public T readKeyParam();

	/**
	 * 框架组装对象时，调用此方法设置注入keyParam，如果是联合key将使用元组传参，具体参见元组工厂类{@link com.simple.base.util.tuple.Tuple}
	 * 
	 * @param keyParam
	 */
	public void writeKeyParam(T keyParam);

}
