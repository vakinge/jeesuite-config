package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppconfigEntity;

public interface AppconfigEntityMapper extends CustomBaseMapper<AppconfigEntity> {
	
	@Select("SELECT * FROM app_configs  where name=#{name} limit 1")
	@ResultMap("BaseResultMap")
	AppconfigEntity findByName(String name);
	
	List<AppconfigEntity> findGlobalConfig(@Param("env") String env,@Param("version") String version);
	
	List<AppconfigEntity> findByQueryParams(Map<String, Object> params);
}