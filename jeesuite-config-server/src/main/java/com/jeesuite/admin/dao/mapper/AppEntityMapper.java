package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppEntity;

public interface AppEntityMapper extends CustomBaseMapper<AppEntity> {
	
	@Select("SELECT * FROM apps  where name=#{name}")
	@ResultMap("BaseResultMap")
	AppEntity findByName(String name);
	
	@Select("SELECT * FROM apps  where master_uid=#{masterUid}")
	@ResultMap("BaseResultMap")
	List<AppEntity> findByMaster(int masterUid);
}