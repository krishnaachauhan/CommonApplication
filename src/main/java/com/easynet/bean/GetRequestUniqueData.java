
package com.easynet.bean;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.easynet.impl.JSONObjectImpl;

/**
 * @author Maulik
 *
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GetRequestUniqueData implements ApplicationContextAware {

	private long uniqueNumber;
	private String langCode;
	Profiler profiler;
	private String ls_action;
	List<Profiler> childprofiler;
	private HttpServletRequest request;

	private JSONObject requestCommonDataJson;

	private String userName;
	private String userRole;
	private String browserFingerprint;
	private String machineName;
	private String machineIP;
	private String baseCompCode;
	private String baseBranchCode;
	private String compCode;
	private String branchCode;
	
	private String workingDate;
	private JSONObjectImpl loginUserDetailsJson;
	private String throughChannel;

	public JSONObject getRequestCommonDataJson() {
		return requestCommonDataJson;
	}

	public void setRequestCommonDataJson(JSONObject requestCommonDataJson) {
		this.requestCommonDataJson = requestCommonDataJson;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public GetRequestUniqueData() {
		uniqueNumber = System.nanoTime();
		childprofiler = new ArrayList<Profiler>();
	}

	public long getUniqueNumber() {
		return uniqueNumber;
	}

	public void setUniqueNumber(long uniqueNumber) {
		this.uniqueNumber = uniqueNumber;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	public List<Profiler> getChildprofiler() {
		return childprofiler;
	}

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		// store ApplicationContext reference to access required beans later on
		GetRequestUniqueData.context = applicationContext;
	}

	public static <T extends Object> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	public String getLs_action() {
		return ls_action;
	}

	public void setLs_action(String ls_action) {
		this.ls_action = ls_action;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getBrowserFingerprint() {
		return browserFingerprint;
	}

	public void setBrowserFingerprint(String browserFingerprint) {
		this.browserFingerprint = browserFingerprint;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public JSONObjectImpl getLoginUserDetailsJson() {
		return loginUserDetailsJson;
	}

	public void setLoginUserDetailsJson(JSONObject jsonObject) {
		this.loginUserDetailsJson = (JSONObjectImpl) jsonObject;
	}

	public String getMachineIP() {
		return machineIP;
	}

	public void setMachineIP(String machineIP) {
		this.machineIP = machineIP;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getWorkingDate() {
		return workingDate;
	}

	public void setWorkingDate(String workingDate) {
		this.workingDate = workingDate;
	}

	public String getBaseCompCode() {
		return baseCompCode;
	}

	public void setBaseCompCode(String baseCompCode) {
		this.baseCompCode = baseCompCode;
	}

	public String getBaseBranchCode() {
		return baseBranchCode;
	}

	public void setBaseBranchCode(String baseBranchCode) {
		this.baseBranchCode = baseBranchCode;
	}

	public String getCompCode() {
		return compCode;
	}

	public void setCompCode(String compCode) {
		this.compCode = compCode;
	}

	public String getThroughChannel() {
		return throughChannel;
	}

	public void setThroughChannel(String throughChannel) {
		this.throughChannel = throughChannel;
	}

}
