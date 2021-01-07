package com.jeesuite.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesuite.admin.model.LoginUserInfo;
import com.jeesuite.admin.model.Menu;
import com.jeesuite.admin.model.SubMenu;
import com.jeesuite.admin.util.SecurityUtil;
import com.jeesuite.common.util.ResourceUtils;

@Controller
@RequestMapping("/admin")
public class AdminIndexController {
	
	@RequestMapping(value = "menus", method = RequestMethod.GET)
	public @ResponseBody List<Menu> getMenus(){
		
		LoginUserInfo userInfo = SecurityUtil.getLoginUserInfo();
		List<Menu> baseMenus = new ArrayList<>();
		Menu menu = new Menu("全局管理", "fa-cubes", false);
		if(userInfo.isSuperAdmin()){			
			menu.getChildren().add(new SubMenu("profile管理", "&#xe61d;", "profiles/list.html"));
		}
		menu.getChildren().add(new SubMenu("业务组管理", "&#xe613;", "group/list.html"));
		if(userInfo.isSuperAdmin() || userInfo.isGroupAdmin()){
			menu.getChildren().add(new SubMenu("用户管理", "&#xe612;", "user/list.html"));
		}
		menu.getChildren().add(new SubMenu("应用管理", "&#xe63b;", "app/list.html"));
		baseMenus.add(menu);
		
		menu = new Menu("配置管理", "fa-cubes", true);
		menu.getChildren().add(new SubMenu("配置列表", "&#xe60a;", "config/list.html"));
		menu.getChildren().add(new SubMenu("新建配置", "&#xe63c;", "config/add.html"));
		baseMenus.add(menu);

		return baseMenus;
	}


}
