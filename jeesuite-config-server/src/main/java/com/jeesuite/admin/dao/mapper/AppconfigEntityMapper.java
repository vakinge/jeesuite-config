package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppconfigEntity;

public interface AppconfigEntityMapper extends CustomBaseMapper<AppconfigEntity> {
	
	
	List<AppconfigEntity> findByQueryParams(Map<String, Object> params);
	
	int countByQueryParams(Map<String, Object> params);
}