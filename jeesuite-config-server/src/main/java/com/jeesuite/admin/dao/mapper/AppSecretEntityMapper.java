package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppSecretEntity;

public interface AppSecretEntityMapper extends CustomBaseMapper<AppSecretEntity> {
	
	@Select("SELECT * FROM app_secret  where app_id=#{appId} and env=#{env} and secret_type=#{encryptType}")
	@ResultMap("BaseResultMap")
	AppSecretEntity get(@Param("appId") int appId,@Param("env") String env,@Param("encryptType") String encryptType);
	
	@Select("SELECT * FROM app_secret  where app_id=#{appId} and secret_type=#{encryptType}")
	@ResultMap("BaseResultMap")
	List<AppSecretEntity> findByAppid(@Param("appId") int appId,@Param("encryptType") String encryptType);
}