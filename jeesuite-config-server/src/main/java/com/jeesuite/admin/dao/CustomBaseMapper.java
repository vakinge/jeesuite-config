package com.jeesuite.admin.dao;

import com.jeesuite.mybatis.core.BaseEntity;
import com.jeesuite.mybatis.core.BaseMapper;

public interface CustomBaseMapper<T extends BaseEntity> extends BaseMapper<T,Integer> {

}
