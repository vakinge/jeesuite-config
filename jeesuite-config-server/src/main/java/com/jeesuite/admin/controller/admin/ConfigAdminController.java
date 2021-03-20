package com.jeesuite.admin.controller.admin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;

import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.component.ProfileZkClient;
import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.mapper.AppConfigsHistoryEntityMapper;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.PageResult;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditConfigRequest;
import com.jeesuite.admin.util.ConfigParseUtils;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.model.Page;
import com.jeesuite.common.model.PageParams;
import com.jeesuite.common.util.AssertUtil;
import com.jeesuite.common.util.BeanUtils;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.mybatis.plugin.pagination.PageExecutor;
import com.jeesuite.mybatis.plugin.pagination.PageExecutor.PageDataLoader;

@Controller
@RequestMapping("/admin/config")
public class ConfigAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private List<String> sensitiveKeys = new ArrayList<>(Arrays.asList("password","key","secret","token","credentials"));
	private static List<String> allow_upload_suffix = new ArrayList<>(Arrays.asList("xml","properties","yml","yaml"));
	private boolean sensitiveForceEncrypt = ResourceUtils.getBoolean("sensitive.config.force.encrypt",false);
	
	private @Autowired AppEntityMapper appMapper;
	private @Autowired AppconfigEntityMapper appconfigMapper;
	private @Autowired AppConfigsHistoryEntityMapper appconfigHisMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ProfileZkClient profileZkClient;

	
	@RequestMapping(value = "upload", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadConfigFile(@RequestParam("file") MultipartFile file){
		
		String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
		if(!allow_upload_suffix.contains(suffix)){
			throw new JeesuiteBaseException(9999, "支持上传文件类型:"+Arrays.toString(allow_upload_suffix.toArray()));
		}
		try {
			Map<String, String> result = new HashMap<>();
			result.put("fileName", file.getOriginalFilename());
			result.put("content", new String(file.getBytes(), StandardCharsets.UTF_8));
			return new ResponseEntity<Object>(new WrapperResponseEntity(result),HttpStatus.OK);
		} catch (Exception e) {
			throw new JeesuiteBaseException(9999, "上传失败");
		}
	}
	
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		SecurityUtil.requireAllPermission(entity.getGroupId(),entity.getAppId(),GrantOperate.RO);
		String appName = buildConfigRalateAppNames(entity);
		entity.setAppNames(appName);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<WrapperResponseEntity> addConfig(@RequestBody AddOrEditConfigRequest addRequest){
		
        if(addRequest.getGlobal()){
        	if(addRequest.getGroupId() == null) {
        		throw new JeesuiteBaseException(4001,"全局配置需要指定所属业务组");
        	}
        	SecurityUtil.requireAllPermission(addRequest.getGroupId(), null, GrantOperate.RW);
        }
        
		if(!addRequest.getGlobal() && addRequest.getAppId() == null){
			throw new JeesuiteBaseException(4001,"非全局绑定应用不能为空");
		}
		
		if(StringUtils.isBlank(addRequest.getEnv())){
			throw new JeesuiteBaseException(4001,"绑定环境profile不能为空");
		}
		
		if(addRequest.getType().intValue() == 2 && StringUtils.isBlank(addRequest.getName())){
			throw new JeesuiteBaseException(4001,"配置项名称不能空");
		}
		
		SecurityUtil.requireAllPermission(addRequest.getGroupId(),addRequest.getAppId(),GrantOperate.RW);
		
//       if(StringUtils.isNotBlank(addRequest.getName()) 
//    		   && appconfigMapper.findSameByName(addRequest.getEnv(), appId, addRequest.getName()) != null){
//    	   throw new JeesuiteBaseException(4001,"配置名称已经存在");
//       }

		AppconfigEntity entity = BeanUtils.copy(addRequest, AppconfigEntity.class);
		entity.setAppId(addRequest.getAppId());
		if(!addRequest.getGlobal()){
			Integer groupId = appMapper.selectByPrimaryKey(addRequest.getAppId()).getGroupId();
			entity.setGroupId(groupId);
		}
		entity.setCreatedBy(SecurityUtil.getLoginUserInfo().getName());
		entity.setCreatedAt(new Date());
		entity.setUpdatedAt(entity.getCreatedAt());
		entity.setUpdatedBy(entity.getCreatedBy());
		//
		encryptPropItemIfRequired(entity);
		//
		appconfigMapper.insertSelective(entity);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}

	@Transactional
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateConfig(@RequestBody AddOrEditConfigRequest addRequest){
		if(addRequest.getId() == null || addRequest.getId() == 0){
			throw new JeesuiteBaseException(1003, "id参数缺失");
		}
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(addRequest.getId());
		SecurityUtil.requireAllPermission(entity.getGroupId(),entity.getAppId(),GrantOperate.RW);
		//
		saveAppConfigHistory(entity);
		entity.setVersion(addRequest.getVersion());
		
		String orignContents = entity.getContents();
		entity.setContents(addRequest.getContents());
		//
		encryptPropItemIfRequired(entity);
		entity.setUpdatedBy(SecurityUtil.getLoginUserInfo().getName());
		entity.setUpdatedAt(new Date());
		appconfigMapper.updateByPrimaryKeySelective(entity);
		//
		publishConfigChangeEvent(orignContents,entity);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	public @ResponseBody PageResult<AppconfigEntity> queryConfigs(
			@RequestParam("pageNo") int pageNo,
    		@RequestParam("pageSize") int pageSize,
			@RequestParam(value="env",required=false)String env,
			@RequestParam(value="appId",required=false)Integer appId
			){

		Map<String, Object> queryParams = new HashMap<>();
		
		LoginUserInfo loginUserInfo = SecurityUtil.getLoginUserInfo();
		if(loginUserInfo.isGroupAdmin()){
			queryParams.put("groupId", loginUserInfo.getGroupId());
		}else if(!loginUserInfo.isSuperAdmin()){
			if(loginUserInfo.getGrantAppIds().isEmpty()){
				return new PageResult<>(pageNo, pageSize, 0L, new ArrayList<>(0));
			}
			if(appId == null)queryParams.put("appIds", loginUserInfo.getGrantAppIds());
		}
		
		if(appId != null){
			if(appId <= 0){
				queryParams.put("isGlobal", true);
			}else{				
				queryParams.put("appId", appId);
			}
		}
		if(StringUtils.isNotBlank(env)){
			queryParams.put("env", env);
		}
		
		Page<AppconfigEntity> page = PageExecutor.pagination(new PageParams(pageNo, pageSize), new PageDataLoader<AppconfigEntity>() {
			@Override
			public List<AppconfigEntity> load() {
				return appconfigMapper.findByQueryParams(queryParams);
			}
		});
		
		for (AppconfigEntity entity : page.getData()) {
			String appName = buildConfigRalateAppNames(entity);
			entity.setAppNames(appName);
		}
		
		return new PageResult<>(pageNo, pageSize, page.getTotal(), page.getData());
	}

	
	/**
	 * 配置历史记录
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "config_histories/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> queryHistoryConfigs(@PathVariable("id") int id){
		List<AppConfigsHistoryEntity> historyList = appconfigHisMapper.findTopNLatest(id, 5);
		if(!historyList.isEmpty()){
			AppconfigEntity appconfigEntity = appconfigMapper.selectByPrimaryKey(historyList.get(0).getOriginId());
			for (AppConfigsHistoryEntity entity : historyList) {
				entity.setActiveContents(appconfigEntity.getContents());
			}
			//删除历史记录
			if(historyList.size() == 5){
				try {
					Integer keepMaxId = historyList.get(4).getId();
					appconfigHisMapper.deleteExpireHisConfigs(id, keepMaxId);
				} catch (Exception e) {}
			}
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(historyList),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> deleteConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		SecurityUtil.requireAllPermission(entity.getGroupId(),entity.getAppId(),GrantOperate.RW);
		entity.setEnabled(false);
		appconfigMapper.updateByPrimaryKeySelective(entity);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	
	@RequestMapping(value = "rollback/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> rollbackConfig(@PathVariable("id") int id){
		AppConfigsHistoryEntity historyEntity = appconfigHisMapper.selectByPrimaryKey(id);
		if(historyEntity != null){
			AppconfigEntity appconfigEntity = appconfigMapper.selectByPrimaryKey(historyEntity.getOriginId());
			SecurityUtil.requireAllPermission(appconfigEntity.getGroupId(),appconfigEntity.getAppId(),GrantOperate.RW);
			appconfigEntity.setAppId(historyEntity.getAppId());
			appconfigEntity.setContents(historyEntity.getContents());
			appconfigMapper.updateByPrimaryKeySelective(appconfigEntity);
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}	
	
	@RequestMapping(value = "delete/history/{id}", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<WrapperResponseEntity> deleteHisConfig(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		AppConfigsHistoryEntity entity = appconfigHisMapper.selectByPrimaryKey(id);
		AssertUtil.notNull(entity);
		List<AppConfigsHistoryEntity> latest = appconfigHisMapper.findTopNLatest(entity.getOriginId(),1);
		if(latest.size() > 0 && latest.get(0).getId().intValue() == id){
			throw new JeesuiteBaseException(403, "最后一条备份记录禁止删除");
		}
		appconfigHisMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}

	private void publishConfigChangeEvent(String orignContents,AppconfigEntity entity) {
		try {
			if(profileZkClient.getClient(entity.getEnv()) == null){
				logger.info("skip publishConfigChangeEvent,env:{},appId:{}",entity.getEnv(),entity.getAppId());
				return;
			}
			logger.info("begin publishConfigChangeEvent,env:{},appId:{}",entity.getEnv(),entity.getAppId());
			Map<String, String> changedMap = buildChangeConfigs(entity, orignContents);
			if(changedMap.isEmpty()){
				return ;
			}
			
			List<String> appKeys;
			//通知所有应用
			if(entity.getGlobal()){
				appKeys = appMapper.findByGroupId(entity.getGroupId()).stream().map(e -> {return e.getAppKey();}).collect(Collectors.toList());
			}else{
				appKeys = Arrays.asList(appMapper.selectByPrimaryKey(entity.getAppId()).getAppKey());
			}
			
			profileZkClient.publishChangeConfig(entity.getEnv(), appKeys, changedMap);
			logger.info("finish publishConfigChangeEvent,env:{},appId:{},changedMap:{}",entity.getEnv(),entity.getAppId(),changedMap);
		} catch (Exception e) {
			logger.error("publishConfigChangeEvent error",e);
		}
	}
	
	public static Map<String, String> buildChangeConfigs(AppconfigEntity currentEntity,String orignContents){
		
		Map<String, Object> currentConfigMap = ConfigParseUtils.parseConfigToKVMap(currentEntity);
		
		currentEntity.setContents(orignContents);
		Map<String, Object> orignConfigMap = ConfigParseUtils.parseConfigToKVMap(currentEntity);
		
		Set<String> keys = currentConfigMap.keySet();
		
		Map<String, String> changedMap = new HashMap<>();
		for (String key : keys) {
			Object orignValue = orignConfigMap.get(key);
			Object currentVaule = currentConfigMap.get(key);
			if(!Objects.equals(orignValue, currentVaule)){
				changedMap.put(key, currentVaule.toString());
			}
		}
		
		return changedMap;
	}
	
	private void encryptPropItemIfRequired(AppconfigEntity entity) {
		Map<String, Object> props = ConfigParseUtils.parseConfigToKVMap(entity) ;
		
		String content = entity.getContents();
		String value;String encryptValue;
		
		boolean needCrypt = false;
		for (String key : props.keySet()) {
			value = props.get(key).toString();
			//
			needCrypt = value.startsWith(CryptComponent.cryptPrefix);
			if(!needCrypt && sensitiveForceEncrypt) {
				for (String k : sensitiveKeys) {
					if(needCrypt = key.toLowerCase().contains(k))break;
				}
			}
			if(!needCrypt)continue;
			int appId = entity.getGlobal() ? 0 : entity.getAppId();
			if(cryptComponent.isEncrpted(appId, entity.getEnv(), value))continue;
			encryptValue = cryptComponent.encrypt(appId, entity.getEnv(), value);
			content = StringUtils.replace(content, value, encryptValue);
		}
		entity.setContents(content);
	}
	
	/**
	 * @param entity
	 */
	private void saveAppConfigHistory(AppconfigEntity entity) {
		AppConfigsHistoryEntity historyEntity = new AppConfigsHistoryEntity();
		historyEntity.setOriginId(entity.getId());
		historyEntity.setName(historyEntity.getName());
		historyEntity.setEnv(entity.getEnv());
		historyEntity.setAppId(entity.getAppId());
		historyEntity.setAppNames(buildConfigRalateAppNames(entity));
		historyEntity.setType(entity.getType());
		historyEntity.setContents(entity.getContents());
		historyEntity.setVersion(entity.getVersion());
		historyEntity.setCreatedAt(new Date());
		historyEntity.setCreatedBy(SecurityUtil.getLoginUserInfo().getName());
		appconfigHisMapper.insertSelective(historyEntity);
	}
	
	private String buildConfigRalateAppNames(AppconfigEntity appconfigEntity) {
		if(appconfigEntity.getGlobal()){
			return "全局配置";
		}else{
			return appMapper.selectByPrimaryKey(appconfigEntity.getAppId()).getFullName();
		}
	}
	
}
