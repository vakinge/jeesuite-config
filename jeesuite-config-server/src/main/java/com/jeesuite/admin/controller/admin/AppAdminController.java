package com.jeesuite.admin.controller.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.component.EventPublishClient;
import com.jeesuite.admin.constants.AppExtrAttrName;
import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.constants.UserType;
import com.jeesuite.admin.dao.entity.ApplicationEntity;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.dao.mapper.ApplicationEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.model.AppTreeModel;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.PageResult;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditAppRequest;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.model.IdParam;
import com.jeesuite.common.model.KeyValuePair;
import com.jeesuite.common.model.Page;
import com.jeesuite.common.model.PageParams;
import com.jeesuite.common.util.AssertUtil;
import com.jeesuite.common.util.BeanUtils;
import com.jeesuite.mybatis.plugin.pagination.PageExecutor;
import com.jeesuite.springweb.model.WrapperResponse;

@Controller
@RequestMapping("/admin/app")
public class AppAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private @Autowired ApplicationEntityMapper appMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired UserEntityMapper userMapper;
	private @Autowired AppconfigEntityMapper appconfigMapper;
	
	//所有环境统一TOKEN
	@Value("${same-app-token.strategy.enabled:false}")
	private boolean sameAppTokenStrategy;
	

	@RequestMapping(value = "list",method = RequestMethod.POST)
	public @ResponseBody PageResult<ApplicationEntity> pageQuery(
			@RequestParam(value="appType",required = false) String appType,
		    @RequestParam(value = "pageNo")Integer pageNo,
		    @RequestParam(value = "pageSize")Integer pageSize){
		Map<String, Object> queryParams = new HashMap<>();

		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();

		if(loginUserInfo.getUserType().equals(UserType.user.name())) {
			queryParams.put("enabled", 1);
			queryParams.put("userId", loginUserInfo.getId());
		} else if(loginUserInfo.getUserType().equals(UserType.groupAdmin.name())) {
			queryParams.put("groupId",loginUserInfo.getGroupId());
		}
		if(appType != null){
			queryParams.put("appType", appType);
		}

		Page<ApplicationEntity> page = PageExecutor.pagination(new PageParams(pageNo, pageSize), new PageExecutor.PageDataLoader<ApplicationEntity>() {
			@Override
			public List<ApplicationEntity> load() {
				return appMapper.findByQueryParams(queryParams);
			}
		});
		return new PageResult<>(pageNo, pageSize, page.getTotal(), page.getData());
	}
	
	@RequestMapping(value = "tree", method = RequestMethod.GET)
	public @ResponseBody WrapperResponse<Collection<AppTreeModel>> findAppTree(){
		Map<String, Object> queryParams = new HashMap<>();
		List<ApplicationEntity> apps = appMapper.findByQueryParams(queryParams);
		Map<String, AppTreeModel> map = new LinkedHashMap<>();
		
		AppTreeModel model;
		for (ApplicationEntity app : apps) {
			model = new AppTreeModel(app.getId().toString(), app.getName(), app.getCode());
			model.setServiceId(app.getServiceId());
			if(app.getParentId() == null) {
				map.put(app.getId().toString(), model);
			}else {
				map.get(app.getParentId().toString()).addChild(model);
			}
		}
		
		return new WrapperResponse<>(map.values());
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getApp(@PathVariable("id") int id){
		ApplicationEntity entity = appMapper.selectByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<WrapperResponseEntity> addApp(@RequestBody AddOrEditAppRequest addAppRequest){
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		//
		if(!loginUserInfo.isSuperAdmin() || addAppRequest.getOwnerId() == null || addAppRequest.getOwnerId() == 0){
			addAppRequest.setOwnerId(Integer.parseInt(loginUserInfo.getId()));
		}

		if(!loginUserInfo.isSuperAdmin() && addAppRequest.getGroupId() == null){
			addAppRequest.setGroupId(loginUserInfo.getGroupId());
		}
		
		if(addAppRequest.getParentId() == null && addAppRequest.getGroupId() == null){
			throw new JeesuiteBaseException(1002, "业务组必填");
		}

		ApplicationEntity appEntity = BeanUtils.copy(addAppRequest, ApplicationEntity.class);
		//
		UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getOwnerId());
		appEntity.setOwnerId(master.getId());
		appEntity.setOwnerName(master.getName());
		appEntity.setCreatedAt(new Date());
		appEntity.setCreatedBy(SecurityUtil.getLoginUserInfo().getUsername());
		//
		if(appEntity.getParentId() != null) {
			ApplicationEntity parent = appMapper.selectByPrimaryKey(appEntity.getParentId());
			appEntity.setGroupId(parent.getGroupId());
			appEntity.setIsModule(true);
			appEntity.setCode(parent.getCode() + "-" + appEntity.getCode());
		}
		
		if(appMapper.findByAppKey(appEntity.getCode()) != null){
			throw new JeesuiteBaseException(1002, "应用["+addAppRequest.getCode()+"]已存在");
		}
		
		appMapper.insertSelective(appEntity);
		//
		SecurityUtil.reloadPermssionDatas();
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateApp(@RequestBody AddOrEditAppRequest addAppRequest){
		ApplicationEntity app = appMapper.selectByPrimaryKey(addAppRequest.getId());
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		SecurityUtil.requireAllPermission(app.getGroupId(), app.getId(), GrantOperate.RW);

		ApplicationEntity appEntity = BeanUtils.copy(addAppRequest, ApplicationEntity.class);
		
		if(addAppRequest.getOwnerId() != null && addAppRequest.getOwnerId() > 0 
				&& !addAppRequest.getOwnerId().equals(app.getOwnerId())){
			UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getOwnerId());
			appEntity.setOwnerName(master.getName());
		}
		
		appEntity.setUpdatedBy(SecurityUtil.getLoginUserInfo().getUsername());
		appEntity.setUpdatedAt(new Date());
		appMapper.updateByPrimaryKeySelective(appEntity);
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteApp(@PathVariable("id") Integer id){
		
		SecurityUtil.requireSuperAdmin();
		//
		ApplicationEntity app = appMapper.selectByPrimaryKey(id);
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		if(app.getParentId() == null && !app.getIsModule()) {
			ApplicationEntity example = new ApplicationEntity();
			example.setParentId(app.getId());
			example.setEnabled(true);
			if(appMapper.countByExample(example) > 0) {
				throw new JeesuiteBaseException(1002, "该应用下包含启用状态模块");
			}
		}
		//
		app.setEnabled(false);
		app.setUpdatedBy(SecurityUtil.getLoginUserInfo().getUsername());
		app.setUpdatedAt(new Date());
		appMapper.updateByPrimaryKey(app);
		//删除关联的配置
		appconfigMapper.deleteByAppId(id);
		
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "toggle", method = RequestMethod.POST)
	public @ResponseBody WrapperResponseEntity enableSwitch(@RequestBody IdParam<Integer> param){
        ApplicationEntity entity = appMapper.selectByPrimaryKey(param.getId());
		AssertUtil.notNull(entity);
		SecurityUtil.requireAllPermission(entity.getGroupId(), entity.getId(), GrantOperate.RW);
		entity.setEnabled(!entity.getEnabled());
		//发送事件
		EventPublishClient.publishAllEnvs("configChanged", "app", null);
		
		appMapper.updateByPrimaryKeySelective(entity);
		
		return new WrapperResponseEntity();
	}
	
	@RequestMapping(value = "options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getAppOptions(@RequestParam(value="type",required = false) String type
			,@RequestParam(value="format",required = false) String format){
		List<ApplicationEntity> list = findAppByCurrentUserAndParams("app".equals(type));
		return list.stream().map(e -> {
			String value = "code".equals(format) ? e.getCode() : String.valueOf(e.getId());
			return new SelectOption(value, e.getFullName());
		}).collect(Collectors.toList());
	}
	
	
	@RequestMapping(value = "extrAttrs", method = RequestMethod.GET)
	public @ResponseBody WrapperResponseEntity getExtrAttrs(@RequestParam int appId,@RequestParam String name){
		List<KeyValuePair> attrs = new ArrayList<>();
		List<ProfileEntity> profiles = profileMapper.findAllEnabledProfiles();
		for (ProfileEntity profile : profiles) {
			String extrAttr = appMapper.findExtrAttr(appId, profile.getName(), name);
			attrs.add(new KeyValuePair(profile.getName(), StringUtils.trimToEmpty(extrAttr)));
		}
		return new WrapperResponseEntity(attrs);
	}
	
	@RequestMapping(value = "tokens", method = RequestMethod.GET)
	public @ResponseBody WrapperResponseEntity getAppTokens(@RequestParam int appId){
		List<KeyValuePair> list = appMapper.findProfileExtrAttrs(appId, AppExtrAttrName.API_TOKEN.name());
		if(list.isEmpty()) {
			List<ProfileEntity> profiles = profileMapper.selectAll();
			
			list = new ArrayList<>(profiles.size());
			String token = StringUtils.remove(UUID.randomUUID().toString(), "-");
			for (ProfileEntity profile : profiles) {
				token = sameAppTokenStrategy ? token : StringUtils.remove(UUID.randomUUID().toString(), "-");
				appMapper.insertAttr(appId, profile.getName(), AppExtrAttrName.API_TOKEN.name(), token);
				list.add(new KeyValuePair(profile.getName(), token));
			}
		}
		return new WrapperResponseEntity(list);
	}

	
	private List<ApplicationEntity> findAppByCurrentUserAndParams(boolean onlyParentApp){
		
		Map<String, Object> param = new HashMap<String, Object>(4);
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(loginUserInfo.isGroupAdmin()){
			param.put("groupId", loginUserInfo.getGroupId());
		}else if(!loginUserInfo.isSuperAdmin()){
			param.put("userId", loginUserInfo.getId());
		}
		if(onlyParentApp) {
			param.put("onlyParentApp", onlyParentApp);
		}
		
		return appMapper.findByQueryParams(param);
	}
}
