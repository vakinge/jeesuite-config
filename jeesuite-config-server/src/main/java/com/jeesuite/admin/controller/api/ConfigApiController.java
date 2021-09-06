package com.jeesuite.admin.controller.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

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

import com.jeesuite.admin.annotation.ValidateSign;
import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.component.ProfileZkClient;
import com.jeesuite.admin.controller.admin.ConfigAdminController;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;
import com.jeesuite.admin.dao.entity.ApplicationEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.mapper.AppConfigsHistoryEntityMapper;
import com.jeesuite.admin.dao.mapper.ApplicationEntityMapper;
import com.jeesuite.admin.dao.mapper.AppconfigEntityMapper;
import com.jeesuite.admin.model.Constants;
import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.admin.util.ConfigParseUtils;
import com.jeesuite.common.JeesuiteBaseException;

@Controller
@RequestMapping("/api")
public class ConfigApiController {

	private static final String LATEST_FETCH_TIMESTAMP = "latest.fetch.timestamp";
	private @Autowired AppconfigEntityMapper appconfigMapper;
	private @Autowired ApplicationEntityMapper appMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ProfileZkClient profileZkClient;
	private @Autowired AppConfigsHistoryEntityMapper appconfigHisMapper;

	@ValidateSign
	@RequestMapping(value = "fetch_all_configs", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> fetchConfigs(HttpServletRequest request,
			@RequestParam(value = "appName") String appName, @RequestParam(value = "env") String env,
			@RequestParam(value = "version", required = false) String version,
			@RequestParam(value = "ignoreGlobal", required = false,defaultValue="false") boolean ignoreGlobal) {

		if (StringUtils.isBlank(version)){
			version = Constants.DEFAULT_CONFIG_VERSION;
		}

		ApplicationEntity appEntity = appMapper.findByAppKey(appName);
        //全局的
		List<AppconfigEntity> configs = ignoreGlobal ? new  ArrayList<>() : appconfigMapper.findGlobalConfig(env,appEntity.getGroupId(), version);
		// 再查应用的
		Map<String, Object> queyParams = new HashMap<>();
		queyParams.put("env", env);
		queyParams.put("version", version);
		//
		if(appEntity.getParentId() != null) {
			queyParams.put("appId", appEntity.getParentId());
			configs.addAll(appconfigMapper.findByQueryParams(queyParams));
		}
		//
		queyParams.put("appId", appEntity.getId());
		configs.addAll(appconfigMapper.findByQueryParams(queyParams));

		if (configs.isEmpty()) {
			throw new JeesuiteBaseException(1001, "配置不存在");
		}

		Map<String, Object> result = new HashMap<>();
		for (AppconfigEntity config : configs) {
			ConfigParseUtils.parseConfigToKVMap(result, config);
		}
		//
		String cryptKey = cryptComponent.getCryptKey(0, env);
		result.put("jeesuite.configcenter.global-encrypt-secret", cryptKey);
		cryptKey = cryptComponent.getCryptKey(appEntity.getId(), env);
		result.put("jeesuite.configcenter.encrypt-secret", cryptKey);
		//
		String zkServers = profileZkClient.getZkServer(env);
		if (zkServers != null) {
			result.put("jeesuite.configcenter.sync-zk-servers", zkServers);
			result.put("global.event.zk-servers", zkServers);
		}
		result.put(LATEST_FETCH_TIMESTAMP, System.currentTimeMillis());
		
		return result;
	}


	@RequestMapping(value = "fetch_changed_configs", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<WrapperResponseEntity> fetchChangedConfigs(HttpServletRequest request,@RequestBody Map<String, Object> params) {
		String version = Objects.toString(params.remove("version"), null);
		Date lastFetchTime = new Date(Long.parseLong(params.remove("lastTime").toString()));
		boolean ignoreGlobal = params.containsKey("ignoreGlobal") && Boolean.parseBoolean(params.remove("ignoreGlobal").toString());
		boolean fetchAll = params.containsKey("fetchAll") && Boolean.parseBoolean(params.remove("fetchAll").toString());
		
		if (StringUtils.isBlank(version)){
			version = Constants.DEFAULT_CONFIG_VERSION;
		}
		ApplicationEntity appEntity = appMapper.findByAppKey(params.remove("appName").toString());
		//
		params.put("groupId", appEntity.getGroupId());
		params.put("lastUpdateTime", lastFetchTime);
		
		List<AppconfigEntity> configs;
		if(ignoreGlobal){
			configs = new ArrayList<>(1);
		}else{
			params.put("isGlobal", true);
			params.put("version", Objects.toString(params.remove("globalVersion"), version));
			configs = appconfigMapper.findByQueryParams(params);
		}
		
		params.remove("groupId");
		params.remove("isGlobal");
		params.put("appId", appEntity.getId());
		params.put("version", version);
		configs.addAll(appconfigMapper.findByQueryParams(params));
		
		Map<String, String> changeConfigs = new HashMap<>();
		for (AppconfigEntity config : configs) {
			if(fetchAll){ //全量
				ConfigParseUtils.parseConfigToKVMap(config).forEach( (k,v) -> {
					changeConfigs.put(k, v.toString());
				} );
			}else{
				List<AppConfigsHistoryEntity> latestHisEntity = appconfigHisMapper.findTopNLatest(config.getId(), 1);
				if(!latestHisEntity.isEmpty()){
					Map<String, String> _changeConfigs = ConfigAdminController.buildChangeConfigs(config, latestHisEntity.get(0).getContents());
					if(!_changeConfigs.isEmpty()){
						changeConfigs.putAll(_changeConfigs);
					}
				}else{
					ConfigParseUtils.parseConfigToKVMap(config).forEach( (k,v) -> {
						changeConfigs.put(k, k);
					} );
					
				}
			}
		}
		
		changeConfigs.put(LATEST_FETCH_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(changeConfigs), HttpStatus.OK);
	}

	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public @ResponseBody String ping() {
		return "200";
	}

}
