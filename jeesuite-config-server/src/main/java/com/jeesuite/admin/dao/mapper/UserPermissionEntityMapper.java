package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.UserPermissionEntity;

public interface UserPermissionEntityMapper extends CustomBaseMapper<UserPermissionEntity> {
	
	@Select("SELECT * FROM user_permissions  where user_id=#{userId}")
	@ResultMap("BaseResultMap")
	List<UserPermissionEntity> findByUserId(Integer userId);
	
	@Select("SELECT * FROM user_permissions  where user_id=#{userId} and env=#{env}")
	@ResultMap("BaseResultMap")
	List<UserPermissionEntity> findByUserIdAndEnv(@Param("userId") Integer userId,@Param("env") String env);
	
	List<UserPermissionEntity> findByQueryParams(Map<String, Object> param);
}