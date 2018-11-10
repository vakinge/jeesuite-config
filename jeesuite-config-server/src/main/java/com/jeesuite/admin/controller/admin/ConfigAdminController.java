package com.jeesuite.admin.controller.admin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jeesuite.admin.component.ConfigStateHolder;
import com.jeesuite.admin.component.ConfigStateHolder.ConfigState;
import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.AppSecretEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.entity.OperateLogEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.dao.mapper.OperateLogEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditConfigRequest;
import com.jeesuite.admin.model.request.EncryptRequest;
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
	private @Autowired OperateLogEntityMapper operateLogMapper;
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
		SecurityUtil.requireProfileGanted(entity.getEnv());
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addConfig(@RequestBody AddOrEditConfigRequest addRequest){
		
		SecurityUtil.requireProfileGanted(addRequest.getEnv());

		if(!addRequest.getGlobal() && StringUtils.isBlank(addRequest.getAppIds())){
			throw new JeesuiteBaseException(4001,"非全局绑定应用不能为空");
		}
		
		if(StringUtils.isBlank(addRequest.getEnv())){
			throw new JeesuiteBaseException(4001,"绑定环境profile不能为空");
		}
		
		if(addRequest.getType().intValue() == 2 && StringUtils.isBlank(addRequest.getName())){
			throw new JeesuiteBaseException(4001,"配置项名称不能空");
		}

		AppconfigEntity entity = BeanUtils.copy(addRequest, AppconfigEntity.class);
		appconfigMapper.insertSelective(entity);
		
		
		
		operateLogMapper.insertSelective(SecurityUtil.getOperateLog().addBizData("id", entity.getId()));
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateConfig(@RequestBody AddOrEditConfigRequest addRequest){
		if(addRequest.getId() == null || addRequest.getId() == 0){
			throw new JeesuiteBaseException(1003, "id参数缺失");
		}
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(addRequest.getId());
		SecurityUtil.requireProfileGanted(entity.getEnv());
		
		OperateLogEntity operateLog = SecurityUtil.getOperateLog();
		operateLog.setBeforeData(entity.getContents());
		operateLog.setAfterData(addRequest.getContents());
		
		if(!addRequest.getGlobal() && StringUtils.isBlank(addRequest.getAppIds())){
			throw new JeesuiteBaseException(4001,"非全局绑定应用不能为空");
		}
		
		operateLog.addBizData("beforeAppIds", entity.getAppIds()).addBizData("afterAppIds", addRequest.getAppIds());
		entity.setAppIds(addRequest.getAppIds());
		
		operateLog.addBizData("beforeVersion", entity.getVersion()).addBizData("afterVersion", addRequest.getVersion());
		entity.setVersion(addRequest.getVersion());
		
		String orignContents = entity.getContents();
		entity.setContents(addRequest.getContents());
		appconfigMapper.updateByPrimaryKeySelective(entity);
		//
		publishConfigChangeEvent(orignContents,entity);
		//
		operateLogMapper.insertSelective(operateLog);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> queryConfigs(@RequestBody QueryConfigRequest query){
		
		if(StringUtils.isNotBlank(query.getEnv())){
			SecurityUtil.requireProfileGanted(query.getEnv());
		}
		
        if(StringUtils.isBlank(query.getAppId()) && !SecurityUtil.isSuperAdmin()){
        	throw new JeesuiteBaseException(417, "请选择应用");
		}
		
		Map<String, Object> queyParams = BeanUtils.beanToMap(query);
		
		if(StringUtils.isBlank(query.getEnv()) && !SecurityUtil.isSuperAdmin()){
			List<String> gantProfiles = SecurityUtil.getLoginUserInfo().getGantProfiles();
			if(gantProfiles.isEmpty()){
				return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(new ArrayList<>()),HttpStatus.OK);
			}
			queyParams.put("envs", gantProfiles);
		}
		
		List<AppconfigEntity> list = appconfigMapper.findByQueryParams(queyParams);
		//set appName
		for (AppconfigEntity appconfigEntity : list) {
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
			appconfigEntity.setAppNames(appName);
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		if(entity != null)SecurityUtil.requireProfileGanted(entity.getEnv());
		//全局配置
		if(entity.getGlobal())SecurityUtil.requireSuperAdmin();
		int delete = entity == null ? 0 : appconfigMapper.deleteByPrimaryKey(id);
		operateLogMapper.insertSelective(SecurityUtil.getOperateLog().addBizData("id", entity.getId()));
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
	}
	
	@RequestMapping(value = "copy", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> copyConfig(@RequestBody Map<String, String> params){
		String from = params.get("from");
		SecurityUtil.requireProfileGanted(from);
		String to = params.get("to");
		SecurityUtil.requireProfileGanted(to);
		
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
	
	
	@RequestMapping(value = "encrypt", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> encryptConfig(@RequestBody EncryptRequest param){
		if(StringUtils.isAnyBlank(param.getEnv(),param.getData(),param.getEncryptType()) || param.getAppId() <= 0){
			throw new JeesuiteBaseException(1001, "请完整填写输入项");
		}
		SecurityUtil.requireProfileGanted(param.getEnv());
		AppEntity entity = appMapper.selectByPrimaryKey(param.getAppId());
		
		AppSecretEntity appSecret = cryptComponent.getAppSecret(entity.getId(), param.getEnv(), param.getEncryptType());
		
		if(appSecret == null)throw new JeesuiteBaseException(1001, "无["+ param.getEncryptType()+"]密钥配置");
		String encodeStr = cryptComponent.encode(appSecret, param.getData().trim());
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(encodeStr),HttpStatus.OK);
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
	
}
