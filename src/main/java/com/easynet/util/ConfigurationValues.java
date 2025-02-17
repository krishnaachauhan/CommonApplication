package com.easynet.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class ConfigurationValues {
	public static String PROJECT_NAME="Enfinity";
	public static String CATALINA_HOME=System.getProperty("catalina.home");
    public static String API_DATA=StringUtils.isBlank(CATALINA_HOME)?System.getProperty("user.dir"):CATALINA_HOME;
	public static String FILE_SEPRETOR=File.separator;
	public static String PROJECT_FOLDER_PATH="";
	public static String SPRING_CONFIG_LOCATION="";
	public static String COMMON_PROJECT_FOLDER_PATH="";
}
