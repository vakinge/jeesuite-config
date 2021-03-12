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

import com.jeesuite.admin.component.CryptComponent;
import com.jeesuite.admin.component.ProfileZkClient;
import com.jeesuite.admin.constants.AppExtrAttrName;
import com.jeesuite.admin.controller.admin.ConfigAdminController;
import com.jeesuite.admin.dao.entity.AppConfigsHistoryEntity;
import com.jeesuite.admin.dao.entity.AppEntity;
import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.dao.mapper.AppConfigsHistoryEntityMapper;
import com.jeesuite.admin.dao.mapper.AppEntityMapper;
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
	private @Autowired AppEntityMapper appMapper;
	private @Autowired CryptComponent cryptComponent;
	private @Autowired ProfileZkClient profileZkClient;
	private @Autowired AppConfigsHistoryEntityMapper appconfigHisMapper;

	@RequestMapping(value = "fetch_all_configs", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> fetchConfigs(HttpServletRequest request,
			@RequestParam(value = "appName") String appName, @RequestParam(value = "env") String env,
			@RequestParam(value = "version", required = false) String version,
			@RequestParam(value = "globalVersion", required = false) String globalVersion,
			@RequestParam(value = "ignoreGlobal", required = false,defaultValue="false") boolean ignoreGlobal) {

		if (StringUtils.isBlank(version)){
			version = Constants.DEFAULT_CONFIG_VERSION;
		}

		if(StringUtils.isBlank(globalVersion)){
			globalVersion = version;
		}

		AppEntity appEntity = appMapper.findByAppKey(appName);
		if (appEntity == null)
			throw new JeesuiteBaseException(1001, "app不存在");

		validateSignature(request,appEntity.getId(),env);

		List<AppconfigEntity> globalConfigs = ignoreGlobal ? new  ArrayList<>(0) : appconfigMapper.findGlobalConfig(env,appEntity.getGroupId(), globalVersion);
		// 再查应用的
		Map<String, Object> queyParams = new HashMap<>();
		queyParams.put("env", env);
		queyParams.put("version", version);
		queyParams.put("appId", appEntity.getId());
		List<AppconfigEntity> appConfigs = appconfigMapper.findByQueryParams(queyParams);

		if (globalConfigs.isEmpty() && appConfigs.isEmpty()) {
			throw new JeesuiteBaseException(1001, "配置不存在");
		}

		Map<String, Object> result = new HashMap<>();

		if (!globalConfigs.isEmpty()) {
			for (AppconfigEntity config : globalConfigs) {
				ConfigParseUtils.parseConfigToKVMap(result, config);
			}
			String cryptKey = cryptComponent.getCryptKey(0, env);
			result.put("jeesuite.configcenter.global-encrypt-secret", cryptKey);
		}

		for (AppconfigEntity config : appConfigs) {
			ConfigParseUtils.parseConfigToKVMap(result, config);
		}
		String cryptKey = cryptComponent.getCryptKey(appEntity.getId(), env);
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
	public @ResponseBody ResponseEntity<WrapperResponseEntity> syncStatus(HttpServletRequest request,@RequestBody Map<String, Object> params) {
		String version = Objects.toString(params.remove("version"), null);
		Date lastFetchTime = new Date(Long.parseLong(params.remove("lastTime").toString()));
		boolean ignoreGlobal = params.containsKey("ignoreGlobal") && Boolean.parseBoolean(params.remove("ignoreGlobal").toString());
		boolean fetchAll = params.containsKey("fetchAll") && Boolean.parseBoolean(params.remove("fetchAll").toString());
		String env = params.get("env").toString();
		
		if (StringUtils.isBlank(version)){
			version = Constants.DEFAULT_CONFIG_VERSION;
		}
		AppEntity appEntity = appMapper.findByAppKey(params.remove("appName").toString());
		//
		validateSignature(request, appEntity.getId(),env);
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

	private void validateSignature(HttpServletRequest request,int appId,String env ) {
		String authtoken = request.getParameter("authtoken");
		if (StringUtils.isBlank(authtoken))
			throw new JeesuiteBaseException(400,"[authtoken] is miss");
		
		String value = appMapper.findExtrAttr(appId, env, AppExtrAttrName.API_TOKEN.name());
		if(!StringUtils.equals(authtoken, value)) {
			throw new JeesuiteBaseException(400,"[authtoken] validate fail");
		}
	}
}
