package com.easynet.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.easynet.bean.GetApplicationData;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.ConfigCredentials;
import com.easynet.configuration.DatasourceConfigure;
import com.easynet.configuration.ReloadablePropertySourceConfig;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.CommonBase;
import com.easynet.util.CustomContentCachingResponseWrapper;
import com.easynet.util.RequestWrapper;
import com.easynet.util.common;

import static com.easynet.util.ConstantKeyValue.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
@Order(0)
public class PreRequestProcess extends OncePerRequestFilter {

	private String ls_instance_id;

	static Logger LOGGER = LoggerFactory.getLogger(PreRequestProcess.class);

	final static List<String> EXCLUDE_URL = Arrays.asList(
			"/enfinityCommonServiceAPI/GETDYNAMICDATA/GETLOGINPAGEDTL","/enfinityCommonServiceAPI/GETDYNAMICDATA/GETLOGINIMAGEDATA", "actuator/health");

	@Autowired
	private GetApplicationData getApplicationData;

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;

	@Autowired
	CommonBase commonBase;

	@Autowired
	DatasourceConfigure datasourceConfigure;

	@Autowired
	ConfigCredentials configCredentials;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String ls_responseData = StringUtils.EMPTY;
		String ls_screenName = StringUtils.EMPTY;
		JSONObjectImpl requestDataJson = null;
		String ls_langCode = StringUtils.EMPTY;
		String ls_unique_ref_ID = StringUtils.EMPTY;
		long ll_uniqueNumber;
		LoggerImpl loggerImpl = null;
		Logger reqResLogger = null;
		byte[] responseArray = null;
		String ls_uniqueNO = StringUtils.EMPTY;
		String ls_apiToken = StringUtils.EMPTY;
		String ls_userName = StringUtils.EMPTY;
		String ls_requestIp = StringUtils.EMPTY;
		boolean lb_errorGenerated = false;
		Profiler mainProfiler = null;
		CustomContentCachingResponseWrapper responseWrapper = null;
		String ls_requestURI = request.getRequestURI();
		String ls_contentType = StringUtils.EMPTY;

		JSONObjectImpl loginUserDetailsJson = null;
		String ls_userRole = StringUtils.EMPTY;
		String ls_browser_fingerprint = StringUtils.EMPTY;
		String ls_machine_name = StringUtils.EMPTY;
		String ls_branchCode = StringUtils.EMPTY;
		String ls_baseBranchCode = StringUtils.EMPTY;
		String ls_baseCompCode = StringUtils.EMPTY;
		String ls_compCode = StringUtils.EMPTY;
		String ls_working_date = StringUtils.EMPTY;
		String ls_through_channel = StringUtils.EMPTY;

		try {
			loggerImpl = new LoggerImpl();

			/*
			 * get the wrapper response object for changes and store response data of
			 * request
			 */
			responseWrapper = new CustomContentCachingResponseWrapper(response);

			// for write the request and response logs.
			reqResLogger = LoggerFactory.getLogger("REQ_RES_LOGGER");

			// for future reference
			common.msgIdentifier = ReloadablePropertySourceConfig.configurableEnvironment.getProperty("MsgIdentifier");
			ls_instance_id = ReloadablePropertySourceConfig.configurableEnvironment
					.getProperty("eureka.instance.instance-id");

			// set the file data of masked column
			getApplicationData.getMaskedColumnProperties();

			// set the application name for store in logs
			MDC.put("instance_id", ls_instance_id);

			ls_uniqueNO = request.getHeader("UNIQUE_REQ_ID");
			ls_apiToken = request.getHeader("APITOKEN");
			ls_unique_ref_ID = request.getHeader("UNIQUE_REF_ID");
			ls_unique_ref_ID = ls_unique_ref_ID == null ? "" : ls_unique_ref_ID;

			// for check the request comes from gateway or not.
			if (StringUtils.isAnyBlank(ls_uniqueNO, ls_apiToken)) {
				ls_responseData = commonBase.ofGetFailedMSg("common.invalid_req_data", CommonBase.ST9992,
						"Wrong request parameter", "(ENP673)");

				MDC.put("uniqueReqID", "0");
				return;
			} else {
				ls_apiToken = new String(Base64.getDecoder().decode(ls_apiToken));
				if (!ls_apiToken.equals(ls_uniqueNO)) {
					ls_responseData = commonBase.ofGetFailedMSg("common.invalid_req_data", CommonBase.ST9992,
							"Wrong request parameter", "(ENP674)");

					MDC.put("uniqueReqID", "0");
					return;
				}
			}

			ll_uniqueNumber = Long.parseLong(ls_uniqueNO);
			getRequestUniqueData.setUniqueNumber(ll_uniqueNumber);

			// write this statement for print unique no. and screen name in log file
			MDC.put("uniqueReqID", String.valueOf(ll_uniqueNumber));
			getRequestUniqueData.setLs_action(ls_requestURI);

			ls_screenName = request.getHeader("SCREEN_NAME");
			ls_requestIp = getIpDetail(request);

			if (StringUtils.isBlank(ls_screenName)) {
				ls_screenName = ls_requestURI;
			}

			MDC.put("screenName", ls_screenName);
			MDC.put("uniqueRefId", ls_unique_ref_ID);
			MDC.put("actionName", ls_requestURI);
			MDC.put("requestFrom", "ADMINPANEL");
			MDC.put("requestIp", ls_requestIp);

			// set the profiler
			mainProfiler = new Profiler(ls_screenName);
			getRequestUniqueData.setProfiler(mainProfiler);

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			RequestWrapper modifiedRequest = new RequestWrapper(httpRequest);

			// set for future reference
			getRequestUniqueData.setRequest(modifiedRequest);

			ls_contentType = request.getHeader("Content-Type");

			/**
			 * Skpied the log writing of request in case of request data is not json data
			 **/
			if (MediaType.APPLICATION_JSON_VALUE.equals(ls_contentType)) {

				// Read the body data request.
				Scanner scanner = new Scanner(httpRequest.getInputStream(), "UTF-8").useDelimiter("\\A");
				String requestDataStr = scanner.hasNext() ? scanner.next() : "";

				requestDataJson = common.ofGetJsonObject(requestDataStr);
				ls_langCode = requestDataJson.getString("DISPLAY_LANGUAGE");
				if ("/authenticationServiceAPI/AUTH/LOGIN".equals(ls_requestURI)
						&& requestDataJson.has("LOGINUSERDETAILS")) {
					loginUserDetailsJson = requestDataJson.getJSONObject("LOGINUSERDETAILS");
					ls_userName = loginUserDetailsJson.getString("USERNAME");
					ls_userRole = loginUserDetailsJson.optString("USERROLE", "");
					ls_browser_fingerprint = loginUserDetailsJson.getString("BROWSER_FINGERPRINT");
					ls_machine_name = getHostName(ls_requestIp);
					ls_branchCode = loginUserDetailsJson.optString("BRANCH_CD");
					ls_baseBranchCode = loginUserDetailsJson.optString("BASE_BRANCH_CD");
					ls_baseCompCode = loginUserDetailsJson.optString("BASE_COMP_CD");
					ls_compCode = loginUserDetailsJson.optString("COMP_CD");
					ls_working_date = loginUserDetailsJson.optString("WORKING_DATE");
					ls_through_channel = loginUserDetailsJson.optString("THROUGH_CHANNEL");
				} else if ("/authenticationServiceAPI/AUTH/LOGIN".equals(ls_requestURI)
						|| "/Enfinity-AuthenticationService-1.0/authenticationServiceAPI/AUTH/LOGIN"
								.equals(ls_requestURI)
						|| "/authenticationServiceAPI/POSTLOGIN/VERIFYOTP".equals(ls_requestURI)) {
					ls_userName = requestDataJson.getString("USER_ID");
					ls_userRole = "0";
					ls_browser_fingerprint = requestDataJson.getString("BROWSER_FINGERPRINT");
					ls_machine_name = getHostName(ls_requestIp);
					loginUserDetailsJson = new JSONObjectImpl();
					loginUserDetailsJson.put("USERROLE", ls_userRole);
					loginUserDetailsJson.put("MACHINE_NAME", ls_machine_name);
				} else if ("/authenticationServiceAPI/AUTH/DOAUTHORIZEDREQUEST".equals(ls_requestURI)) {
					ls_userName = request.getHeader("USER_ID");
					ls_requestIp = getIpDetail(request);
					ls_browser_fingerprint = requestDataJson.optString("BROWSER_FINGERPRINT", "");
					loginUserDetailsJson = new JSONObjectImpl();
				} else {
					loginUserDetailsJson = requestDataJson.getJSONObject("LOGINUSERDETAILS");
					ls_userName = loginUserDetailsJson.getString("USERNAME");
					ls_userRole = loginUserDetailsJson.optString("USERROLE", "");
					ls_browser_fingerprint = loginUserDetailsJson.getString("BROWSER_FINGERPRINT");
					ls_machine_name = getHostName(ls_requestIp);
					ls_branchCode = loginUserDetailsJson.optString("BRANCH_CD");
					ls_baseBranchCode = loginUserDetailsJson.optString("BASE_BRANCH_CD");
					ls_baseCompCode = loginUserDetailsJson.optString("BASE_COMP_CD");
					ls_compCode = loginUserDetailsJson.optString("COMP_CD");
					ls_working_date = loginUserDetailsJson.optString("WORKING_DATE");
					ls_through_channel = loginUserDetailsJson.optString("THROUGH_CHANNEL");
				}

				if (StringUtils.isBlank(ls_machine_name)) {
					ls_machine_name = ls_requestIp;
				}

				loginUserDetailsJson.put("USERROLE", ls_userRole);
				loginUserDetailsJson.put("MACHINE_NAME", ls_machine_name);

				getRequestUniqueData.setUserName(ls_userName);
				getRequestUniqueData.setUserRole(ls_userRole);
				getRequestUniqueData.setBrowserFingerprint(ls_browser_fingerprint);
				getRequestUniqueData.setMachineName(ls_machine_name);
				getRequestUniqueData.setMachineIP(ls_requestIp);
				getRequestUniqueData.setLoginUserDetailsJson(loginUserDetailsJson);
				getRequestUniqueData.setBaseCompCode(ls_baseCompCode);
				getRequestUniqueData.setBaseBranchCode(ls_baseBranchCode);
				getRequestUniqueData.setBranchCode(ls_branchCode);
				getRequestUniqueData.setCompCode(ls_compCode);
				getRequestUniqueData.setWorkingDate(ls_working_date);
				getRequestUniqueData.setThroughChannel(ls_through_channel);

				if (!requestDataJson.has("MACHINE_IP")) {
					requestDataJson.put("MACHINE_IP", ls_requestIp);
				}

				if (!requestDataJson.has("MACHINE_NAME")) {
					requestDataJson.put("MACHINE_NAME", ls_machine_name);
				}

				MDC.put("userName", ls_userName);

				getRequestUniqueData.setRequestCommonDataJson(requestDataJson);
				getRequestUniqueData.setLangCode(ls_langCode);

				loggerImpl.info(reqResLogger, requestDataStr, "Request Data-Request");

				// set the authentication data in required format
				if ("/authenticationServiceAPI/AUTH/LOGIN".equals(ls_requestURI)
						|| "/Enfinity-AuthenticationService-1.0/authenticationServiceAPI/AUTH/LOGIN"
								.equals(ls_requestURI)) {
					doSetPreauthorizedData(request, modifiedRequest, requestDataJson);
				}
				// reset the body data
				modifiedRequest.resetInputStream(requestDataJson.toString().getBytes());

				filterChain.doFilter(modifiedRequest, responseWrapper);

			} else {
				// forward the request as is comming from UI.
				filterChain.doFilter(request, responseWrapper);
			}

			if (!common.ofExcludeMediaType(responseWrapper.getHeader("responseType"))) {
				responseArray = responseWrapper.getContentAsByteArray();
				ls_responseData = new String(responseArray);
			} else {
//				//ls_responseData =ofGetResponseJson(new JSONArray(), "", "Response excluded.", CommonBase.ST0, "G", "Success")
//						//.toString();
//				;
				// ls_responseData =ofGetResponseJson(new JSONArray(), "", "Response excluded.",
				// CommonBase.ST0, "G", "common.success_msg").toString();
				ls_responseData = commonBase
						.ofGetResponseJson(new JSONArray(), "", "Response excluded.", commonBase.ST0, "G", "")
						.toString();

			}
		} catch (Exception exception) {
			lb_errorGenerated = true;
			ls_responseData = commonBase.getExceptionMSg(exception, LOGGER, loggerImpl, "IN:PreRequestProcess",
					"(ENP673)", "Error in pre processing of request.", null);

		} finally {
			/* stop the profiler and print logs. */
			loggerImpl.stopAndPrintMainOptLogs(LOGGER, "Total API performance log track", "IN:PreRequestProcess");
			loggerImpl.info(reqResLogger, ls_responseData, "Response Data-Response");

			/*
			 * Must clear the set data in MDC else same data will be used again for same
			 * thread when start in thread pool.
			 */
			MDC.clear();

			// set the response data in response object
			if (!common.ofExcludeMediaType(responseWrapper.getHeader("responseType")) || lb_errorGenerated) {
				responseWrapper.of_set_data(ls_responseData);
				responseWrapper.copyBodyToResponse(common.getMediaTypeFromResType(""));
			} else {
				responseWrapper
						.copyBodyToResponse(common.getMediaTypeFromResType(responseWrapper.getHeader("responseType")));
			}
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return EXCLUDE_URL.stream().anyMatch(exclude -> request.getServletPath().contains(exclude));
	}

//
//	private boolean isUserAuthService(HttpServletRequest request) throws ServletException {	
//		String ls_ServletPath=request.getServletPath();
//
//		if(StringUtils.isNotBlank(ls_ServletPath)){
//			return ls_ServletPath.startsWith("userAuthenticationServiceAPI") || ls_ServletPath.startsWith("/userAuthenticationServiceAPI"); 
//		}else {
//			return false;
//		}		
//	}

//	private String ofGetRequestFrom(JSONObject requestData,HttpServletRequest request) {
//
//		String ls_requestFrom=request.getHeader("REQUEST_FROM");
//		if(StringUtils.isBlank(ls_requestFrom)) {			
//			ls_requestFrom=requestData.optString("CHANNEL","");
//		}
//		if("I".equals(ls_requestFrom)) {
//			ls_requestFrom="Web";
//		}else if("M".equals(ls_requestFrom)) {
//			ls_requestFrom="MB";
//		}
//
//		return ls_requestFrom;
//	}

	private String getIpDetail(HttpServletRequest request) {
		String ls_ipAddress = request.getHeader("X-Real-IP");

		if (StringUtils.isBlank(ls_ipAddress)) {
			ls_ipAddress = request.getRemoteAddr();
		}
		return ls_ipAddress;
	}

	private String getHostName(String ipAddr) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ipAddr);
			String hostName = inetAddress.getHostName();
			if (StringUtils.isBlank(hostName)) {
				return ipAddr;
			}
			return hostName;
		} catch (Exception e) {
			return ipAddr;
		}
	}

	/**
	 * Thsi method used for set the data from request body to request parameter.
	 * 
	 * @param request         Actual request
	 * @param modifiedRequest Modified request object
	 * @param requestDataJson request JSON
	 * @exception in case exception is generated then it throws exception.
	 */
	private void doSetPreauthorizedData(HttpServletRequest request, RequestWrapper modifiedRequest,
			JSONObject requestDataJson) throws JsonMappingException, JsonProcessingException {

		// get the key list from request data.
		Set<String> keysList = requestDataJson.keySet();
		HashMap<String, String[]> authorizedReqData = new HashMap<>();

		String[] ls_methodName = new String[1];
		String[] ls_grantType = new String[1];
		String[] ls_clientId = new String[1];
		String[] ls_clientSecretKey = new String[1];

		ls_grantType[0] = configCredentials.getProperty("grant_type").toString();
		ls_clientId[0] = configCredentials.getProperty("client_id").toString();
		ls_clientSecretKey[0] = configCredentials.getProperty("client_secret").toString();

		authorizedReqData.put("grant_type", ls_grantType);
		authorizedReqData.put("client_id", ls_clientId);
		authorizedReqData.put("client_secret", ls_clientSecretKey);

		ls_methodName[0] = request.getMethod();
		authorizedReqData.put("_method", ls_methodName);

		for (String keyName : keysList) {
			String[] keyValueList = new String[1];
			keyValueList[0] = requestDataJson.getString(keyName);
			authorizedReqData.put(keyName, keyValueList);
		}

		// put the data in request parameter
		modifiedRequest.putParameter(authorizedReqData);
	}
}