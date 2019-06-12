package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppconfigEntity;

public interface AppconfigEntityMapper extends CustomBaseMapper<AppconfigEntity> {
	
	
	List<AppconfigEntity> findGlobalConfig(@Param("env") String env,@Param("version") String version);
	
	List<AppconfigEntity> findByQueryParams(Map<String, Object> params);
}