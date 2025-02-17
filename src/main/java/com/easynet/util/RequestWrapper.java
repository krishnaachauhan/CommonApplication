package com.easynet.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

public class RequestWrapper  extends HttpServletRequestWrapper {

	private final Map<String, String[]> modifiableParameters;
	private Map<String, String[]> allParameters = null;
	
	// holds custom header and value mapping
	private Map<String, String> customHeaders;
	private ResettableServletInputStream servletStream;
	
	/**
	 * Create a new request wrapper that will merge additional parameters into
	 * the request object without prematurely reading parameters from the
	 * original request.
	 * 
	 * @param request
	 * @param additionalParams
	 */

	public RequestWrapper(HttpServletRequest request) {
		super(request);
		modifiableParameters = new TreeMap<String, String[]>();
		//modifiableParameters.putAll(additionalParams);
		this.customHeaders = new HashMap<String, String>();
		this.servletStream = new ResettableServletInputStream();
	}

	@Override
	public String getParameter(final String name){
		String[] strings = getParameterMap().get(name);
		if (strings != null){
			return strings[0];
		}
		return super.getParameter(name);
	}

	@Override
	public Map<String, String[]> getParameterMap(){
		if (allParameters == null){
			allParameters = new TreeMap<String, String[]>();
			allParameters.putAll(super.getParameterMap());
			allParameters.putAll(modifiableParameters);
		}
		//Return an unmodifiable collection because we need to uphold the interface contract.
		return Collections.unmodifiableMap(allParameters);
	}

	@Override
	public Enumeration<String> getParameterNames(){
		return Collections.enumeration(getParameterMap().keySet());
	}

	@Override
	public String[] getParameterValues(final String name){
		return getParameterMap().get(name);
	}

	public void putParameter(final Map<String, String[]> additionalParams){
		modifiableParameters.putAll(additionalParams);
	}
	
	/*Below all code for get and set request header in request*/
	public void putHeader(String name, String value){
		this.customHeaders.put(name, value);
	}
		
	public String getHeader(String name) {
		// check the custom headers first
		String headerValue = this.customHeaders.get(StringUtils.upperCase(name));		
		if (headerValue != null){
			return headerValue;
		}
		// else return from into the original wrapped object
		return ((HttpServletRequest) getRequest()).getHeader(name);
	}

	public Enumeration<String> getHeaderNames() {
		// create a set of the custom header names
		Set<String> set = new HashSet<String>(this.customHeaders.keySet());

		// now add the headers from the wrapped request object
		@SuppressWarnings("unchecked")
		Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
		while (e.hasMoreElements()) {
			// add the names of the request headers into the list
			String n = e.nextElement();
			set.add(n);
		}

		// create an enumeration from the set and return
		return Collections.enumeration(set);
	}

	public void resetInputStream(byte[] newRawData) {
		servletStream.stream = new ByteArrayInputStream(newRawData);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if(servletStream.stream==null) {
			return super.getInputStream();
		}else {		
			return servletStream;
		}
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(servletStream));
	}


	private class ResettableServletInputStream extends ServletInputStream {

		private InputStream stream;

		@Override
		public int read() throws IOException {
			return stream.read();
		}

		@Override
		public boolean isFinished() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setReadListener(ReadListener listener) {
			// TODO Auto-generated method stub

		}
	}  

//	public String getBody(HttpServletRequest orgHttpRequest) throws IOException{
//
//		StringBuilder stringBuilder = new StringBuilder();
//		BufferedReader bufferedReader = null;
//		try {
//			InputStream inputStream = orgHttpRequest.getInputStream();
//			if (inputStream != null){
//				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//				char[] charBuffer = new char[128];
//				int bytesRead = -1;
//				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
//					stringBuilder.append(charBuffer, 0, bytesRead);
//				}
//			} else {
//				stringBuilder.append("");
//			}
//		} catch (IOException ex) {
//			throw ex;
//		} finally {
//			if (bufferedReader != null) {
//				try {
//					bufferedReader.close();
//				} catch (IOException ex) {
//					throw ex;
//				}
//			}
//		}
//		//Store request body content in 'body' variable 
//		return stringBuilder.toString();
//	}	

}