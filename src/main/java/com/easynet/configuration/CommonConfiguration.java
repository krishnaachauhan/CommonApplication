package com.easynet.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.easynet.util.ConfigurationValues;
import com.easynet.filter.PreRequestProcess;

@Configuration
public class CommonConfiguration {	
	
	@Autowired
	ReloadablePropertySourceConfig reloadablePropertySourceConfig;
	
	@RefreshScope
	@Bean
	public MessageSource messageSource() {	
 		
 		String LS_CONFIG_PATH=reloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
 		
		ReloadableResourceBundleMessageSource messageSource= new ReloadableResourceBundleMessageSource();
							
		//messageSource.setBasename("classpath:messages");	
		//messageSource.setCacheSeconds(60);
		//messageSource.setBasename("file:"+projectFolderpath+"messages");
		messageSource.setBasename(LS_CONFIG_PATH+"messages");		
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

//	@Bean
//	public MessageSource messageSource() {
//		String projectFolderpath=ConfigurationValues.COMMON_PROJECT_FOLDER_PATH;//configurableEnvironment.getProperty("PROJECT_FOLDER_PATH");
//		
//		ReloadableResourceBundleMessageSource messageSource= new ReloadableResourceBundleMessageSource();
//							
//		//messageSource.setBasename("classpath:messages");	
//		messageSource.setBasename("file:"+projectFolderpath+"messages");
//		messageSource.setDefaultEncoding("UTF-8");
//		messageSource.setCacheSeconds(60);
//		//org.springframework.web.cors.CorsConfiguration
//		return messageSource;
//	}
	
	
//	@Order(-1)
// 	@Bean
//    public CorsFilter corsFilter() {//cross site security
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOrigin("*");
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//	
//	@Order(0)
// 	@Bean
// 	public PreRequestProcess preRequestProcess() {
//        return new PreRequestProcess();
//    }
}
