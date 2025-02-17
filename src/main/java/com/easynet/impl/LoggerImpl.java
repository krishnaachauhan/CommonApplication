package com.easynet.impl;

import java.util.List;


import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.core.env.Environment;

import com.easynet.bean.GetApplicationData;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.ReloadablePropertySourceConfig;
import com.easynet.util.common;

/**
 * The custom logging logic write in this class. Main purpose of writing this
 * class is to generate the all logs from one place. Only The required method of
 * logger class are implemented in this class.
 * 
 * @author Sagar Umate
 * @Date 10/03/2021
 */

public class LoggerImpl {

	private Profiler profiler = null;
	Environment environment;
	//static AnnotationConfigApplicationContext ctx;

	//@Value("${PER_MON_LOG_GEN}")
	private String PER_MON_LOG_GEN = "";
	private String LOG_WRITE = "";
	private String MASKED_LOG_DATA = "";

	private List<Profiler> childprofiler;	

	Logger LOGGER=LoggerFactory.getLogger(LoggerImpl.class);
	GetRequestUniqueData getRequestUniqueData;
	GetApplicationData getApplicationData;

	/* set the initial values */
	void getCommonProperties() {
		try {
			//ctx = new AnnotationConfigApplicationContext();
			//environment = ctx.getEnvironment();
			environment = ReloadablePropertySourceConfig.configurableEnvironment;
			PER_MON_LOG_GEN = environment.getProperty("PER_MON_LOG_GEN");
			LOG_WRITE = environment.getProperty("LOG_WRITE");	
			MASKED_LOG_DATA = environment.getProperty("MASKED_LOG_DATA");
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	/**
	 * This is customized trace method used for write logs.
	 * 
	 * @param logger -name of logger from which print the logs.
	 * @param msg    -msg to print
	 * @param object -multiple argument object. 1.API Name. <br> 
	 * 				2.object for print.<br>
	 *               3.Replace the data of msg.
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void trace(Logger logger, String msg, Object... object) {
		String apiName = "";
		String msgStr="";
		String ls_error="";
		try {
			getCommonProperties();
			if ("Y".equalsIgnoreCase(LOG_WRITE)) {
				apiName = String.valueOf(object[0]);
				Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));								
				msgStr=getJsonFormatStr(msg);				
				logger.trace(apiNameMarker, msgStr);				
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:trace"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized debug method used for write logs.
	 * 
	 * @param logger -name of logger from which print the logs.
	 * @param msg    -msg to print
	 * @param object -multiple argument object. 1.API Name.<br> 
	 * 				 2.object for print.<br>
	 *               3.Replace the data of msg.
	 * @return nothing to return only print msg.
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void debug(Logger logger, String msg, Object... object) {
		String apiName = "";
		String msgStr="";
		String 	ls_error="";

		try {
			getCommonProperties();
			if ("Y".equalsIgnoreCase(LOG_WRITE)) {
				apiName = String.valueOf(object[0]);
				Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));
				msgStr=getJsonFormatStr(msg);
				logger.debug(apiNameMarker, msgStr);
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:debug"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized info method used for write logs.
	 * 
	 * @param logger -name of logger from which print the logs.
	 * @param msg    -msg to print
	 * @param object -multiple argument object. 1.API Name. <br>
	 * 				2.object for print.<br>
	 *               3.Replace the data of msg.
	 * @return nothing to return only print msg
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void info(Logger logger, String msg, Object... object) {
		String apiName = "";
		String msgStr="";
		String ls_error="";
		try {
			getCommonProperties();
			if ("Y".equalsIgnoreCase(LOG_WRITE)) {
				apiName = String.valueOf(object[0]);
				Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));
				msgStr=getJsonFormatStr(msg);
				logger.info(apiNameMarker,msgStr);
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:info"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized error method used for write logs.
	 * 
	 * @param logger -name of logger from which print the logs.
	 * @param msg    -msg to print
	 * @param object -multiple argument object. 1.API Name. <br>
	 * 				2.object for print.<br>
	 *               3.Replace the data of msg.
	 * @return nothing to return only print msg
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void error(Logger logger, String msg, Object... object) {
		String apiName = "";
		String msgStr="";
		String	ls_error="";
		try {
			getCommonProperties();
			if ("Y".equalsIgnoreCase(LOG_WRITE)) {
				apiName = String.valueOf(object[0]);
				Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));
				msgStr=getJsonFormatStr(msg);
				logger.error(apiNameMarker, msgStr);
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:error"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized printOptLogs method used for write performance logs.
	 * 
	 * @param logger -name of logger from which print the logs.
	 * @param msg    -msg to print
	 * @param object -multiple argument object. 1.API Name.<br>
	 * 				 2.object for print.<br>
	 *               3.Replace the data of msg.
	 * @return nothing to return only print msg
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void printOptLogs(Logger logger, String msg, Object... object) {
		String apiName = "";
		String msgStr="";
		String ls_error="";

		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(LOG_WRITE)) {
				apiName = String.valueOf(object[0]);
				Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));
				msgStr=getJsonFormatStr(msg);
				logger.debug(apiNameMarker, msgStr);
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:printOptLogs"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized generateProfiler method used for generate the profiler.
	 * 
	 * @param aprofileName -name of profiler
	 * @return profile object if generated else null.
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public Profiler generateProfiler(String aprofileName) {
		String ls_error="";
		Profiler childProfiler;
		Profiler lastProfiler;
		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {

				lastProfiler=getLastProfiler();
				childProfiler=lastProfiler.startNested(aprofileName);
				childprofiler.add(childProfiler);				
				return childProfiler;
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:generateProfiler"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
		return null;
	}

	/**
	 * This is customized startProfiler method used for start the profiler.
	 * 
	 * @param name -name of watcher
	 * @return true if generated else false.
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public boolean startProfiler(String name) {
		String ls_error="";
		Profiler currentProfiler;
		try {
			getCommonProperties();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {
				getMainProfiler();
				currentProfiler=getLastProfiler();
				currentProfiler.start(name);
				return true;
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:startProfiler"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
		return false;
	}

	/**
	 * This is customized stopProfiler method used for stop the profiler.
	 * 
	 * @return true if stop else false.
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public boolean stopProfiler() {
		String ls_error="";
		Profiler currentProfiler;
		
		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {
				currentProfiler=getLastProfiler();
				currentProfiler.stop();
				removeLastProfiler();
				return true;
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:stopProfiler"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
		return false;
	}


	public boolean stopMainProfiler() { 
		String ls_error=""; 
		long ll_totTime=0;
		try {
			getCommonProperties();
			getMainProfiler();
			if("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {
				this.profiler.stop(); 
				ll_totTime=this.profiler.elapsedTime();
				ll_totTime=ll_totTime > 0?ll_totTime/1000000:0;
				MDC.put("TOT_TIME",ll_totTime+" milliseconds");
				return true;
			} 
		}catch (Exception exception) {
			Marker apiNameMarker =MarkerFactory.getMarker(common.getAPINameandType("IN:stopMainProfiler")); 
			ls_error =common.ofGetTotalErrString(exception, "");
			LOGGER.error(apiNameMarker,ls_error); 
		} 
		return false; 
	}


	/**
	 * This is customized getProfilerStr method used for get detail of profile in
	 * string format.
	 * 
	 * @return String if detail available else ""
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public String getProfilerStr() {

		String ls_error="";
		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {
				return this.profiler.toString();				
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:getProfilerStr"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
		return "";
	}

	/**
	 * This is customized stopAndPrintOptLogs method. This method stop the
	 * child profiler
	 *  
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void stopAndPrintOptLogs(Logger logger, String msg, Object... object) {

		String ls_error="";
		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN) && this.profiler != null) {				
				stopProfiler();
			}

		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:stopAndPrintOptLogs"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	/**
	 * This is customized stopAndPrintOptLogs method. This method stop the
	 * main profiler and print the details.
	 *  
	 * @exception if exception generated then print the logs on console.
	 *
	 */
	public void stopAndPrintMainOptLogs(Logger logger, String msg, Object... object) {
		String apiName = "";
		String profilerStr = "";
		String msgStr="";
		String ls_error="";
		String ls_returnValues="";

		try {
			getCommonProperties();
			getMainProfiler();
			if ("Y".equalsIgnoreCase(PER_MON_LOG_GEN) && this.profiler != null) {

				stopMainProfiler();				
				profilerStr = getProfilerStr();

				if ("Y".equalsIgnoreCase(LOG_WRITE)) {
					apiName = String.valueOf(object[0]);
					Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType(apiName));
					msgStr=getJsonFormatStr(msg); 
					
					ls_returnValues= msgStr + "\n" +profilerStr;
					//set the multi-line message in single line message.
					if(ls_returnValues.indexOf("\r\n") > 0) {										
						ls_returnValues=new JSONArray().put(ls_returnValues).toString();
						if(ls_returnValues.length() >=2 ) ls_returnValues=ls_returnValues.substring(2,ls_returnValues.length() -2);										
					}
										
					logger.debug(apiNameMarker,ls_returnValues); 
				}
			}
		} catch (Exception exception) {
			Marker apiNameMarker = MarkerFactory.getMarker(common.getAPINameandType("IN:stopAndPrintOptLogs"));
			ls_error = common.ofGetTotalErrString(exception, "");			
			LOGGER.error(apiNameMarker,ls_error);
		}
	}

	public String getJsonFormatStr(String inputStr)throws Exception  {
		String ls_returnValues=inputStr;
		
		while(ls_returnValues.contains("\\\\\"")) {			
			ls_returnValues=ls_returnValues.replace("\\\\\"", "\\\"");						
		}
		
		getCommonProperties();
		if("Y".equalsIgnoreCase(MASKED_LOG_DATA)){			
			getApplicationData=GetApplicationData.getBean(GetApplicationData.class);			
			ls_returnValues=common.ofReplaceData(ls_returnValues,getApplicationData.getMaskedColumnProperties());			
		}
		
		//set the multi-line message in single line message.
		if(ls_returnValues.indexOf("\r\n") > 0) {		
			if(ls_returnValues.trim().startsWith("{") || (ls_returnValues.trim().startsWith("[")&& common.ofvalidateJSonArrayData(ls_returnValues))){						
			}else {			
				ls_returnValues=new JSONArray().put(ls_returnValues).toString();
				if(ls_returnValues.length() >=2 ) ls_returnValues=ls_returnValues.substring(2,ls_returnValues.length() -2);					
			}
		}
		
		/*This below code for set data in message key in logging.*/
		if(ls_returnValues.trim().startsWith("{") ||ls_returnValues.trim().startsWith("[")){			
			ls_returnValues=new JSONArray().put(ls_returnValues).toString();
			if(ls_returnValues.length() >=2 ) ls_returnValues=ls_returnValues.substring(2,ls_returnValues.length() -2);					
		}
				
		return ls_returnValues;			
	}

	private Profiler getLastProfiler() {
		int li_size;
		Profiler childProfiler;

		li_size=this.childprofiler.size();

		if (li_size > 0) {
			childProfiler=this.childprofiler.get(li_size - 1 );			
		}else{
			childProfiler=this.profiler;
		}
		return childProfiler;
	}

	private void removeLastProfiler() {
		int li_size;

		li_size=this.childprofiler.size();

		if (li_size > 0) {
			this.childprofiler.remove(li_size - 1);			
		}
	}

	private void getMainProfiler() {	
		getCommonProperties();
		if("Y".equalsIgnoreCase(PER_MON_LOG_GEN)) {		
			getRequestUniqueData=GetRequestUniqueData.getBean(GetRequestUniqueData.class);
			profiler=getRequestUniqueData.getProfiler();
			childprofiler=getRequestUniqueData.getChildprofiler();
		}
	}
	
	public String getStringJson(Object object) {
		JSONArray jSONArray = new JSONArray();

		String jsonStr = "";
		String changeStr = "";

		changeStr = jSONArray.put(new JSONArray(jsonStr).toString()).toString();
		changeStr = changeStr.trim().substring(2, changeStr.length() - 2);
		return changeStr;

		/*
		 * if(object instanceof String) { jsonStr=(String)object;
		 * if(jsonStr.trim().startsWith("{")) { changeStr=jSONArray.put(new
		 * JSONObject(jsonStr).toString()).toString();
		 * changeStr=changeStr.trim().substring(2,changeStr.length()- 2); return
		 * changeStr;
		 * 
		 * }else if(jsonStr.trim().startsWith("[")) { changeStr=jSONArray.put(new
		 * JSONArray(jsonStr).toString()).toString();
		 * changeStr=changeStr.trim().substring(2,changeStr.length()- 2); return
		 * changeStr;
		 * 
		 * } }
		 */

		//	return object.toString();
	}
}
