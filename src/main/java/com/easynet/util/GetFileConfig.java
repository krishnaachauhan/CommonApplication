package com.easynet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.easynet.impl.LoggerImpl;

public class GetFileConfig {
	
	static Logger LOGGER=LoggerFactory.getLogger(GetFileConfig.class);
	
	/**
	 * @param apiUrl       -pass the url link.
	 * @param httpHeaderss - String format json object to set into request header.
	 * @return -Return response data in string format.
	 */
	public static String GetFileFromConfigServer(String as_baseApiUrl,String as_fileName) throws Exception {

		String responseString = "";
		String outputString = "";
		LoggerImpl loggerImpl=null;
		String ls_localPath=ConfigurationValues.PROJECT_FOLDER_PATH;		
		String ls_actualErrMsg;
		
		try {
			loggerImpl=new LoggerImpl();
			
			URL url = new URL(as_baseApiUrl+as_fileName);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;

			// add reuqest header
			httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			httpConn.setRequestMethod("GET");
			//httpConn.setRequestProperty("Authorization", "Bearer " + AccessToken);
			httpConn.setConnectTimeout(10000);
			httpConn.setReadTimeout(6000);
			httpConn.setDoOutput(true);
			
			if (httpConn.getResponseCode() != 200){
				
				//get error message of API and return error in response.
				InputStream errorObjectStream = httpConn.getErrorStream();
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorObjectStream));

				StringBuilder errorRespoceData = new StringBuilder();
				try {
					String responseDataLine = null;
					while ((responseDataLine = errorReader.readLine()) != null) {
						errorRespoceData.append(responseDataLine);
					}
				}finally {

					errorReader.close();
					httpConn.disconnect();
				}	
				
				outputString="Response Status-"+String.valueOf(httpConn.getResponseCode())+"Error-"+errorRespoceData.toString();
				throw new Exception(outputString);
								
			} else {

				ls_localPath=ls_localPath+"temp_"+as_fileName;
				File tempFile=new File(ls_localPath);
				FileOutputStream fileOutputStream = new FileOutputStream(tempFile);		
				
				InputStreamReader inputStreamReader = new InputStreamReader(httpConn.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				// Write the message response to a String.
				while ((responseString = bufferedReader.readLine()) != null) {					
					fileOutputStream.write(responseString.getBytes());
					fileOutputStream.write("\r\n".getBytes());					
				}
				
				fileOutputStream.flush();
				fileOutputStream.close();								
				
				httpConn.disconnect();
				return ls_localPath;
			}			
		} catch (Exception exception) {		
			//writh the exception in logs
			 ls_actualErrMsg = common.ofGetTotalErrString(exception, "Exception generated at the time of reading config file data-"+as_fileName+".");
			loggerImpl.error(LOGGER,"Exception : " + ls_actualErrMsg,"GetFileFromConfigServer");
						
			throw exception;			
		}		
	}
	
	public static void doDeleleConfigFile(String asFilePath) {
		File deleteConfigFile=new File(asFilePath);
		deleteConfigFile.delete();		
	}	
}
