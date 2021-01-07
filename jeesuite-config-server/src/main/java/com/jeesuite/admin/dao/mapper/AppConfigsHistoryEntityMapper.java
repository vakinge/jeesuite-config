package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;

public interface AppConfigsHistoryEntityMapper extends CustomBaseMapper<AppConfigsHistoryEntity> {
	
	@Select("SELECT * FROM app_configs_history  where origin_id=#{configId} ORDER BY id DESC LIMIT #{limit}")
	@ResultMap("BaseResultMap")
	List<AppConfigsHistoryEntity> findTopNLatest(@Param("configId")Integer configId,@Param("limit") int limit);
		
	@Delete("DELETE FROM app_configs_history WHERE origin_id=#{configId} AND id < #{keepMaxId}")
	@ResultType(Integer.class)
	int deleteExpireHisConfigs(@Param("configId")Integer configId,@Param("keepMaxId") Integer keepMaxId);
}