package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Update;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.mybatis.plugin.cache.annotation.Cache;

public interface AppconfigEntityMapper extends CustomBaseMapper<AppconfigEntity> {
	
	AppconfigEntity findSameByName(@Param("env") String env,@Param("appId") Integer appId,@Param("name") String name);
	
	@Cache
	List<AppconfigEntity> findGlobalConfig(@Param("env") String env,@Param("groupId") Integer groupId,@Param("version") String version);
	
	@Cache
	List<AppconfigEntity> findByQueryParams(Map<String, Object> params);
	
	@Update("UPDATE app_configs SET enabled=0 WHERE app_id=#{appId}")
	@ResultType(Integer.class)
	int deleteByAppId(Integer appId);

}