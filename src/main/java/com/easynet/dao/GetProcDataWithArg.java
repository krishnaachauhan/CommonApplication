package com.easynet.dao;

import java.util.Base64;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;

@Repository
public class GetProcDataWithArg extends GetDataDB {

	static Logger LOGGER = LoggerFactory.getLogger(GetProcDataWithArg.class);

	@Autowired
	private GetProcData getProcData;

	@Autowired
	private GetFuctionData getFuctionData;

	@Autowired
	private SelectData selectData;

	@Autowired
	private UpdateData updateData;
	
	@Autowired
	private DynamicPagination dynamicPagination;

	private static final String LS_VARCHAR = "STRING";
	private static final String LS_INTEGER = "INTEGER";
	private static final String LS_JSON = "JSON";

	public String getDataFromConfig(String as_proc_name, String req_data, String as_config, String lsGetType,
			String as_isCompressed,String ls_pagination) {

		Object[] configData = null;
		LoggerImpl loggerImpl = null;
		int li_confiDataLength;
		int order = 0;
		JSONObjectImpl confiDataJsonimpl;
		JSONObjectImpl resJson;
		JSONObjectImpl reqDataJson;
		JSONArrayImpl confiDataJlist;
		String ls_ParaValue = null;
		String ls_type = StringUtils.EMPTY;
		String key_name = StringUtils.EMPTY;
		String ls_responseData = StringUtils.EMPTY;
		String ls_compressData = StringUtils.EMPTY;
		String ls_status = StringUtils.EMPTY;

		try {
			loggerImpl = new LoggerImpl();

			reqDataJson = new JSONObjectImpl(req_data);

			confiDataJlist = new JSONArrayImpl(as_config);
			li_confiDataLength = confiDataJlist.length();

			if (li_confiDataLength > 0) {

				configData = new Object[li_confiDataLength];

				for (Object confiDataJson : confiDataJlist) {

					confiDataJsonimpl = new JSONObjectImpl((JSONObjectImpl) confiDataJson);

					// Integer order= Integer.parseInt(confiDataJsonimpl.getString("WHERE_SEQ_ID"));
					ls_type = confiDataJsonimpl.getString("REQ_PARA_TYPE");
					key_name = confiDataJsonimpl.getString("REQ_PARA");

					if (LS_JSON.equals(ls_type)) {
						configData[order] = reqDataJson.toString();
					} else if (LS_VARCHAR.equals(ls_type)) {
						configData[order] = reqDataJson.getString(key_name);
						;
					} else if (LS_INTEGER.equals(ls_type)) {
						ls_ParaValue = reqDataJson.getString(key_name);
						configData[order] = Integer.parseInt(ls_ParaValue);
					} else {
						configData[order] = reqDataJson.getString(key_name);
					}
					order++;
				}
			}

			if (GetSelectDynamicData.LS_PROC.equals(lsGetType)) {
				return getProcData.getClobData(as_proc_name, true, configData);
			} else if (GetSelectDynamicData.LS_PROCWITHCURSOR.equals(lsGetType)) {
				return getProcData.getCursorData(as_proc_name, true, configData);
			} else if (GetSelectDynamicData.LS_FUNCWITHCURSOR.equals(lsGetType)) {
				return getFuctionData.getCursorData(as_proc_name, configData);
			} else if (GetSelectDynamicData.LS_SELECT.equals(lsGetType) && "Y".equalsIgnoreCase(as_isCompressed)) {
				
				
				if (ls_pagination.equals("Y")) {
					if (Objects.isNull(configData)) {
						configData = new Object[0];
					}
					ls_responseData = dynamicPagination.getDynamicPaginationData(reqDataJson, as_proc_name, configData);
				} else {
					ls_responseData = selectData.getSelectData(as_proc_name, configData);
				}
				
				
				//ls_responseData = selectData.getSelectData(as_proc_name, configData);
			} else if (GetSelectDynamicData.LS_SELECT.equals(lsGetType)) {
				
				if (ls_pagination.equals("Y")) {
					if (Objects.isNull(configData)) {
						configData = new Object[0];
					}
					return dynamicPagination.getDynamicPaginationData(reqDataJson, as_proc_name, configData);
				} else {
					return selectData.getSelectData(as_proc_name, configData);
				}
				//return selectData.getSelectData(as_proc_name, configData);
				
				
			} else if (GetSelectDynamicData.LS_UPDATE.equals(lsGetType)) {
				return updateData.doUpdateData(as_proc_name, configData);
			} else if (GetSelectDynamicData.LS_PROCEXECUTE.equals(lsGetType)) {
				return getProcData.procExecuteOnly(as_proc_name, true, configData);

			}else {
			ls_responseData = ofGetFailedMSg("common.invalid_req_data", "", "Please Enter Proper TYPE.Invalid request.",
					"");}

			resJson = common.ofGetJsonObject(ls_responseData);
			ls_status = resJson.getString("STATUS");

			if (!isSuccessStCode(ls_status) || !"Y".equalsIgnoreCase(as_isCompressed)) {
				return ls_responseData;
			}

			ls_compressData = Base64.getEncoder()
					.encodeToString(common.compress(resJson.getJSONArray("RESPONSE").toString()));

			return (common.ofGetResponseJson(new JSONArray().put(new JSONObject().put("DATA", ls_compressData)), "", "",
					ST0, "G", "").put("ISDATACOMPRESSED", "Y")).toString();

		} catch (

		Exception exception) {
			return getExceptionMSg(exception, LOGGER, loggerImpl, "IN:GetProcDataWithArg", "(ENP020)");
		}

	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}
}
