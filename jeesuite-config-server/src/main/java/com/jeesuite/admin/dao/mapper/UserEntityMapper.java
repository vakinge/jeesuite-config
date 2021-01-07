package com.jeesuite.admin.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jeesuite.admin.dao.CustomBaseMapper;
import com.jeesuite.admin.dao.entity.UserEntity;

public interface UserEntityMapper extends CustomBaseMapper<UserEntity> {
	
	@Select("SELECT * FROM users  where name=#{name}")
	@ResultMap("BaseResultMap")
	UserEntity findByName(String name);
	
	@Select("SELECT * FROM users  where mobile=#{mobile}")
	@ResultMap("BaseResultMap")
	UserEntity findByMobile(String mobile);
	
	@Select("SELECT * FROM users  where email=#{email}")
	@ResultMap("BaseResultMap")
	UserEntity findByEmail(String email);
	
	
	@Select("SELECT * FROM users  where name <> 'admin'")
	@ResultMap("BaseResultMap")
	List<UserEntity> findAllUser();
	
	@Select("SELECT * FROM users  where group_id=#{groupId}")
	@ResultMap("BaseResultMap")
	List<UserEntity> findByGroupId(Integer groupId);
	
	@Update("UPDATE users SET type = #{type} WHERE id = #{userId} AND type <> 1")
	@ResultType(Integer.class)
	int updateUserType(@Param("userId") Integer userId,@Param("type") String type);
}