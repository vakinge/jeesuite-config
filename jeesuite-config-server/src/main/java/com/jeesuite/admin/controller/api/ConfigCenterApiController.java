package com.jeesuite.admin.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.component.ConfigStateHolder;
import com.jeesuite.admin.component.ConfigStateHolder.ConfigState;
import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.admin.model.Constants;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.util.ConfigParseUtils;


@Controller
@RequestMapping("/api")
public class ConfigCenterApiController {

	private @Autowired AppconfigEntityMapper appconfigMapper;
	private @Autowired AppEntityMapper appMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ConfigStateHolder configStateHolder;
	
	@RequestMapping(value = "fetch_all_configs", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> fetchConfigs(  @RequestParam(value = "appName") String appName,
			                     @RequestParam(value = "env") String env,
			                     @RequestParam(value = "version",required = false) String version){
		
		if(StringUtils.isBlank(version) || "0.0.0".equals(version))version = Constants.DEFAULT_CONFIG_VERSION;
		
		AppEntity appEntity = appMapper.findByName(appName);
		if(appEntity == null)throw new JeesuiteBaseException(1001, "app不存在");
		
		List<AppconfigEntity> globalConfigs = appconfigMapper.findGlobalConfig(env, version);
		//再查应用的
		Map<String, Object> queyParams = new HashMap<>();
		queyParams.put("env", env);
		queyParams.put("version", version);
		queyParams.put("appId", appEntity.getId());
        List<AppconfigEntity> appConfigs = appconfigMapper.findByQueryParams(queyParams);
		
		if(globalConfigs.isEmpty() &&  appConfigs.isEmpty()){
			throw new JeesuiteBaseException(1001, "配置不存在");
		}
		
		Map<String, Object> result = new HashMap<>();
		
        if(!globalConfigs.isEmpty()){
        	for (AppconfigEntity config : globalConfigs) {
            	ConfigParseUtils.parseConfigToKVMap(result, config);
    		}
        	result.put("jeesuite.configcenter.global-encrypt-secret", cryptComponent.getCryptKey(0, env));
        }
        
        for (AppconfigEntity config : appConfigs) {
        	ConfigParseUtils.parseConfigToKVMap(result, config);
		}
        result.put("jeesuite.configcenter.encrypt-secret", cryptComponent.getCryptKey(appEntity.getId(), env));
		return result;
	}
	
	@RequestMapping(value = "notify_final_config", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> notifyFinalConfig(@RequestBody Map<String, String> params){
		configStateHolder.add(new ConfigState(params));
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(true),HttpStatus.OK);
	}
	
	@RequestMapping(value = "sync_status", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> syncStatus(@RequestBody Map<String, String> params){
		String env = params.remove("env");
		String appName = params.remove("appName");
		String nodeId = params.remove("nodeId");
		
		ConfigState configState = null;
		List<ConfigState> states = configStateHolder.get(appName, env);
		for (ConfigState cs : states) {
			if(cs.getNodeId().equals(nodeId)){
				configState = cs;
				break;
			}
		}
		
		Map<String, String> result = new HashMap<>();
		if(configState != null){
			configState.update(nodeId,params);
			result.putAll(configState.getWaitingSyncConfigs());
			//TODO  应该在配置通知节点返回成功后执行
			configState.onConfigSyncSuccess();
		}
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(result),HttpStatus.OK);
	}
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public @ResponseBody String ping(){
		return "200";
	}
}
