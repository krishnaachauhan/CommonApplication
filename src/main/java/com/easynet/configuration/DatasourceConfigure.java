package com.easynet.configuration;

import java.util.Properties;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.acute.dao.crypto.DataEncrDecrImpl;
import com.easynet.util.GetFileConfig;

@Configuration
@ConfigurationProperties("spring.datasource")
/**
 *@author Sagar Umate
 *@Date-27/11/2021
 *This class made for dynamically changes the profile set.
 *using this class user can connect with multiple database.
 *When #spring.profiles.active is active in application.properties then at the time of deployment spring will take bean for respective profile.
 *If you change in active profile in spring boot then you need to restart the server.
 * */
public class DatasourceConfigure {
		
	private Properties prop=null;
	
	/*just for create bean before this class.*/
	@Autowired
	ReloadablePropertySourceConfig ReloadablePropertySourceConfig;
	
	@RefreshScope
	@Profile("PROD")
	@Bean
	@Primary
	public DataSource getProdDataSource() throws Exception{
		
		String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
		String devPropertyFilePath=GetFileConfig.GetFileFromConfigServer(LS_CONFIG_PATH, "DataSourceConfig-PROD.properties");
		prop = DataEncrDecrImpl.ofGetInstance().ofGetDecrData(devPropertyFilePath);
//		System.out.println(prop);
						
		//delete the file after load in properties object.
		GetFileConfig.doDeleleConfigFile(devPropertyFilePath);		
		
	    DriverManagerDataSource dataSource = new DriverManagerDataSource();	    	    
	    dataSource.setUsername(prop.getProperty("username").trim());
	    dataSource.setPassword(prop.getProperty("password").trim());
	    dataSource.setUrl(prop.getProperty("url").trim());
	    dataSource.setDriverClassName(prop.getProperty("driver_name").trim());
	   
	    return dataSource;
	}	
	
	@RefreshScope
	@Profile("UAT")
	@Bean
	public DataSource getUatDataSource() throws Exception {
		String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
		Properties prop=null;
				
		String devPropertyFilePath=GetFileConfig.GetFileFromConfigServer(LS_CONFIG_PATH,"DataSourceConfig-UAT.properties");
		prop = DataEncrDecrImpl.ofGetInstance().ofGetDecrData(devPropertyFilePath);
		
		//delete the file after load in properties object.
		GetFileConfig.doDeleleConfigFile(devPropertyFilePath);		

	    DriverManagerDataSource dataSource = new DriverManagerDataSource();	    	    
	    dataSource.setUsername(prop.getProperty("username").trim());
	    dataSource.setPassword(prop.getProperty("password").trim());
	    dataSource.setUrl(prop.getProperty("url").trim());
	    dataSource.setDriverClassName(prop.getProperty("driver_name").trim());
	    return dataSource;
	}
	
	@RefreshScope
	@Profile("IBMB")
	@Bean
	@Primary
	public DataSource getIBMBDataSource() throws Exception {
		String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
		Properties prop=null;
				
		String devPropertyFilePath=GetFileConfig.GetFileFromConfigServer(LS_CONFIG_PATH,"DataSourceConfig-IBMB.properties");
		prop = DataEncrDecrImpl.ofGetInstance().ofGetDecrData(devPropertyFilePath);
		
		//delete the file after load in properties object.
		GetFileConfig.doDeleleConfigFile(devPropertyFilePath);		

	    DriverManagerDataSource dataSource = new DriverManagerDataSource();	    	    
	    dataSource.setUsername(prop.getProperty("username").trim());
	    dataSource.setPassword(prop.getProperty("password").trim());
	    dataSource.setUrl(prop.getProperty("url").trim());
	    dataSource.setDriverClassName(prop.getProperty("driver_name").trim());
	    return dataSource;
	}
	
	
	@RefreshScope
	@Profile("IBMB")
	@Bean("PROD")
	public DataSource getIBMBUatDataSource() throws Exception {
		String LS_CONFIG_PATH=ReloadablePropertySourceConfig.configurableEnvironment.getProperty("COMM_CONFIG_PATH");
		Properties prop=null;
				
		String devPropertyFilePath=GetFileConfig.GetFileFromConfigServer(LS_CONFIG_PATH,"DataSourceConfig-PROD.properties");
		prop = DataEncrDecrImpl.ofGetInstance().ofGetDecrData(devPropertyFilePath);
		
		//delete the file after load in properties object.
		GetFileConfig.doDeleleConfigFile(devPropertyFilePath);		

	    DriverManagerDataSource dataSource = new DriverManagerDataSource();	    	    
	    dataSource.setUsername(prop.getProperty("username").trim());
	    dataSource.setPassword(prop.getProperty("password").trim());
	    dataSource.setUrl(prop.getProperty("url").trim());
	    dataSource.setDriverClassName(prop.getProperty("driver_name").trim());
	    return dataSource;
	}
	public Properties getProp() {
		return prop;
	}
}