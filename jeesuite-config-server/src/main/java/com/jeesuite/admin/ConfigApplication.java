package com.jeesuite.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeesuite.common.util.NodeNameHolder;
import com.jeesuite.common.util.ResourceUtils;
import com.jeesuite.security.SecurityDelegatingFilter;
import com.jeesuite.springboot.starter.cache.EnableJeesuiteCache;
import com.jeesuite.springboot.starter.mybatis.EnableJeesuiteMybatis;

@Controller
@SpringBootApplication
@MapperScan(basePackages = "com.jeesuite.admin.dao.mapper")
@ComponentScan(value = { "com.jeesuite.admin" })
@EnableJeesuiteMybatis
@EnableJeesuiteCache
@EnableTransactionManagement
public class ConfigApplication {
	public static void main(String[] args) {

		System.setProperty("client.nodeId", NodeNameHolder.getNodeId());
		new SpringApplicationBuilder(ConfigApplication.class).web(WebApplicationType.SERVLET).run(args);
		System.out.println("...............................................................");
		System.out.println("..................Service starts successfully..................");
		System.out.println("spring.profiles.active:" + ResourceUtils.getProperty("spring.profiles.active"));
		System.out.println("...............................................................");
	}
	
	@Bean
	public FilterRegistrationBean<SecurityDelegatingFilter> someFilterRegistration() {
	    FilterRegistrationBean<SecurityDelegatingFilter> registration = new FilterRegistrationBean<>();
	    registration.setFilter(new SecurityDelegatingFilter());
	    registration.addUrlPatterns("/user/*");
	    registration.addUrlPatterns("/admin/*");
	    registration.setName("authFilter");
	    registration.setOrder(0);
	    return registration;
	}

	@RequestMapping("/")
	String home() {
		return "redirect:/admin.html";
	}

}
