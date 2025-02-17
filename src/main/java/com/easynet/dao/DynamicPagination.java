package com.easynet.dao;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.easynet.dao.SelectData;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.util.CommonBase;
import com.easynet.util.common;

/**
 * This class is used for dyanmic  pagination .
 * 
 * @author Jay Khimsuriya
 * @since 08/02/2024
 * @modifiedBy Chandan Chauhan
 */
@Repository
public class DynamicPagination extends CommonBase {

	@Autowired
	private SelectData selectData;

	public String getDynamicPaginationData(JSONObject requestJson, String as_query, Object... objects) throws Exception {
		return getDynamicPaginationData(false, requestJson, as_query, objects);
	}

public String getDynamicPaginationData(boolean isQueryChange,JSONObject requestJson,String as_query,Object... objects ) throws Exception {
		
		JSONArrayImpl orderByJList ;
		JSONObjectImpl filterJson;
		JSONArrayImpl filterJList;
		JSONObjectImpl queryJson;
		JSONObjectImpl orderJson;
		JSONObjectImpl countJson;
		JSONObjectImpl mergeJson;
		
		String ls_orderAccessor = StringUtils.EMPTY;
		String ls_orderValue = StringUtils.EMPTY;
		String ls_filterAccessor = StringUtils.EMPTY;
		String ls_filterCondition = StringUtils.EMPTY;
		String ls_filterType = StringUtils.EMPTY;
		String ls_filterValue = StringUtils.EMPTY;
		String ls_fromRow = StringUtils.EMPTY;
		String ls_toRow = StringUtils.EMPTY;
		String ls_count = StringUtils.EMPTY;
		String ls_countRes = StringUtils.EMPTY;
		String ls_queryRes = StringUtils.EMPTY;
		
		int orderLen;
		int filterlen;
		
		StringBuffer sbQueryCountData = new StringBuffer();
		StringBuffer sbQueryData = new StringBuffer();
		StringBuffer orderBuffer = new StringBuffer();
		StringBuffer filterBuffer = new StringBuffer();	
		
		boolean lb_pagination = false;
		boolean lb_isDate = false;
		boolean lb_truncRequired = false;
		
		Object[] objArgu;
		Object[] objWhere;
		Object[] objArg2;
		
		lb_pagination = requestJson.optBoolean("IS_PAGE",false);
		
		if(!lb_pagination) {
			return selectData.getSelectData(as_query,objects);
		}
		
		ls_fromRow = requestJson.getString("FROM_ROW");
		ls_toRow = requestJson.getString("TO_ROW");
		
		sbQueryCountData.append("SELECT COUNT(0) AS TOTAL_ROWS FROM (");
		sbQueryData.append("SELECT ROWNUM AS ROW_SR_CD_ , RPT.* FROM ( SELECT * FROM (");
		
		filterJList = (JSONArrayImpl) requestJson.getJSONArray("FILTER_CONDITIONS");
		orderByJList = (JSONArrayImpl) requestJson.getJSONArray("ORDERBY_COLUMNS");
		
		filterlen = filterJList.length();
		
		List<Object> objList =new ArrayList<>();
		
		for(int i=0;i<filterlen;i++) {
			
			filterJson = filterJList.getJSONObject(i);
			
			ls_filterAccessor = filterJson.getString("ACCESSOR");
			ls_filterCondition = filterJson.getString("CONDITION");
			ls_filterType = filterJson.optString("TYPE","string");
			
			if(filterJson.get("VALUE") instanceof String ? (StringUtils.EMPTY.equals(filterJson.getString("VALUE"))?true:false): false){
				continue; 
			}
			
			if(!"between".equals(ls_filterCondition) && !"in".equals(ls_filterCondition)) {
				ls_filterValue = filterJson.getString("VALUE");
			}else {
				if(filterJson.getJSONArray("VALUE").length()==0) { continue; }
			}
			
			if(i==0) {
				filterBuffer.append(" WHERE ");
			}else {
				filterBuffer.append(" AND ");
			}
			if("DATETIME".equalsIgnoreCase(ls_filterType)) {
				lb_isDate = true;
			}
			if("DATE".equalsIgnoreCase(ls_filterType)) {
				filterBuffer.append("TRUNC(");
				filterBuffer.append(ls_filterAccessor);
				filterBuffer.append(")");
				lb_isDate = true;
				lb_truncRequired = true;
			}else {
				if(isQueryChange && "USER_NAME".equals(ls_filterAccessor))
				{
					filterBuffer.append("USER_ID");
				}else {
					filterBuffer.append(ls_filterAccessor);
				}
				
			}
			
			if(isQueryChange && "USER_NAME".equals(ls_filterAccessor))
			{
				ls_filterValue = ls_filterValue.toUpperCase();
				if("contains".equals(ls_filterCondition)) {
					filterBuffer.append(" IN  ");
					filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) LIKE ? )");
					objList.add("%"+ls_filterValue+"%");
				}else if("equal".equals(ls_filterCondition)) {
					filterBuffer.append(" IN  ");
					filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) = ? )");
					objList.add(ls_filterValue);
				}else if("startsWith".equals(ls_filterCondition)) {
					filterBuffer.append(" IN  ");
					filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) LIKE ? )");
					objList.add(ls_filterValue+"%");
				}else if("endsWith".equals(ls_filterCondition)) {
					filterBuffer.append(" IN  ");
					filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) LIKE ? )");
					objList.add("%"+ls_filterValue);
				}else if("between".equals(ls_filterCondition)) {
					if(lb_isDate) {
						filterBuffer.append(" BETWEEN ? AND TRUNC? ");
						objList.add(getSqlDateFromString(filterJson.getJSONArray("VALUE").get(0).toString()));
						objList.add(getSqlDateFromString(filterJson.getJSONArray("VALUE").get(1).toString()));
					}else {
						filterBuffer.append(" BETWEEN ? AND ? ");
						objList.add(filterJson.getJSONArray("VALUE").get(0).toString());
						objList.add(filterJson.getJSONArray("VALUE").get(1).toString());
					}
				}else if("in".equals(ls_filterCondition)) {
					
						filterBuffer.append(" IN  ");
						filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) IN ("+StringUtils.repeat("?",",",filterJson.getJSONArray("VALUE").length())+") )");
						for(int j=0 ;j<filterJson.getJSONArray("VALUE").length();j++) {
							objList.add(filterJson.getJSONArray("VALUE").get(j).toString());
						}
					
				}else {
					filterBuffer.append(" IN  ");
					filterBuffer.append(" (SELECT E.USER_ID FROM IMB_USER_MST E WHERE UPPER(TRIM(E.USER_NAME)) = ? )");
					objList.add(ls_filterValue);
				}
			}else if("contains".equals(ls_filterCondition)) {
				filterBuffer.append(" LIKE ?");
				objList.add("%"+ls_filterValue+"%");
			}else if("equal".equals(ls_filterCondition)) {
				if(lb_isDate) {
					filterBuffer.append(lb_truncRequired?" = TRUNC(?) ":" = ? ");
					objList.add(getSqlDateFromString(ls_filterValue));
				}else {
					filterBuffer.append(" = ? ");
					objList.add(ls_filterValue);
				}
			}else if("startsWith".equals(ls_filterCondition)) {
				filterBuffer.append(" LIKE ?");
				objList.add(ls_filterValue+"%");
			}else if("endsWith".equals(ls_filterCondition)) {
				filterBuffer.append(" LIKE ?");
				objList.add("%"+ls_filterValue);
			}else if("between".equals(ls_filterCondition)) {
				if(lb_isDate) {
					if(lb_truncRequired) {
						filterBuffer.append(" BETWEEN TRUNC(?) AND TRUNC(?) ");
					}else
					{
						filterBuffer.append(" BETWEEN ? AND ? ");
					}
					objList.add(getSqlDateFromString(filterJson.getJSONArray("VALUE").get(0).toString()));
					objList.add(getSqlDateFromString(filterJson.getJSONArray("VALUE").get(1).toString()));
				}else {
					filterBuffer.append(" BETWEEN ? AND ? ");
					objList.add(filterJson.getJSONArray("VALUE").get(0).toString());
					objList.add(filterJson.getJSONArray("VALUE").get(1).toString());
				}
			}else if("in".equals(ls_filterCondition)) {
				if(lb_isDate) {
					filterBuffer.append(" IN ( "+ StringUtils.repeat(lb_truncRequired?"TRUNC(?)":"?",",",filterJson.getJSONArray("VALUE").length())+" )");
					for(int j=0 ;j<filterJson.getJSONArray("VALUE").length();j++) {
						objList.add(getSqlDateFromString(filterJson.getJSONArray("VALUE").get(j).toString()));
					}
				}else {
					filterBuffer.append(" IN ( "+ StringUtils.repeat("?",",",filterJson.getJSONArray("VALUE").length())+" )");
					for(int j=0 ;j<filterJson.getJSONArray("VALUE").length();j++) {
						objList.add(filterJson.getJSONArray("VALUE").get(j).toString());
					}
				}
			}else {
				if(lb_isDate) {
					filterBuffer.append(lb_truncRequired?" = TRUNC(?) ":" = ? ");
					objList.add(getSqlDateFromString(ls_filterValue));
				}else {
					filterBuffer.append(" = ? ");
					objList.add(ls_filterValue);
				}
			}
			lb_isDate = false;
		}
		
		sbQueryCountData.append(as_query);
		sbQueryData.append(as_query);
		sbQueryCountData.append(") ");
		sbQueryData.append(") ");
		sbQueryCountData.append(filterBuffer.toString());
		sbQueryData.append(filterBuffer.toString());
		
		orderLen = orderByJList.length();

		if(orderLen > 0) {
			orderBuffer.append(" ORDER BY ");
		}		
		
		for(int i=0;i<orderLen;i++) {
			
			orderJson = orderByJList.getJSONObject(i);
			
			ls_orderAccessor = orderJson.getString("ACCESSOR");
			ls_orderValue = orderJson.getString("VALUE");
			
			if(i>0) {
				orderBuffer.append(",");
			}
			orderBuffer.append(ls_orderAccessor);
			orderBuffer.append(" ");
			orderBuffer.append(ls_orderValue);
		}
		
		sbQueryData.append(orderBuffer.toString());
		
		sbQueryData.append(" ) RPT OFFSET (:FROM_ROW - 1) ROWS FETCH NEXT (:TO_ROW-:FROM_ROW)+1 ROWS ONLY");
		
		objArgu = new Object[objects.length+objList.size()];
		objWhere = new Object[objList.size()];
		for(int i=0;i<objWhere.length;i++) {
			objWhere[i]=objList.get(i);
		}
		System.arraycopy(objects, 0, objArgu, 0, objects.length);
		System.arraycopy(objWhere, 0, objArgu, objects.length, objWhere.length);
		
		ls_countRes = selectData.getSelectData(sbQueryCountData.toString(),objArgu);

		countJson = common.ofGetJsonObject(ls_countRes);
		
		if(!isSuccessStCode(countJson.getString("STATUS"))) {
			return ls_countRes;
		}
		
		ls_count = common.ofGetJsonObject(countJson.getJSONArray("RESPONSE").toString()).getString("TOTAL_ROWS");
		
		objArg2 = new Object[objArgu.length+3];
		
		System.arraycopy(objArgu, 0, objArg2, 0, objArgu.length);
		
		objArg2[objArgu.length] = ls_fromRow;
		objArg2[objArgu.length+ 1] = ls_toRow;
		objArg2[objArgu.length+ 2] = ls_fromRow;

		ls_queryRes = selectData.getSelectData(sbQueryData.toString(),objArg2); 
		
		queryJson = common.ofGetJsonObject(ls_queryRes);

		if(!isSuccessStCode(queryJson.getString("STATUS"))) {
			return ls_queryRes;
		}
		
		mergeJson = new JSONObjectImpl();
		mergeJson.put("COUNT", ls_count);
		mergeJson.put("REPORT_DATA", queryJson.getJSONArray("RESPONSE"));
		
		return common.ofGetResponseJson(new JSONArray().put(mergeJson), "", "", ST0,"G","").toString();
		
	}

}
