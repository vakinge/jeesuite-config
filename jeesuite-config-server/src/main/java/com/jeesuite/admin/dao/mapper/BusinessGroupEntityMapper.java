package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.entity.BusinessGroupEntity;
import com.jeesuite.mybatis.core.BaseMapper;

public interface BusinessGroupEntityMapper extends BaseMapper<BusinessGroupEntity, Integer> {
	
	@Select("select * from business_group where enabled = 1")
	@ResultMap("BaseResultMap")
	List<BusinessGroupEntity> listAllEnabledGroup();
	
	@Select("select * from business_group where name=#{name} limit 1")
	@ResultMap("BaseResultMap")
	BusinessGroupEntity findByName(String name);
}