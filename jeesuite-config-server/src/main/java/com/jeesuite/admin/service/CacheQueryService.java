package com.jeesuite.admin.service;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jeesuite.admin.component.CacheComponent;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;

@Service
public class CacheQueryService {

	private @Autowired AppEntityMapper appMapper;
	
	@Autowired
	private CacheComponent cacheComponent;
	
	public AppEntity findAppEntity(final int appId){
		return cacheComponent.queryTryCache("appEntity:"+appId, new Callable<AppEntity>() {
			@Override
			public AppEntity call() throws Exception {
				return appMapper.selectByPrimaryKey(appId);
			}
		});
	}
}
