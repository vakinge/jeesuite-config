package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.ProfileEntity;

public interface ProfileEntityMapper extends CustomBaseMapper<ProfileEntity> {
	
	@Select("SELECT * FROM profiles  where enabled=1")
	@ResultMap("BaseResultMap")
	List<ProfileEntity> findAllEnabledProfiles();
	
	@Select("SELECT * FROM profiles  where name=#{name}")
	@ResultMap("BaseResultMap")
	ProfileEntity findByName(String name);
}