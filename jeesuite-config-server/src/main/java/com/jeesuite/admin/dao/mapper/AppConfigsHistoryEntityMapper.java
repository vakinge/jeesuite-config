package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;

public interface AppConfigsHistoryEntityMapper extends CustomBaseMapper<AppConfigsHistoryEntity> {
	
	@Select("SELECT * FROM app_configs_history  where origin_id=#{configId} ORDER BY id DESC LIMIT 10")
	@ResultMap("BaseResultMap")
	List<AppConfigsHistoryEntity> findByConfigId(Integer configId);
}