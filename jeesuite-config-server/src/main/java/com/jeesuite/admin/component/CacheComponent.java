package com.jeesuite.admin.component;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jeesuite.common.util.ResourceUtils;

//@Component
public class CacheComponent {

	private Cache<String,Object> cache;
	
	private int cacheExpireSeconds = ResourceUtils.getInt("cache.expire.seconds", 3600);

	public CacheComponent() {
		if(!ResourceUtils.containsProperty("jeesuite.cache.servers")) {
			cache  =  CacheBuilder
					.newBuilder()
					.maximumSize(5000)
					.expireAfterWrite(cacheExpireSeconds, TimeUnit.SECONDS)
					.build();
		}
	}
	
	public void set(String key,Object value) {
		if(cache == null) {
		}else {
			cache.put(key, value);
		}
	}
	
    public void remove(String key) {
        if(cache == null) {
			
		}else {
			cache.invalidate(key);
		}
	}
	
}
