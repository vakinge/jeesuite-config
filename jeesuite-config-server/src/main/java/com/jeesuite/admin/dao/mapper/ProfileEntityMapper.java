package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.model.KeyValuePair;

public interface ProfileEntityMapper extends CustomBaseMapper<ProfileEntity> {
	
	@Select("SELECT * FROM profiles  where enabled=1")
	@ResultMap("BaseResultMap")
	List<ProfileEntity> findAllEnabledProfiles();
	
	@Select("SELECT * FROM profiles  where name=#{name}")
	@ResultMap("BaseResultMap")
	ProfileEntity findByName(String name);
	
	@Select("SELECT attr_name as key,attr_value as value FROM profile_extr_attrs WHERE profile=#{profile}")
	@ResultType(KeyValuePair.class)
    List<KeyValuePair> findExtrAttrs(String profile);
	
	@Select("SELECT attr_value FROM profile_extr_attrs WHERE profile=#{profile} and attr_name=#{name}")
	@ResultType(String.class)
    String findExtrAttr(@Param("profile")String profile,@Param("name") String name);
}