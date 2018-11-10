package com.jeesuite.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeesuite.springboot.starter.mybatis.EnableJeesuiteMybatis;

@Controller
@SpringBootApplication
@MapperScan(basePackages = "com.jeesuite.admin.dao.mapper")
@ComponentScan(value = {"com.jeesuite.admin"})
@EnableJeesuiteMybatis
public class ConfigApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(ConfigApplication.class).web(WebApplicationType.SERVLET).run(args);
	}
	
	@RequestMapping("/")
    String home() {
        return "redirect:/admin.html";
    }
    
}
