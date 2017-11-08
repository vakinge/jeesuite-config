package com.jeesuite.admin.component;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class CacheComponent implements DisposableBean{

	private Map<String, Object> cache = new ConcurrentHashMap<>();
	private PriorityBlockingQueue<CacheKey> cacheKeys = new PriorityBlockingQueue<>();

	private ScheduledExecutorService cleanScheduledExecutor = Executors.newScheduledThreadPool(1);
	
	//缓存时间 （秒）
   private static int defaultExpireTime = 300;
		
	private int maxSize = 5000;
	
	private AtomicInteger currentCacheSize = new AtomicInteger(0);

	public CacheComponent() {
		this(5000);
	}
	
	public CacheComponent(final long period,int maxSize) {
		this(period);
		this.maxSize = maxSize;
	}

	/**
	 * @param period
	 *            检查过期间隔（毫秒）
	 */
	public CacheComponent(final long period) {
		
		cleanScheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				CacheKey cacheKey = cacheKeys.poll();
				if(cacheKey == null){
					return;
				}
				
				long currentTimeMils = System.currentTimeMillis();
				if(cacheKey.expireAt > currentTimeMils){
					//放回去
					cacheKeys.add(cacheKey);
					return;
				}
				// 过期的移除
				cache.remove(cacheKey.key);
				currentCacheSize.decrementAndGet();
				
			}
		}, period, period, TimeUnit.MILLISECONDS);
		
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 *            单位：秒
	 * @return
	 */
	public <T> boolean set(String key, T value, int timeout) {
		
		if(currentCacheSize.incrementAndGet() > maxSize)throw new RuntimeException("CacheSize over the max size");

		cache.put(key, value);
		if (timeout > 0) {
			cacheKeys.add(new CacheKey(key, System.currentTimeMillis() + timeout * 1000));
		}
		return true;
	
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) cache.get(key);
	}

	public boolean remove(String key) {
		Object removeObj = cache.remove(key);
		if(removeObj != null){
			cacheKeys.remove(new CacheKey(key, 0));
			currentCacheSize.decrementAndGet();
		}
		return true;
	}

	public boolean exists(String key) {
		return cache.containsKey(key);
	}

	public <T> T queryTryCache(String key,Callable<T> dataCaller){
		return queryTryCache(key, dataCaller, defaultExpireTime);
	}
	
	public <T> T queryTryCache(String key,Callable<T> dataCaller,int expire){
		T result = get(key);
		if(result == null){
			try {				
				result = dataCaller.call();
				if(result != null){
					set(key, result, expire);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else{
		}
		return result;
	}

	private class CacheKey implements Comparable<CacheKey>{
		String key;
		long expireAt;

		public CacheKey(String key, long expireAt) {
			super();
			this.key = key;
			this.expireAt = expireAt;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

		private CacheComponent getOuterType() {
			return CacheComponent.this;
		}

		@Override
		public int compareTo(CacheKey o) {
			return Long.compare(this.expireAt, o.expireAt);
		}

		@Override
		public String toString() {
			return "[key=" + key + ", expireAt=" + expireAt + "]";
		}

	}

	@Override
	public void destroy() throws Exception {
		cleanScheduledExecutor.shutdown();
	}

}
