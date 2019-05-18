package com.jeesuite.admin.controller.admin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.springframework.web.multipart.MultipartFile;

import com.jeesuite.admin.component.ConfigStateHolder;
import com.jeesuite.admin.component.ConfigStateHolder.ConfigState;
import com.jeesuite.admin.constants.GrantOperate;
import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.mapper.AppConfigsHistoryEntityMapper;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditConfigRequest;
import com.jeesuite.admin.model.request.QueryConfigRequest;
import com.jeesuite.admin.util.ConfigParseUtils;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.BeanUtils;

import tk.mybatis.mapper.entity.Example;

@Controller
@RequestMapping("/admin/config")
public class ConfigAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private @Autowired AppEntityMapper appMapper;
	private @Autowired AppconfigEntityMapper appconfigMapper;
	private @Autowired AppConfigsHistoryEntityMapper appconfigHisMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ConfigStateHolder configStateHolder;
	

	
	@RequestMapping(value = "upload", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadConfigFile(@RequestParam("file") MultipartFile file){
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
		SecurityUtil.requireAnyPermission(entity.getEnv(), entity.getAppIds(),GrantOperate.RO);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addConfig(@RequestBody AddOrEditConfigRequest addRequest){
		
		if(!addRequest.getGlobal() && StringUtils.isBlank(addRequest.getAppIds())){
			throw new JeesuiteBaseException(4001,"非全局绑定应用不能为空");
		}
		
		if(StringUtils.isBlank(addRequest.getEnv())){
			throw new JeesuiteBaseException(4001,"绑定环境profile不能为空");
		}
		
		if(addRequest.getType().intValue() == 2 && StringUtils.isBlank(addRequest.getName())){
			throw new JeesuiteBaseException(4001,"配置项名称不能空");
		}
		
		SecurityUtil.requireAllPermission(addRequest.getEnv(),addRequest.getGlobal() ? "0" : addRequest.getAppIds(),GrantOperate.RW);

		AppconfigEntity entity = BeanUtils.copy(addRequest, AppconfigEntity.class);
		//
		appconfigMapper.insertSelective(entity);
		
		if(encryptPropItemIfRequired(entity)){
			appconfigMapper.updateByPrimaryKey(entity);
		}
		
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateConfig(@RequestBody AddOrEditConfigRequest addRequest){
		if(addRequest.getId() == null || addRequest.getId() == 0){
			throw new JeesuiteBaseException(1003, "id参数缺失");
		}
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(addRequest.getId());
		SecurityUtil.requireAllPermission(entity.getEnv(),entity.getGlobal() ? "0" : entity.getAppIds(),GrantOperate.RW);
		if(!addRequest.getGlobal() && StringUtils.isBlank(addRequest.getAppIds())){
			throw new JeesuiteBaseException(4001,"非全局绑定应用不能为空");
		}
		//
		saveAppConfigHistory(entity);
		
		entity.setAppIds(addRequest.getAppIds());
		entity.setVersion(addRequest.getVersion());
		
		String orignContents = entity.getContents();
		entity.setContents(addRequest.getContents());
		//
		encryptPropItemIfRequired(entity);
		appconfigMapper.updateByPrimaryKeySelective(entity);
		//
		publishConfigChangeEvent(orignContents,entity);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> queryConfigs(@RequestBody QueryConfigRequest query){
		
		if(StringUtils.isNotBlank(query.getEnv())){
			SecurityUtil.requireAllPermission(query.getEnv(),GrantOperate.RO);
		}
		
        if(StringUtils.isBlank(query.getAppId()) && !SecurityUtil.isSuperAdmin()){
        	throw new JeesuiteBaseException(417, "请选择应用");
		}
		
		Map<String, Object> queyParams = BeanUtils.beanToMap(query);
		
		if(StringUtils.isBlank(query.getEnv()) && !SecurityUtil.isSuperAdmin()){
			List<String> gantProfiles = SecurityUtil.getLoginUserInfo().getGrantedProfiles();
			if(gantProfiles.isEmpty()){
				return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(new ArrayList<>()),HttpStatus.OK);
			}
			queyParams.put("envs", gantProfiles);
		}
		
		List<AppconfigEntity> list = appconfigMapper.findByQueryParams(queyParams);
		//set appName
		for (AppconfigEntity appconfigEntity : list) {
			String appName = buildConfigRalateAppNames(appconfigEntity);
			appconfigEntity.setAppNames(appName);
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}

	
	/**
	 * 配置历史记录
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "config_histories/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> queryHistoryConfigs(@PathVariable("id") int id){
		List<AppConfigsHistoryEntity> historyList = appconfigHisMapper.findByConfigId(id);
		if(!historyList.isEmpty()){
			AppconfigEntity appconfigEntity = appconfigMapper.selectByPrimaryKey(historyList.get(0).getOriginId());
			for (AppConfigsHistoryEntity entity : historyList) {
				entity.setActiveContents(appconfigEntity.getContents());
			}
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(historyList),HttpStatus.OK);
	}
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	@Transactional
	public ResponseEntity<WrapperResponseEntity> deleteConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		//全局配置
		if(entity.getGlobal())SecurityUtil.requireSuperAdmin();
		//		
		SecurityUtil.requireAllPermission(entity.getEnv(),entity.getAppIds(),GrantOperate.RW);
		int delete = entity == null ? 0 : appconfigMapper.deleteByPrimaryKey(id);
		//
		saveAppConfigHistory(entity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
	}
	
	@RequestMapping(value = "rollback/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> rollbackConfig(@PathVariable("id") int id){
		AppConfigsHistoryEntity historyEntity = appconfigHisMapper.selectByPrimaryKey(id);
		if(historyEntity != null){
			AppconfigEntity appconfigEntity = appconfigMapper.selectByPrimaryKey(historyEntity.getOriginId());
			appconfigEntity.setAppIds(historyEntity.getAppIds());
			appconfigEntity.setContents(historyEntity.getContents());
			appconfigMapper.updateByPrimaryKeySelective(appconfigEntity);
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(),HttpStatus.OK);
	}
	

	@RequestMapping(value = "copy", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> copyConfig(@RequestBody Map<String, String> params){
		String from = params.get("from");
		String to = params.get("to");
		
		Example example = new Example(AppconfigEntity.class);
		example.createCriteria().andEqualTo("env", from);
		List<AppconfigEntity> configList = appconfigMapper.selectByExample(example);
		if(configList == null || configList.isEmpty()){
			throw new JeesuiteBaseException(1001, "Profile["+from+"]无任何配置");
		}
		
		for (AppconfigEntity entity : configList) {
			entity.setId(null);
			entity.setEnv(to);
			appconfigMapper.insertSelective(entity);
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}	
	
	private void publishConfigChangeEvent(String orignContents,AppconfigEntity entity) {
		try {
			logger.info("begin publishConfigChangeEvent,{}-{}",entity.getAppIds(),entity.getEnv());
			//更新后的配置
			Map<String, Object> currentConfigMap = ConfigParseUtils.parseConfigToKVMap(entity);
			
			entity.setContents(orignContents);
			Map<String, Object> orignConfigMap = ConfigParseUtils.parseConfigToKVMap(entity);
			
			List<ConfigState> configStates;
			if(entity.getGlobal()){
				configStates = configStateHolder.get(entity.getEnv());
			}else{	
				configStates = new ArrayList<>();
				String[] appIds = entity.getAppIds().split(",");
				for (int i = 0; i < appIds.length; i++) {
					AppEntity appEntity = appMapper.selectByPrimaryKey(Integer.parseInt(appIds[i]));
					List<ConfigState> tmpList = configStateHolder.get(appEntity.getName(), entity.getEnv());
					if(tmpList != null && !tmpList.isEmpty())configStates.addAll(tmpList);
				}
			}
			
			if(configStates.isEmpty())return;
			
			//zookeeper 通知同一环境+app只需要通知一次，所以这里保存一个通知状态
			Set<String> publishList = new HashSet<>();
			for (ConfigState configState : configStates) {
				Set<String> keys = currentConfigMap.keySet();
				
				Map<String, String> changedMap = new HashMap<>();
				for (String key : keys) {
					Object orignValue = orignConfigMap.get(key);
					Object currentVaule = currentConfigMap.get(key);
					if(!Objects.equals(orignValue, currentVaule)){
						changedMap.put(key, currentVaule.toString());
					}
				}
				String publishedKey = configState.getEnv() + ":" + configState.getAppName();
				if(ConfigStateHolder.SYNC_TYPE_ZK.equals(configState.getSyncType()) == false 
						|| publishList.contains(publishedKey) == false){				
					configState.publishChangeConfig(changedMap);
					publishList.add(publishedKey);
					logger.info("publishConfigChangeEvent,env:{},appName:{},changeConfig:{}",configState.getEnv(),configState.getAppName(),changedMap);
				}
			}
		} catch (Exception e) {
			logger.error("publishConfigChangeEvent error",e);
		}
	}
	
	private boolean encryptPropItemIfRequired(AppconfigEntity entity) {
		Map<String, Object> props = ConfigParseUtils.parseConfigToKVMap(entity) ;
		
		String content = entity.getContents();
		String value;String encryptValue;
		
		boolean needCrypt = false;
		for (String key : props.keySet()) {
			value = props.get(key).toString();
			if(!value.startsWith(CryptComponent.cryptPrefix))continue;
			if(cryptComponent.isEncrpted(entity.getGlobal() ? 0 : entity.getId(), entity.getEnv(), value))continue;
			encryptValue = cryptComponent.encrypt(entity.getGlobal() ? 0 : entity.getId(), entity.getEnv(), value);
			content = StringUtils.replace(content, value, encryptValue);
			needCrypt = true;
		}
		entity.setContents(content);
		
		return needCrypt;
	}
	
	/**
	 * @param entity
	 */
	private void saveAppConfigHistory(AppconfigEntity entity) {
		AppConfigsHistoryEntity historyEntity = new AppConfigsHistoryEntity();
		historyEntity.setOriginId(entity.getId());
		historyEntity.setName(historyEntity.getName());
		historyEntity.setEnv(entity.getEnv());
		historyEntity.setAppIds(entity.getAppIds());
		historyEntity.setAppNames(buildConfigRalateAppNames(entity));
		historyEntity.setType(entity.getType());
		historyEntity.setContents(entity.getContents());
		historyEntity.setVersion(entity.getVersion());
		historyEntity.setCreatedAt(new Date());
		historyEntity.setCreatedBy(SecurityUtil.getLoginUserInfo().getName());
		appconfigHisMapper.insertSelective(historyEntity);
	}
	
	private String buildConfigRalateAppNames(AppconfigEntity appconfigEntity) {
		String appName = "";
		if(StringUtils.isBlank(appconfigEntity.getAppIds())){
			appName = "全局配置";
		}else{
			String[] appIds = appconfigEntity.getAppIds().split(",");
			for (int i = 0; i < appIds.length; i++) {
				AppEntity appEntity = appMapper.selectByPrimaryKey(Integer.parseInt(appIds[i]));
				appName = appName + appEntity.getAlias() + (i < appIds.length - 1 ? "," : "" );
			}
		}
		return appName;
	}
	
}
