/**
 * $Id$
 * Copyright(C) 2010-2016 happyelements.com. All rights reserved.
 */
package com.simple.base.jedis;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;

import com.simple.base.util.CollectionUtil;
import com.simple.base.util.tuple.TwoTuple;

/**
 * 配置文件：redis_conf
 * <redis_conf>
 * <host>10.130.136.32</host>
 * <port>6379</host>
 * 
 * </redis_conf>
 * 
 * @author <a href="mailto:yongliang.zhao@happyelements.com">yongliang.zhao</a>
 * @version 1.0
 * @since 1.0
 */
public class JedisClient {

	private volatile static Map<String, JedisClient> clientMap = new ConcurrentHashMap<String, JedisClient>();

	public static JedisClient getInstance(String instanceName) {
		JedisConfig config = JedisConfig.getConfig(instanceName);
		JedisClient instance = clientMap.get(instanceName);
		if (instance == null) {
			synchronized (JedisClient.class) {
				if (instance == null) {
					instance = new JedisClient(instanceName, config.getHost(), config.getPort(), config.getMaxIdel(),
							config.getMaxWait(), config.getPoolTimeWait());
					clientMap.put(instanceName, instance);
				}
			}
		}
		return instance;
	}
	
	private JedisPoolConfig config;
	private JedisPool pool;
	private String instanceName;

	private JedisClient(String instanceName, String host, int port, int maxIdel, int maxWait, int poolTimeWait) {
		this.instanceName = instanceName;
		config = new JedisPoolConfig();
		config.setMaxIdle(maxIdel);
		config.setMaxWait(maxWait);
		pool = new JedisPool(config, host, port, poolTimeWait);
	}

	private String buildKey(String key) {
		return instanceName + "#" + key;
	}

	/**
	 * 获取对应Key的Value。如果key不存在，则返回null。
	 * 此操作只允许获取String类型数据。假如此Key对应的value不是String就会返回error。
	 * 
	 * @param key
	 * @return
	 * @throws JedisException
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			T t = (T) jedis.get(key);
			return t;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long incrBy(String key, long inc) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.incrBy(key, inc);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void set(String key, String value) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.set(key, value);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void expire(String key, int timeSecond) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.expire(key, timeSecond);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void expireAt(String key, int unixTime) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.expireAt(key, unixTime);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void delKey(String key) throws JedisException {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.del(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 批量将值插入sorted sets中，如果member对应的value不存在，则初始化，否则更新此member对应value
	 * 
	 * @param key
	 *            jedis中key
	 * @param scoreMembers
	 *            添加(修改的)sets , key:score,value:member
	 */
	public void zadd(String key, Map<Double, String> scoreMembers) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.zadd(key, scoreMembers);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 将一个或多个member元素及其score值加入到有序集key当中。
	 * 如果某个member已经是有序集的成员，那么更新这个member的score值，并通过重新插入这个member元素，来保证该member在正确的位置上。
	 * score值可以是整数值或双精度浮点数。
	 * 如果key不存在，则创建一个空的有序集并执行ZADD操作。
	 * 当key存在但不是有序集类型时，返回一个错误。
	 * 
	 * @param key
	 *            jedis中对应的key
	 * @param score
	 *            分数，double
	 * @param member
	 *            当前分数对应的对象
	 */
	public void zadd(String key, double score, String member) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			jedis.zadd(key, score, member);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回升序排名的用户名和分数的集合,即分数从最低到最高的排列，分数相等的两个member根据字典排序顺序返回。
	 * 如果要获取降序排列，请使用Zrevrange接口。
	 * start和stop的名次都是基于0的，也就是0是第一个元素，1是第二个元素，以此类推。
	 * 他们也可以是负数，表示从sorted set最后开始定位，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 * 假如说越界了，不会产生错误。如果开始位（start）比整个sorted set都大，或者start大于stop，那么会返回一个空的list。
	 * 如果stop比真个sorted sets都大，redis会把stop认为是此sorted set中的最后一个元素。
	 * 
	 * @param key
	 *            jedis中对应的key
	 * @param start
	 *            start index，start with 0,
	 * @param stop
	 */
	public LinkedHashSet<String> zrange(String key, long start, long stop) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<String> result = (LinkedHashSet<String>) jedis.zrange(key, start, stop);
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回升序排序排列中特定排名段内的member、score的列表。次序从低分到高分，分数相同的按照member的字典排序。
	 * 假如需要从最高到最低获取排序，请参考Zrevrange接口。
	 * start和stop的名次都是基于0的，也就是0是第一个元素，1是第二个元素，以此类推。
	 * 他们也可以是负数，表示从sorted set最后开始定位，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 * 返回member、score对应的LinkedList
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return LinkedList<TwoTuple<String, Double>> first:member,second:score
	 */
	public LinkedList<TwoTuple<String, Double>> zrangeWithScore(String key, long start, long end) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrangeWithScores(key, start, end);
			LinkedList<TwoTuple<String, Double>> result = new LinkedList<TwoTuple<String, Double>>();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回指定分数区间内的所有member以及对应的分数(左闭右闭)，返回List顺序按照score从大到小排列，
	 * 分数相同的两个member按照字典排序返回
	 * 假如需要获取升序排序(分数从低到高)，请参考接口ZRANGEBYSCORE
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public LinkedList<TwoTuple<String, Double>> zrangeByScore(String key, double min, double max) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrangeByScoreWithScores(key, min, max);
			LinkedList<TwoTuple<String, Double>> result = CollectionUtil.newLinkedList();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public LinkedList<TwoTuple<String, Double>> zrangeByScore(String key, final double min,
			final double max, final int offset, final int count) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrangeByScoreWithScores(key, min, max,
					offset, count);
			LinkedList<TwoTuple<String, Double>> result = CollectionUtil.newLinkedList();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回mem对应的排名(从低到高排名，假如这个哥们分数最低，则返回的是0而不是1)
	 * 假如需要获取分数从大到小的排名，请使用方法ZREVRANK
	 * 假如对应member的在sorted set中存在，返回对应的rank。
	 * 如果member不存在，或者key对应的sorted set不存在，返回null
	 * 
	 * @param key
	 * @param member
	 */
	public Long zrank(String key, String member) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			Long index = jedis.zrank(key, member);
			return index;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回mem对应的排名(从高到低排名，假如这个哥们分数最高，则返回的是0而不是1)
	 * 假如需要获取分数从小到大的排名，请使用方法ZRANK
	 * 假如对应member的在sorted set中存在，返回对应的rank。
	 * 如果member不存在，或者key对应的sorted set不存在，返回null
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zrevrank(String key, String member) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			Long index = jedis.zrevrank(key, member);
			return index;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回降序排名的用户名和分数的集合,即分数从最高到最低的排列，分数相等的两个member根据字典排序顺序返回。
	 * 如果要获取升序排列，请使用Zrevrange接口。
	 * start和stop的名次都是基于0的，也就是0是第一个元素，1是第二个元素，以此类推。
	 * 他们也可以是负数，表示从sorted set最后开始定位，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 * 假如说越界了，不会产生错误。如果开始位（start）比整个sorted set都大，或者start大于stop，那么会返回一个空的list。
	 * 如果stop比真个sorted sets都大，redis会把stop认为是此sorted set中的最后一个元素，即分数最低的元素。
	 * 
	 * @param key
	 * @param start
	 * @param stop
	 * @return
	 */
	public LinkedHashSet<String> zrevrange(String key, long start, long end) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<String> result = (LinkedHashSet<String>) jedis.zrevrange(key, start, end);
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回升序排序排列中特定排名段内的member、score的列表。次序从高分到低分，分数相同的按照member的字典排序。
	 * 假如需要从最高到最低获取排序，请参考Zrevrange接口。
	 * start和stop的名次都是基于0的，也就是0是第一个元素，1是第二个元素，以此类推。
	 * 他们也可以是负数，表示从sorted set最后开始定位，-1表示最后一个元素，-2表示倒数第二个，以此类推
	 * 返回member、score对应的LinkedList
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public LinkedList<TwoTuple<String, Double>> zrevrangeWithScore(String key, long start, long end) {
		key = buildKey(key);
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrevrangeWithScores(key, start, end);
			LinkedList<TwoTuple<String, Double>> result = CollectionUtil.newLinkedList();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			if (null != jedis)
				pool.returnResource(jedis);
		}
	}

	/**
	 * 返回指定分数段内的member以及对应的分数。返回顺序按照分数从大到小排列，分数相同的两个人，按照字典排序返回
	 * 从低到高的排序返回请参考接口ZRANGEBYSCORE
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public LinkedList<TwoTuple<String, Double>> zrevrangeByScore(String key, double max, double min) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrevrangeByScoreWithScores(key, max, min);
			LinkedList<TwoTuple<String, Double>> result = CollectionUtil.newLinkedList();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public LinkedList<TwoTuple<String, Double>> zrevrangeByScore(String key, final double min,
			final double max, final int offset, final int count) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			LinkedHashSet<Tuple> jedisResult = (LinkedHashSet<Tuple>) jedis.zrevrangeByScoreWithScores(key, min, max,
					offset, count);
			LinkedList<TwoTuple<String, Double>> result = CollectionUtil.newLinkedList();
			for (Tuple tuple : jedisResult) {
				result.add(new TwoTuple<String, Double>(tuple.getElement(), tuple.getScore()));
			}
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 返回指定member对应的分数
	 * 如果member不存在，或者key对应的sorted set不存在，则返回null
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public Double zscore(String key, String member) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			Double result = jedis.zscore(key, member);
			return result;
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 增加指定member的score
	 * 
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public Double zincrBy(String key, Double score, String member) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.zincrby(key, score, member);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 删除key对应的sorted set中的指定members,
	 * 如果member不存在,则不进行任何操作,
	 * 如果key所对应value不是set,返回错误<br>
	 * 
	 * 返回：实际删除的member个数.P.S:1表示member被删除;0表示member不存在
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zremoveMember(String key, String... members) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.zrem(key, members);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	/**
	 * 删除指定key的sorted set中rank位于start和end之间的所有元素.start和end都是基于0的数字,其中rank=0 是最低的分数.
	 * start和end都可以为负数,表示从最高rank起始的元素,例如:-1为最高分,-2为第二高分等.返回删除元素的个数.
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public long zremRangeByRank(String key, long start, long end) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.zremrangeByRank(key, start, end);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long zcount(String key, final double min, final double max) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.zcount(key, min, max);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long zcard(String key) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.zcard(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long sadd(String key, final String... members) {
		Jedis jedis = pool.getResource();
		key = buildKey(key);
		try {
			return jedis.sadd(key, members);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long srem(String key, final String... members) {
		Jedis jedis = pool.getResource();
		key = buildKey(key);
		try {
			return jedis.srem(key, members);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long scard(String key) {
		Jedis jedis = pool.getResource();
		key = buildKey(key);
		try {
			return jedis.scard(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Boolean sismember(String key, final String member) {
		Jedis jedis = pool.getResource();
		key = buildKey(key);
		try {
			return jedis.sismember(key, member);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Set<String> smembers(String key) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.smembers(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long hset(String key, final String field, final String value) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.hset(key, field, value);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String hget(String key, final String field) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.hget(key, field);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long rpushx(String key, final String string) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.rpushx(key, string);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long rpush(String key, final String string) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.rpush(key, string);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long lpush(String key, final String... strings) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.lpush(key, strings);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long lrem(String key, final int count, final String value) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.lrem(key, count, value);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String lpop(String key) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.lpop(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String rpop(String key) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.rpop(key);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public List<String> lrange(String key, final long start,
			final long end) {
		Jedis jedis = pool.getResource();
		key = buildKey(key);
		try {
			return jedis.lrange(key, start, end);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String getSet(String key, final String value) {
		key = buildKey(key);
		Jedis jedis = pool.getResource();
		try {
			return jedis.getSet(key, value);
		} catch (RuntimeException e) {
			throw new JedisException(e);
		} finally {
			pool.returnResource(jedis);
		}
	}

}
