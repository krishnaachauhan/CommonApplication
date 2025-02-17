package com.easynet.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.stereotype.Component;

import com.acute.dao.crypto.DataEncrDecrImpl;

@Component
public class CustomEurekaClientConfigBean extends EurekaClientConfigBean{

	@Autowired
	ConfigCredentials configCredentials;
	
	@Override
	public List<String> getEurekaServerServiceUrls(String myZone){
		
		String ls_maskedPassword;
		String hexPasswordString;
		ArrayList<String> modifiedUrlList=new ArrayList<String>();
		
		List<String> urlList=super.getEurekaServerServiceUrls(myZone);
		String ls_username=configCredentials.getProperty("EUREKA.USERNAME");
		String ls_password=configCredentials.getProperty("EUREKA.PASSWORD");
				
		try {
			ls_maskedPassword=DataEncrDecrImpl.ofGetInstance().ofGetEncrData(ls_password);
			hexPasswordString = Hex.encodeHexString(ls_maskedPassword.getBytes());
		} catch (Exception e) {
			hexPasswordString="";
		}
	
		String ls_modifiedURL;
		
		if (urlList.size() > 0) {
			
			for (String url : urlList) {
				
				ls_modifiedURL=url.replace("EUREKA.USERNAME",ls_username)
						.replace("EUREKA.PASSWORD",hexPasswordString);
				
				modifiedUrlList.add(ls_modifiedURL);		
			}			
		}
		
		return modifiedUrlList;
	}	
}
