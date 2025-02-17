package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;

import com.easynet.util.ConfigurationValues;
import com.easynet.util.common;

//@EnableAspectJAutoProxy
@SpringBootApplication
@ComponentScan("com.easynet.util")
public class CommonApplication{	
	
	public static void main(String[] args) throws Exception {				
		
		String ls_configLocation;
		//this method used to set the project configuration detail
		common.ofSetProjectConfigDtl();		
		ls_configLocation="--spring.config.location="+ConfigurationValues.SPRING_CONFIG_LOCATION;
		SpringApplication.run(CommonApplication.class, ls_configLocation);		
	}

//	@Override
//	public void onStartup(ServletContext servletContext) throws ServletException {
//		
//		//this method used to set the project configuration detail
//		try {
//			common.ofSetProjectConfigDtl();
//		} catch (Exception e) {			
//			e.printStackTrace();
//		}
//		servletContext.setInitParameter("spring.config.location",ConfigurationValues.SPRING_CONFIG_LOCATION);
//		super.onStartup(servletContext);		
//	}
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(){
		return new RestTemplate();
	} 
}