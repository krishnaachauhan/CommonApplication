package com.easynet.util;

import org.apache.commons.lang3.StringUtils;

public class ConstantValue {

	private static final String FINACLE="FINACLE";
	private static final String TRANZWARE="TRANZWARE";
	private static final String ABABIL="ABABIL";
	private static final String CARD="C";
	private static final String ACCOUNT="A";
	private static final String DEBITCARD="DEBIT CARD";
	private static final String SUPPLEMENTARYCARD="SUPPLEMENTARY CARD";
	private static final String CREDITCARD="CREDIT CARD";
	private static final String PREPAIDCARD="PREPAID CARD";
	
	public static final String DB_API_AUTHETICATION_SERVICE="PROC_AUTHETICATION_MSERVICE";
	public static final String DB_API_CARD_SERVICE="PROC_CARD_MSERVICE";
	public static final String DB_API_COMMON_SERVICE="PROC_COMMON_MSERVICE";
	public static final String DB_API_CUSTOMER_REQUEST_SERVICE="PROC_CUSTOMER_REQUEST_MSERVICE";
	public static final String DB_API_FUND_TRF_SERVICE="PROC_FUND_TRF_MSERVICE";
	public static final String DB_API_MANAGE_PAYEE_SERVICE="PROC_MANAGE_PAYEE_MSERVICE";
	public static final String DB_API_OTHER_FINACIAL_SERVICE="PROC_OTHER_FINACIAL_MSERVICE";
	public static final String DB_API_PAYMENT_GATEWAY_SERVICE="PROC_PAYMENT_GATEWAY_MSERVICE";
	public static final String DB_API_QR_SERVICE="PROC_QR_MSERVICE";
	public static final String DB_API_STATEMENT_SERVICE="PROC_STATEMENT_MSERVICE";
	public static final String DB_API_PROFILE_UPDATE_SERVICE="PROC_PROFILE_UPDATE_MSERVICE";
	public static final String DB_API_USER_AUTH_SERVICE="PROC_USER_AUTH_MSERVICE";
	public static final String DB_BILL_UTILITY_SERVICE="PROC_BILL_UTILITY_MSERVICE";
  
	public static boolean isFinalce(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (FINACLE.equals(as_str))?true:false;

	}

	public static boolean isTranzware(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (TRANZWARE.equals(as_str))?true:false;

	}

	public static boolean isAbabil(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (ABABIL.equals(as_str))?true:false;

	}

	public static boolean isCard(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (CARD.equals(as_str))?true:false;

	}
	public static boolean isAccount(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (ACCOUNT.equals(as_str))?true:false;

	}
	public static boolean isDebitCard(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (DEBITCARD.equals(as_str))?true:false;

	}
	
	public static boolean isSupplementaryCard(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (SUPPLEMENTARYCARD.equals(as_str))?true:false;

	}
	
	public static boolean isCreditCard(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (CREDITCARD.equals(as_str))?true:false;

	}
	
	public static boolean isPrepaidCard(String as_str) {
		as_str=StringUtils.upperCase(as_str);
		return (PREPAIDCARD.equals(as_str))?true:false;

	}
	
	
	/**
	 *This method share the transaction type as per from source and to source.
	 *@param as_fromSource source type of transaction.
	 *@param as_toSource destrination type of transaction.
	 *@return string of transaction type. 
	 *@throws Exception if source combination are not valid then this method thows exception.
	 * 
	 * **/
	public static String getTranType(String as_fromSource,String as_toSource) throws Exception {

		if((isFinalce(as_fromSource) && isFinalce(as_toSource)) || 
				(isAbabil(as_fromSource) && isAbabil(as_toSource))||
				(isFinalce(as_fromSource) && isAbabil(as_toSource))||
				(isAbabil(as_fromSource) && isFinalce(as_toSource))) {
			return "a2a";

		}else if((isFinalce(as_fromSource) && isTranzware(as_toSource))||
				(isAbabil(as_fromSource) && isTranzware(as_toSource))){
			return "a2c";	
			
		}else if((isTranzware(as_fromSource) && isFinalce(as_toSource))||
				(isTranzware(as_fromSource) && isAbabil(as_toSource))) {
			return "c2a";
			
		}else if((isTranzware(as_fromSource) && isTranzware(as_toSource))) {
			return "c2c";
			
		}else{
			throw new Exception("Invalid Source combination.");			
		}
	}
	
	/**
	 *This method share the other bank transaction type as per from source and to source.
	 *@param as_fromSource source type of transaction.
	 *@param as_toSource destrination type of transaction.
	 *@return string of transaction type. 
	 *@throws Exception if source combination are not valid then this method thows exception.
	 * 
	 * **/
	public static String getOtherTranType(String as_fromSource,String as_toSource) throws Exception {

		if((isFinalce(as_fromSource) && isAccount(as_toSource)) || 
				(isAbabil(as_fromSource) && isAccount(as_toSource))) {
			return "a2a";

		}else if((isFinalce(as_fromSource) && isCard(as_toSource))||
				(isAbabil(as_fromSource) && isCard(as_toSource))){
			return "a2c";	
			
		}else if((isTranzware(as_fromSource) && isAccount(as_toSource))) {
			return "c2a";
			
		}else if((isTranzware(as_fromSource) && isCard(as_toSource))) {
			return "c2c";
			
		}else{
			throw new Exception("Invalid other Bank Source combination.");			
		}
	}


	public static String getFinacle() {
		return FINACLE;
	}

	public static String getTranzware() {
		return TRANZWARE;
	}

	public static String getAbabil() {
		return ABABIL;
	}

	public static String getCard() {
		return CARD;
	}

	public static String getAccount() {
		return ACCOUNT;
	}

	public static boolean isAccountData(String ls_source) {
		
		if(isFinalce(ls_source) || isAbabil(ls_source)) {
			return true;
		}else {
			return false;
		}
	}

}
