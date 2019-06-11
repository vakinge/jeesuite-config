package com.jeesuite.admin.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.jeesuite.admin.component.ConfigStateHolder;
import com.jeesuite.admin.component.ConfigStateHolder.ConfigState;
import com.jeesuite.admin.model.WrapperResponseEntity;

@Controller
@RequestMapping("/monitor/app")
public class AppMonitorController {

	
	private @Autowired ConfigStateHolder configStateHolder;
	
	@RequestMapping(value="{env}", method = RequestMethod.GET) 
	public ResponseEntity<WrapperResponseEntity> getActiveAppNodes(@PathVariable("env") String env){
		List<ConfigState> list = configStateHolder.get(env);
		return new ResponseEntity<WrapperResponseEntity>(new WrapperResponseEntity(list),HttpStatus.OK);
	}
	
	@RequestMapping(value="config/{env}/{appName}", method = RequestMethod.GET) 
	public ResponseEntity<WrapperResponseEntity> getNodeConfig(@PathVariable("env") String env,@PathVariable("appName") String appName,@RequestParam("nodeId") String nodeId){
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
}
