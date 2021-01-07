package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jeesuite.admin.dao.entity.BusinessGroupEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.BusinessGroupEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.AssertUtil;


@RestController
@RequestMapping("/admin/group")
public class BusinessGroupController {

    private @Autowired BusinessGroupEntityMapper businessGroupMapper;
    private @Autowired UserEntityMapper userMapper;
    
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
	public WrapperResponseEntity list(){
    	List<BusinessGroupEntity> list = businessGroupMapper.selectAll();
    	return new WrapperResponseEntity(list);
    }
    
    @RequestMapping(value = "get/{id}", method = RequestMethod.GET)
    @ResponseBody
	public WrapperResponseEntity getById(@PathVariable("id") int id){
    	BusinessGroupEntity entity = businessGroupMapper.selectByPrimaryKey(id);
		return new WrapperResponseEntity(entity);
	}
    
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
	public WrapperResponseEntity add(@RequestBody BusinessGroupEntity param){
    	AssertUtil.notNull(param.getMasterUid(),"组管理员不能为空");
    	AssertUtil.notBlank(param.getName());
    	SecurityUtil.requireSuperAdmin();
    	AssertUtil.isNull(businessGroupMapper.findByName(param.getName()),"组名已存在");
    	
    	param.setCreatedAt(new Date());
    	param.setCreatedBy(SecurityUtil.getLoginUserInfo().getName());
    	businessGroupMapper.insertSelective(param);
    	return new WrapperResponseEntity();
    }
    
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @ResponseBody
	public WrapperResponseEntity update(@RequestBody BusinessGroupEntity param){
    	SecurityUtil.requireSuperAdmin();
    	AssertUtil.notNull(param.getMasterUid());
    	BusinessGroupEntity groupEntity = businessGroupMapper.selectByPrimaryKey(param.getId());
    	if(!param.getMasterUid().equals(groupEntity.getMasterUid())){
    		groupEntity.setMasterUid(param.getMasterUid());
    		UserEntity userEntity = userMapper.selectByPrimaryKey(param.getMasterUid());
    		groupEntity.setMaster(userEntity.getName());
    	}
    	groupEntity.setName(param.getName());
    	groupEntity.setRemarks(param.getRemarks());
    	groupEntity.setUpdatedBy(SecurityUtil.getLoginUserInfo().getName());
    	groupEntity.setUpdatedAt(new Date());
    	businessGroupMapper.updateByPrimaryKeySelective(groupEntity);
    	return new WrapperResponseEntity();
    }

    @RequestMapping("/options")
    public List<SelectOption> getBusinessGroupOptions() {
        List<BusinessGroupEntity> groupEntities = businessGroupMapper.listAllEnabledGroup();
        if(CollectionUtils.isEmpty(groupEntities)) {
            return new ArrayList<>(0);
        } else {
            return groupEntities.stream()
                    .map(entity -> new SelectOption(entity.getId().toString(),entity.getName()))
                    .collect(Collectors.toList());
        }
    }
}
