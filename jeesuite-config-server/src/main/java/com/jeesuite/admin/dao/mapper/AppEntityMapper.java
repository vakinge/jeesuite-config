package com.jeesuite.admin.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.common.model.KeyValuePair;
import com.jeesuite.mybatis.plugin.cache.annotation.Cache;

public interface AppEntityMapper extends CustomBaseMapper<AppEntity> {
	
	@Cache(uniqueIndex = true)
	@Select("SELECT * FROM apps  where app_key=#{appKey} limit 1")
	@ResultMap("BaseResultMap")
	AppEntity findByAppKey(String appKey);
	
	@Select("SELECT * FROM apps  where service_id=#{serviceId} limit 1")
	@ResultMap("BaseResultMap")
	AppEntity findByServiceId(String serviceId);
	
	@Select("SELECT * FROM apps  where master_uid=#{masterUid} AND enabled = 1")
	@ResultMap("BaseResultMap")
	List<AppEntity> findByMaster(Integer masterUid);

	@Select("SELECT env as `key`,attr_value as `value` FROM app_extr_attrs WHERE app_id=#{appId} and attr_name=#{attrName}")
	@ResultType(KeyValuePair.class)
    List<KeyValuePair> findProfileExtrAttrs(@Param("appId") int appId,@Param("attrName") String attrName);
	
	@Select("SELECT attr_value FROM app_extr_attrs WHERE app_id=#{appId} and env=#{env} and attr_name=#{attrName} limit 1")
	@ResultType(String.class)
    String findExtrAttr(@Param("appId") int appId,@Param("env") String env,@Param("attrName") String attrName);
	
	@Insert("insert into app_extr_attrs ( attr_value, env, app_id, attr_name) values ( #{attrValue}, #{env}, #{appId}, #{attrName})")
	@ResultType(Integer.class)
	Integer insertAttr(@Param("appId") int appId,@Param("env") String env,@Param("attrName") String attrName,@Param("attrValue") String attrValue);
	
	@Insert("update app_extr_attrs set attr_value=#{attrValue} where app_id=#{appId} and attr_name=#{attrName} and env=#{env}")
	@ResultType(Integer.class)
	Integer updateAttr(@Param("appId") int appId,@Param("env") String env,@Param("attrName") String attrName,@Param("attrValue") String attrValue);

	List<AppEntity> findByQueryParams(Map<String, Object> param);

	@Select("SELECT * FROM apps  where group_id=#{groupId} AND enabled = 1")
	@ResultMap("BaseResultMap")
	List<AppEntity> findByGroupId(Integer groupId);

}