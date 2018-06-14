package com.lebaoxun.soa.core.redis.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.lebaoxun.commons.utils.SerializeUtil;
import com.lebaoxun.soa.core.redis.IRedisCache;

/**
 * IRedisCache瀹炵幇
 * 
 */
@Repository("redisCache")
public class RedisCacheImpl implements IRedisCache {

	@Resource
	private RedisTemplate<String,Object> redisTemplate;

	@Override
	public void set(String key, Object obj) {
		redisTemplate.opsForValue().set(key, obj);
	}

	@Override
	public void set(String ks, Object obj,
			Long expire) {
		redisTemplate.opsForValue().set(ks, obj,expire,TimeUnit.SECONDS);
	}

	@Override
	public boolean exists(String key) {
		return redisTemplate.hasKey(key);
	}
	
	@Override
	public void del(final String... keys) {
		if (keys != null && keys.length > 0) {
			if (keys.length == 1)
				redisTemplate.delete(keys[0]);
			else
				for (String key : keys) {
					del(key);
				}
		}
	}

	@Override
	public boolean update(final String ks, final Object obj,
			final Long expire) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection con)
					throws DataAccessException {
				long t = expire != null && expire > 0 ? expire : con.ttl(ks.getBytes());

				if (obj != null) {
					if (t > 0) {
						redisTemplate.opsForValue().set(ks, obj,expire,TimeUnit.SECONDS);
					}else{
						redisTemplate.opsForValue().set(ks, obj);
					}
				}
				return true;
			}
		}, true);
	}

	@Override
	public Object get(final String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	@Override
	public Long ttl(final String ks) {
		return redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection con)
					throws DataAccessException {
				byte[] k = ks.getBytes();
				return con.ttl(k);
			}
		}, true);
	}

	@Override
	public Set<String> searchKey(final String pattern) {
		return redisTemplate.keys(pattern);
	}

	@Override
	public void clear(final String key) {
		Set<String> keys = searchKey(key);
		if (keys != null) {
			this.del(keys.toArray(new String[]{}));
		}
	}

	@Override
	public int size(String key) {
		Set<String> keys = searchKey(key);
		if (keys != null) {
			return keys.size();
		}

		return 0;
	}

	/**
	 * 累加
	 * @param key
	 */
	@Override
	public void increment(String key,long delta){
		redisTemplate.opsForValue().increment(key, delta);
	}

}
