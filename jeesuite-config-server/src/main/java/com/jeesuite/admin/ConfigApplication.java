package com.jeesuite.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.springboot.starter.mybatis.EnableJeesuiteMybatis;

@Controller
@SpringBootApplication
@MapperScan(basePackages = "com.jeesuite.admin.dao.mapper")
@ComponentScan(value = {"com.jeesuite.admin"})
@EnableJeesuiteMybatis
@EnableTransactionManagement
public class ConfigApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(ConfigApplication.class).web(WebApplicationType.SERVLET).run(args);
		System.out.println("================================");
		System.out.println("start ok!! env.type:" + ResourceUtils.getProperty("env.type"));
		System.out.println("================================");
	}
	
	@RequestMapping("/")
    String home() {
        return "redirect:/admin.html";
    }
    
}
