package com.jeesuite.admin.controller.admin;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jeesuite.admin.component.ConfigStateHolder;
import com.jeesuite.admin.component.ConfigStateHolder.ConfigState;
import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.AppSecretEntity;
import com.jeesuite.admin.dao.entity.AppSecretEntity.SecretType;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.entity.OperateLogEntity;
import com.jeesuite.admin.dao.entity.ProfileEntity;
import com.jeesuite.admin.dao.entity.UserEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppSecretEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.dao.mapper.OperateLogEntityMapper;
import com.jeesuite.admin.dao.mapper.ProfileEntityMapper;
import com.jeesuite.admin.dao.mapper.UserEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.SelectOption;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.model.request.AddOrEditAppRequest;
import com.jeesuite.admin.model.request.AddOrEditConfigRequest;
import com.jeesuite.admin.model.request.EncryptRequest;
import com.jeesuite.admin.model.request.QueryConfigRequest;
import com.jeesuite.admin.util.ConfigParseUtils;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.BeanCopyUtils;
import com.jeesuite.common.util.SimpleCryptUtils;

import tk.mybatis.mapper.entity.Example;

@Controller
@RequestMapping("/admin/cc")
public class ConfigCenterAdminController {

	final static Logger logger = LoggerFactory.getLogger("controller");
	
	private @Autowired AppEntityMapper appMapper;
	private @Autowired UserEntityMapper userMapper;
	private @Autowired AppconfigEntityMapper appconfigMapper;
	private @Autowired OperateLogEntityMapper operateLogMapper;
	private @Autowired AppSecretEntityMapper appSecretMapper;
	private @Autowired ProfileEntityMapper profileMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ConfigStateHolder configStateHolder;
	
	@RequestMapping(value = "apps", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> findAllApps(){
		List<AppEntity> list = appMapper.selectAll();
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getApp(@PathVariable("id") int id){
		AppEntity entity = appMapper.selectByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app/add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addApp(@RequestBody AddOrEditAppRequest addAppRequest){
		SecurityUtil.requireSuperAdmin();
		if(addAppRequest.getMasterUid() == null || addAppRequest.getMasterUid() == 0){
			throw new JeesuiteBaseException(1002, "请选择项目负责人");
		}
		Example example = new Example(AppEntity.class);
		example.createCriteria().andEqualTo("name", addAppRequest.getName());
		int count = appMapper.selectCountByExample(example);
		if(count > 0){
			throw new JeesuiteBaseException(1002, "应用["+addAppRequest.getName()+"]已存在");
		}
		AppEntity appEntity = BeanCopyUtils.copy(addAppRequest, AppEntity.class);
		//
		UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
		appEntity.setMaster(master.getName());
		appMapper.insertSelective(appEntity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app/update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateApp(@RequestBody AddOrEditAppRequest addAppRequest){
		SecurityUtil.requireSuperAdmin();
		AppEntity app = appMapper.selectByPrimaryKey(addAppRequest.getId());
		if(app == null){
			throw new JeesuiteBaseException(1002, "应用不存在");
		}
		AppEntity appEntity = BeanCopyUtils.copy(addAppRequest, AppEntity.class);
		
		if(addAppRequest.getMasterUid() != null && addAppRequest.getMasterUid() > 0 
				&& !addAppRequest.getMasterUid().equals(app.getMasterUid())){
			UserEntity master = userMapper.selectByPrimaryKey(addAppRequest.getMasterUid());
			appEntity.setMaster(master.getName());
		}
		
		appMapper.updateByPrimaryKeySelective(appEntity);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app/delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteApp(@PathVariable("id") int id){
		SecurityUtil.requireSuperAdmin();
		int delete = appMapper.deleteByPrimaryKey(id);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
	}
	
	@RequestMapping(value = "config/upload", method = RequestMethod.POST)
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
	
	@RequestMapping(value = "config/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> getConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		SecurityUtil.requireProfileGanted(entity.getEnv());
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(entity),HttpStatus.OK);
	}
	
	@RequestMapping(value = "config/add", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> addConfig(@RequestBody AddOrEditConfigRequest addRequest){
		
		SecurityUtil.requireProfileGanted(addRequest.getEnv());
		
		if(addRequest.getGlobal() == 1){
			addRequest.setAppName("global");
		}
		
		if(StringUtils.isAnyBlank(addRequest.getName(),addRequest.getEnv(),addRequest.getAppName())){
			throw new JeesuiteBaseException(4001,"请完整填写相关信息");
		}

		int count = appconfigMapper.countByQueryParams(BeanCopyUtils.beanToMap(addRequest));
		if(count > 0){
			throw new JeesuiteBaseException(1002, "配置["+addRequest.getName()+"]["+addRequest.getEnv()+"]["+addRequest.getVersion()+"]已存在");
		}
		AppconfigEntity entity = BeanCopyUtils.copy(addRequest, AppconfigEntity.class);
		appconfigMapper.insertSelective(entity);
		
		
		
		operateLogMapper.insertSelective(SecurityUtil.getOperateLog().addBizData("id", entity.getId()));
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "config/update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> updateConfig(@RequestBody AddOrEditConfigRequest addRequest){
		if(addRequest.getId() == null || addRequest.getId() == 0){
			throw new JeesuiteBaseException(1003, "id参数缺失");
		}
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(addRequest.getId());
		SecurityUtil.requireProfileGanted(entity.getEnv());
		
		OperateLogEntity operateLog = SecurityUtil.getOperateLog();
		operateLog.setBeforeData(entity.getContents());
		operateLog.setAfterData(addRequest.getContents());
		
		if(entity.getGlobal() != 1){
			if(StringUtils.isBlank(addRequest.getAppName())){
				throw new JeesuiteBaseException(1003, "请选择绑定应用");
			}else{
				operateLog.addBizData("beforeAppName", entity.getAppName()).addBizData("afterAppName", addRequest.getAppName());
				entity.setAppName(addRequest.getAppName());
			}
		}
		
		String orignContents = entity.getContents();
		entity.setContents(addRequest.getContents());
		appconfigMapper.updateByPrimaryKeySelective(entity);
		//
		publishConfigChangeEvent(orignContents,entity);
		//
		operateLogMapper.insertSelective(operateLog);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	

	@RequestMapping(value = "config/download/{id}", method = RequestMethod.GET)
	public void downConfigFile(@PathVariable("id") int id,HttpServletResponse response){
		AppconfigEntity config = appconfigMapper.selectByPrimaryKey(id);
		
		SecurityUtil.requireProfileGanted(config.getEnv());
		OutputStream output = null;
		try {
			String content = config.getContents();
			response.addHeader("Content-Disposition", "attachment;filename=" + new String(config.getName().getBytes()));
			response.addHeader("Content-Length", "" + content.length());
			output = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			byte[] bytes = content.getBytes();
			output.write(bytes);
			output.flush();
			
			operateLogMapper.insertSelective(SecurityUtil.getOperateLog().addBizData("id", config.getId()));
		} catch (Exception e) {
			throw new JeesuiteBaseException(9999, "下载失败");
		}finally {			
			if(output != null){
				try {output.close(); } catch (Exception e) {}
			}
		}
	}
	
	@RequestMapping(value = "configs", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> queryConfigs(@RequestBody QueryConfigRequest query){
		
		if(StringUtils.isNotBlank(query.getEnv())){
			SecurityUtil.requireProfileGanted(query.getEnv());
		}
		
        if(StringUtils.isBlank(query.getAppName()) && !SecurityUtil.isSuperAdmin()){
        	throw new JeesuiteBaseException(417, "请选择应用");
		}
		
		Map<String, Object> queyParams = BeanCopyUtils.beanToMap(query);
		
		if(StringUtils.isBlank(query.getEnv()) && !SecurityUtil.isSuperAdmin()){
			List<String> gantProfiles = SecurityUtil.getLoginUserInfo().getGantProfiles();
			if(gantProfiles.isEmpty()){
				return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(new ArrayList<>()),HttpStatus.OK);
			}
			queyParams.put("envs", gantProfiles);
		}
		
		List<AppconfigEntity> list = appconfigMapper.findByQueryParams(queyParams);
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app/options", method = RequestMethod.GET)
	public @ResponseBody List<SelectOption> getAppOptions(){
		List<SelectOption> result = new ArrayList<>();
		List<AppEntity> list = null;
		if(SecurityUtil.isSuperAdmin()){
			list = appMapper.selectAll();
		}else{
			list = appMapper.findByMaster(SecurityUtil.getLoginUserInfo().getId());
		}
		for (AppEntity entity : list) {
			result.add(new SelectOption(entity.getName(), entity.getAlias()));
		}
		return result;
	}
	
	@RequestMapping(value = "config/delete/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> deleteConfig(@PathVariable("id") int id){
		AppconfigEntity entity = appconfigMapper.selectByPrimaryKey(id);
		if(entity != null)SecurityUtil.requireProfileGanted(entity.getEnv());
		//全局配置
		if(entity.getGlobal() == 1)SecurityUtil.requireSuperAdmin();
		int delete = entity == null ? 0 : appconfigMapper.deleteByPrimaryKey(id);
		operateLogMapper.insertSelective(SecurityUtil.getOperateLog().addBizData("id", entity.getId()));
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(delete > 0),HttpStatus.OK);
	}
	
	@RequestMapping(value = "config/copy", method = RequestMethod.POST)
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
	
	@RequestMapping(value = "app_secret/{id}", method = RequestMethod.GET)
	public ResponseEntity<WrapperResponseEntity> appSecrets(@PathVariable("id") int id){
		
		Map<String, List<AppSecretEntity>> result = new HashMap<>();
		
		List<ProfileEntity> profiles = profileMapper.selectAll();
		for (ProfileEntity profile : profiles) {
			AppSecretEntity appSecret = cryptComponent.getAppSecret(id, profile.getName(), SecretType.DES.name());
			appSecret.setSecretKey(appSecret.getSecretKey().substring(0,28) + "****") ;
			result.put(profile.getName(), new ArrayList<>(Arrays.asList(appSecret)));
		}
		
		List<AppSecretEntity> secrets = appSecretMapper.findByAppid(id,SecretType.RSA.name());
		
		for (AppSecretEntity appSecret : secrets) {
			result.get(appSecret.getEnv()).add(appSecret);
		}
			
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(result),HttpStatus.OK);
	}
	
	@RequestMapping(value = "app_secret/update", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> appSecretupdate(@RequestBody Map<String, String> params){
		SecurityUtil.requireSuperAdmin();
		int appId = Integer.parseInt(params.get("appId"));
		String env = params.get("env");
		String secretKey = params.get("secretKey");
		String secretPass = params.get("secretPass");
		
		AppSecretEntity secretEntity = appSecretMapper.get(appId, env, SecretType.RSA.name());
		if(secretEntity == null){
			secretEntity = new AppSecretEntity();
			secretEntity.setAppId(appId);
			secretEntity.setEnv(env);
			secretEntity.setSecretType( SecretType.RSA.name());
			secretEntity.setSecretKey(secretKey);
			secretEntity.setSecretPass(SimpleCryptUtils.encrypt(secretKey, secretPass));
			appSecretMapper.insertSelective(secretEntity);
		}else{
			if(secretKey.equals(secretEntity.getSecretKey()) == false || secretPass.equals(secretEntity.getSecretPass()) == false){				
				secretEntity.setSecretKey(secretKey);
				secretEntity.setSecretPass(SimpleCryptUtils.encrypt(secretKey, secretPass));
				appSecretMapper.updateByPrimaryKey(secretEntity);
			}
		}
		
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "encrypt", method = RequestMethod.POST)
	public ResponseEntity<WrapperResponseEntity> encryptConfig(@RequestBody EncryptRequest param){
		if(StringUtils.isAnyBlank(param.getEnv(),param.getAppName(),param.getData(),param.getEncryptType())){
			throw new JeesuiteBaseException(1001, "请完整填写输入项");
		}
		SecurityUtil.requireProfileGanted(param.getEnv());
		AppEntity entity = appMapper.findByName(param.getAppName());
		
		AppSecretEntity appSecret = cryptComponent.getAppSecret(entity.getId(), param.getEnv(), param.getEncryptType());
		
		if(appSecret == null)throw new JeesuiteBaseException(1001, "无["+ param.getEncryptType()+"]密钥配置");
		String encodeStr = cryptComponent.encode(appSecret, param.getData().trim());
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(encodeStr),HttpStatus.OK);
	}
	
	
	@RequestMapping(value="active_nodes/{env}", method = RequestMethod.GET) 
	public ResponseEntity<WrapperResponseEntity> getActiveAppNodes(@PathVariable("env") String env){
		SecurityUtil.requireProfileGanted(env);
		List<ConfigState> list = configStateHolder.get(env);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value="node_config/{env}/{appName}", method = RequestMethod.GET) 
	public ResponseEntity<WrapperResponseEntity> getNodeConfig(@PathVariable("env") String env,@PathVariable("appName") String appName,@RequestParam("nodeId") String nodeId){
		SecurityUtil.requireProfileGanted(env);
		StringBuilder content = new StringBuilder();
		List<ConfigState> list = configStateHolder.get(appName, env);
		for (ConfigState configState : list) {
			if(configState.getNodeId().equals(nodeId)){
				Map<String, String> configs = configState.getConfigs();
				for (String key : configs.keySet()) {
					content.append(key).append(" = ").append(configs.get(key)).append("<br>");
				}
				break;
			}
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(content),HttpStatus.OK);
	}
	
	private void publishConfigChangeEvent(String orignContents,AppconfigEntity entity) {
		try {
			logger.info("begin publishConfigChangeEvent,{}-{}",entity.getAppName(),entity.getEnv());
			//更新后的配置
			Map<String, Object> currentConfigMap = ConfigParseUtils.parseConfigToKVMap(entity);
			
			entity.setContents(orignContents);
			Map<String, Object> orignConfigMap = ConfigParseUtils.parseConfigToKVMap(entity);
			
			List<ConfigState> configStates;
			boolean isGlobal = entity.getGlobal() == 1;
			if(isGlobal){
				configStates = configStateHolder.get(entity.getEnv());
			}else{	
				configStates = new ArrayList<>();
				String[] appNames = entity.getAppName().split(",");
				for (int i = 0; i < appNames.length; i++) {
					List<ConfigState> tmpList = configStateHolder.get(appNames[i], entity.getEnv());
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
