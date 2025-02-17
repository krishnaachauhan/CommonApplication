package com.easynet.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="intcharge")
public class IntChargeCheckData {
	
	public static class CheckData {
		public String tranDesc;
		public String tranCode;
				
		public String getTranDesc() {
			return tranDesc;
		}
		public void setTranDesc(String tranDesc) {
			this.tranDesc = tranDesc;
		}
		public String getTranCode() {
			return tranCode;
		}
		public void setTranCode(String tranCode) {
			this.tranCode = tranCode;
		}
		@Override
		public String toString() {
			return "CheckData [tranDesc=" + tranDesc + ", tranCode=" + tranCode + "]";
		}		
	}
	
	private List<CheckData> checkdata = new ArrayList<CheckData>();

	public List<CheckData> getCheckdata() {
		return checkdata;
	}
}
