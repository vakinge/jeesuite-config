package com.jeesuite.admin.controller.admin;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.model.WrapperResponseEntity;
import com.jeesuite.common.util.SimpleCryptUtils;

/**
 * 
 * <br>
 * Class Name   : CommonToolController
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年9月9日
 */
@Controller
@RequestMapping("/tool")
public class CommonToolController {

	@PostMapping("encrypt")
	public @ResponseBody WrapperResponseEntity encryptString(@RequestBody Map<String, String> param){
		String cryptKey = param.get("cryptKey");
		String data = param.get("data");
		return new WrapperResponseEntity(SimpleCryptUtils.encrypt(cryptKey, data));
	}
}
