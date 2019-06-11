package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppEntity;

public interface AppEntityMapper extends CustomBaseMapper<AppEntity> {
	
	@Select("SELECT * FROM apps  where name=#{name}")
	@ResultMap("BaseResultMap")
	AppEntity findByName(String name);
	
	@Select("SELECT app.* FROM apps app join user_permissions up on app.id=up.app_id where up.user_id=#{userId} and up.env=#{env}")
	@ResultMap("BaseResultMap")
	List<AppEntity> findByUserPermission(@Param("userId")Integer userId,@Param("env") String env);
	
	@Select("SELECT app.* FROM apps app join user_permissions up on app.id=up.app_id where up.user_id=#{userId} and up.env=#{env} and up.operate=#{operate}")
	@ResultMap("BaseResultMap")
	List<AppEntity> findByUserPermissionWithOperate(@Param("userId")Integer userId,@Param("env") String env,@Param("operate") String operate);
}