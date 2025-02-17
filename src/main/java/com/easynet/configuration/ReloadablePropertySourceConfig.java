package com.easynet.configuration;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;

import com.easynet.util.ConfigurationValues;

@Configuration
public class ReloadablePropertySourceConfig {

	//This is used in other class for get the details.
    public static ConfigurableEnvironment configurableEnvironment;

    public ReloadablePropertySourceConfig(@Autowired ConfigurableEnvironment env) {
        this.configurableEnvironment = env;
    }

    @Bean
    public PropertiesConfiguration propertiesConfiguration() throws Exception {  
    	String projectFolderpath=ConfigurationValues.PROJECT_FOLDER_PATH;//configurableEnvironment.getProperty("PROJECT_FOLDER_PATH");
    	File applicationFile=new File(projectFolderpath+"application.properties");
    	
        //String filePath=new ClassPathResource(File.separatorChar +"application.properties").getFile().getAbsolutePath();
        PropertiesConfiguration configuration = new PropertiesConfiguration(applicationFile); 
        configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
        return configuration;
    }
    
    @Bean
    public ReloadablePropertySource reloadablePropertySource(PropertiesConfiguration properties) {
        ReloadablePropertySource ret = new ReloadablePropertySource("dynamic", properties);
        MutablePropertySources sources = configurableEnvironment.getPropertySources();
        sources.addFirst(ret);
        return ret;
    }
}