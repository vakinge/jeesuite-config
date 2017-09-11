package com.jeesuite.admin.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.jeesuite.admin.interceptor.SecurityInterceptor;

@Configuration
public class CostomWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new SecurityInterceptor()).addPathPatterns("/**").excludePathPatterns("/login.html","/auth/**");
        super.addInterceptors(registry);
	}

}
