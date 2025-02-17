package com.easynet.configuration;

import java.io.IOException;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

public class ReloadablePropertySourceFactory extends DefaultPropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String s, EncodedResource encodedResource)
      throws IOException {    	
        Resource internal = encodedResource.getResource();
        try {
        	if (internal instanceof FileSystemResource) {
        		return new ReloadablePropertySource(s, ((FileSystemResource) internal)
        				.getPath());}
        	if (internal instanceof FileUrlResource) {       		
        		return new ReloadablePropertySource(s, ((FileUrlResource) internal)
        				.getURL()
        				.getPath());
        	}
        	if (internal instanceof ClassPathResource) {        	
        		return new ReloadablePropertySource(s, ((ClassPathResource) internal).getFile().getAbsolutePath());
        	}
        }catch(Exception exception) {
        	exception.printStackTrace();
        }
        return super.createPropertySource(s, encodedResource);        
    }
}
