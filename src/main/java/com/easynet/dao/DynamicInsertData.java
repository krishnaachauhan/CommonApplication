package com.easynet.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.acute.dao.InvalidValueException;
import com.easynet.bean.GetRequestUniqueData;
import com.easynet.exception.CustomeException;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.util.AESEncryption;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;
import static com.easynet.util.ConstantKeyValue.*;

@Repository
public class DynamicInsertData extends GetDataDB {

	@Autowired
	private InsertData insertdata;

	@Autowired
	private SelectData selectData;

	@Autowired
	private GetRequestUniqueData getRequestUniqueData;

	private static final String ENTERED_BY = "ENTERED_BY";
	private static final String ENTERED_DATE = "ENTERED_DATE";
	private static final String MACHINE_NM = "MACHINE_NM";

	private static final String LAST_ENTERED_BY = "LAST_ENTERED_BY";
	private static final String LAST_MODIFIED_DATE = "LAST_MODIFIED_DATE";
	private static final String LAST_MACHINE_NM = "LAST_MACHINE_NM";

	private static final String CONFIRMED = "CONFIRMED";

	private static final String VERIFIED_BY = "VERIFIED_BY";
	private static final String VERIFIED_DATE = "VERIFIED_DATE";
	private static final String VERIFIED_MACHINE_NM = "VERIFIED_MACHINE_NM";

	/**
	 * Increment the primary key using Sequence
	 */
	public static final int PARA_USESEQUENCES = 0;
	/**
	 * Increment the primary key using Max Value
	 */
	public static final int PARA_USE_MAXCD = 1;
	/**
	 * Increment the primary key using Requested Data
	 */
	public static final int PARA_NOSEQUENCES = 2;

	/**
	 * Confirmation is Auto(Update "Y")
	 */
	public static final int PARA_CONFIRM_AUTO = 0;
	/**
	 * Confirmation is Manual(Send in Request)
	 */
	public static final int PARA_CONFIRM_MANUAL = 1;
	/**
	 * Not Auto Confirm(Always Update "N")
	 */
	public static final int PARA_CONFIRM_NO = 2;

	private static final String SELECTMOBILEDATAENC = "select FUNC_GET_ENCR_DCR_MOBILE_NO(?,'E') as ENCURPTMOBDATA from dual";
	private static final String SELECTCARDDATAENC = "select FUNC_GET_ENCRYPT(?,'CE') as ENCURPTCARDDATA from dual";
	ArrayList<String> setMobileEncryptData = new ArrayList<>();
	ArrayList<String> setCardEncryptData = new ArrayList<>();
	private int CONFIRMATION_PARA = PARA_CONFIRM_NO;

	private String SCHEMANAME = "ENFINITY";

	private boolean isSingleMstMultiDtl = false;

	private boolean isPrimaryKeyUpdateAllowed = false;

	private boolean isInsertVerificationFields = false;

	protected boolean isInsertAuditDataAllowed = false;

	private static String AUDITINSERTQUERY = "INSERT INTO EASY_BANK.ACT_AUDIT_TRAIL (COMP_CD, BRANCH_CD, TRAN_CD, SR_CD, \n"
			+ "TABLE_KEY_VALUE1, TABLE_KEY_VALUE2, TABLE_KEY_VALUE3, ACTION, TABLE_NAME, COLUMN_NAME, COLUMN_LABEL, \n"
			+ "COLUMN_STYLE, OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_DATE, MACHINE_NM, TABLE_CHAR_KEY1, TABLE_CHAR_KEY2, \n"
			+ "TABLE_CHAR_KEY3, REQUEST_CD, THROUGH_CHANNEL, VERIFIED_BY, VERIFIED_DATE, VERIFIED_MACHINE_NM)\n"
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?,?,?,?,?,?,?,?,?)";

	private static final String AUDITINSERTQUERYFORNETBANKING = "INSERT INTO ACT_AUDIT_TRAIL (COMP_CD, BRANCH_CD, TRAN_CD, SR_CD, \n"
			+ " TABLE_KEY_VALUE1, TABLE_KEY_VALUE2, TABLE_KEY_VALUE3, ACTION, TABLE_NAME, COLUMN_NAME, COLUMN_LABEL, \n"
			+ " COLUMN_STYLE, OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_DATE, MACHINE_NM, TABLE_CHAR_KEY1, TABLE_CHAR_KEY2, \n"
			+ " TABLE_CHAR_KEY3, REQUEST_CD, THROUGH_CHANNEL, VERIFIED_BY, VERIFIED_DATE, VERIFIED_MACHINE_NM)\n"
			+ " VALUES (:COMP_CD,:BRANCH_CD,:TRAN_CD,:SR_CD,:TABLE_KEY_VALUE1,:TABLE_KEY_VALUE2,:TABLE_KEY_VALUE3,:ACTION, "
			+ " :TABLE_NAME,:COLUMN_NAME,:COLUMN_LABEL,:COLUMN_STYLE,:OLD_VALUE,:NEW_VALUE,:MODIFIED_BY,SYSDATE, " // WORKING_DATE
			+ " :MACHINE_NM,:TABLE_CHAR_KEY1,:TABLE_CHAR_KEY2,:TABLE_CHAR_KEY3,:REQUEST_CD,:THROUGH_CHANNEL,:VERIFIED_BY, "
			+ " :VERIFIED_DATE, :VERIFIED_MACHINE_NM)";

	/**
	 * 
	 * Insert Data
	 *
	 * @param connection  a Database Connection Object;
	 * @param jreq        as JSONObject of request;
	 * @param jUser       as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param seqPara     as Sequences Use
	 *                    Parameter(0-USESEQUENCES,1-USE_MAXCD,2-NOSEQUENCES);
	 * @param sequnceName as Sequence Name()
	 * @param asTableName as TableName;
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 * 
	 */
	public String InsertData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, int seqPara,
			String sequnceName, String asTableName, JSONArrayImpl auditDataArray) throws Exception {
		JSONArrayImpl jallColumn = getColumnDefination(asTableName, connection, true);
		JSONObjectImpl jPrimaryKeyDtl = new JSONObjectImpl();
		JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
		String ls_columnLabel = StringUtils.EMPTY;
		JSONObjectImpl columnLabels = null;
		Object[] obj = new Object[jallColumn.length()];
		StringBuffer sbQueryData = new StringBuffer();
		StringBuffer sbQuery = new StringBuffer();

		// For getting column labels from JSON object to put in auditDataArray

		if (isInsertAuditDataAllowed) {
			columnLabels = jreq.optJSONObject("_LABELS_MASTER");
		}

		sbQuery.append("INSERT INTO " + asTableName + "(");
		sbQueryData.append("(");
		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			String ls_columnName = jkeyColumn.getString("COLUMN_NAME");
			String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
			if ((i + 1) == jallColumn.length()) {
				sbQuery.append(ls_columnName);
				sbQueryData.append("?");
			} else {
				sbQuery.append(ls_columnName + ",");
				sbQueryData.append("?,");
			}
			if (isSingleMstMultiDtl && "YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
					&& jreq.has(ls_columnName) && StringUtils.isNotBlank(String.valueOf(jreq.get(ls_columnName)))) {
				obj[i] = jreq.get(ls_columnName);
			} else if ((seqPara == PARA_USESEQUENCES || seqPara == PARA_USE_MAXCD)
					&& "YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
					&& "NUMBER".equals(jkeyColumn.getString("TYPE_NAME"))) {

				long llTranCd;
				if (isSingleMstMultiDtl && seqPara == PARA_USE_MAXCD) {
					llTranCd = getMaxSrCd(connection, asTableName, ls_columnName, jallColumn, jreq);
					llTranCd = llTranCd + 1;
				} else {
					llTranCd = getMaxCd(connection, asTableName, ls_columnName, seqPara, sequnceName);
				}
				obj[i] = llTranCd;
			} else {

				obj[i] = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
			}

			if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
				jPrimaryKeyDtl.put(ls_columnName, obj[i]);
				// For get primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed) {
					jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDatatype.put(ls_columnName, obj[i]);
				}
			}

			/*
			 * check condition for inserting allowed in audit table and if column is not a
			 * primary key then create audit data array
			 */
			if (isInsertAuditDataAllowed && "NO".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
				// Assign label name according it's column name
				ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName, "") : ls_columnName;
				// Assign new value from object and convert it into string
				String newValue = (obj[i] != null) ? obj[i].toString() : "null";

//				System.out.println("Debug: Adding audit data for column: " + ls_columnName + " with value: " + newValue);
				// Create audit data array
				if (!StringUtils.isEmpty(ls_columnLabel)) {
					createAuditRowData("ADD", asTableName, ls_columnName, ls_columnLabel, "", "", newValue,
							jPrimaryKeyWithDatatype, "M", auditDataArray);
				}
			}
		}

		sbQuery.append(")");
		sbQueryData.append(")");

		String ls_query = sbQuery.toString() + " VALUES " + sbQueryData.toString();

		String ls_resData = insertdata.insertDataWithObj(connection, ls_query, obj);
		JSONObjectImpl insertResDataJson = common.ofGetJsonObject(ls_resData);

		if (!isSuccessStCode(insertResDataJson.getString(STATUS))) {
			connection.rollback();
			return ls_resData;
		}

		return ofGetResponseJson(new JSONArrayImpl().put(jPrimaryKeyDtl), "", "Success.", ST0, "G",
				"common.success_msg").toString();

	}

	public String InsertData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, int seqPara,
			String sequnceName, String asTableName) throws Exception {
		return InsertData(connection, jreq, jUser, seqPara, sequnceName, asTableName, new JSONArrayImpl());
	}

	/**
	 * Insert Data
	 *
	 * @param connection     a Database Connection Object;
	 * @param jreq           as JSONObject of request;
	 * @param jUser          as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param seqPara        as Sequences Use
	 *                       Parameter(0-USESEQUENCES,1-USESEQUENCES,2-NOSEQUENCES);
	 * @param sequnceName    as Sequence Name()
	 * @param asTableName    as Master TableName;
	 * @param asDetailsTable as Details TableName
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 */
	public String InsertData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, int seqPara,
			String sequnceName, String asTableName, String asDetailsTable, JSONArrayImpl auditDataArray)
			throws Exception {
		String lsResponse = InsertData(connection, jreq, jUser, seqPara, sequnceName, asTableName, auditDataArray);
		JSONObjectImpl jobj = common.ofGetJsonObject(lsResponse);
		JSONArrayImpl jallColumn = getColumnDefination(asDetailsTable, connection, true);
		if (isSuccessStCode(jobj.getString(STATUS))) {
			return insertDTLData(connection, jreq.getJSONObject("DETAILS_DATA").getJSONArray("isNewRow"), jUser,
					jobj.getJSONArray("RESPONSE").getJSONObject(0), asDetailsTable, jallColumn, auditDataArray);
		} else {
			return lsResponse;
		}
	}

	public String InsertData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, int seqPara,
			String sequnceName, String asTableName, String asDetailsTable) throws Exception {
		return InsertData(connection, jreq, jUser, seqPara, sequnceName, asTableName, asDetailsTable, null);
	}

	public String insertDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable) throws Exception {
		JSONArrayImpl jallColumn = getColumnDefination(asDetailsTable, connection, true);
		return insertDTLData(connection, jarrReq, jUser, jPrimaryDtl, asDetailsTable, jallColumn);
	}

	public String insertDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn) throws Exception {
		return insertDTLData(connection, jarrReq, jUser, jPrimaryDtl, asDetailsTable, jallColumn, null);
	}

	public String insertDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn, JSONArrayImpl auditDataArray)
			throws Exception {
		// System.out.println("jPrimaryDtl=>"+jPrimaryDtl);
		StringBuffer sbQueryData = new StringBuffer();
		StringBuffer sbQuery = new StringBuffer();
		String ls_columnLabel = StringUtils.EMPTY;
		JSONObjectImpl columnLabels = null;
		// For getting column labels from isNewRow[] for detail table to put in
		// auditDataArray
		if (isInsertAuditDataAllowed) {
			if (jarrReq != null) {
				for (int i = 0; i < jarrReq.length(); i++) {
					JSONObjectImpl objJson = jarrReq.getJSONObject(i);
					if (objJson.has("_LABELS_DETAILS_DATA")) {
						columnLabels = objJson.getJSONObject("_LABELS_DETAILS_DATA");
						jarrReq.remove(i);
					}
				}
			}
		}

		sbQuery.append("INSERT INTO " + asDetailsTable + "(");
		sbQueryData.append("(");
		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			String ls_columnName = jkeyColumn.getString("COLUMN_NAME");
			if ((i + 1) == jallColumn.length()) {
				sbQuery.append(ls_columnName);
				sbQueryData.append("?");
			} else {
				sbQuery.append(ls_columnName + ",");
				sbQueryData.append("?,");
			}

		}
		sbQuery.append(")");
		sbQueryData.append(")");
		String ls_query = sbQuery.toString() + " VALUES " + sbQueryData.toString();
		// System.out.println(ls_query);
		long ll_SrCd = 0L;
		for (int i = 0; i < jarrReq.length(); i++) {
			JSONObjectImpl jDtlRow = jarrReq.getJSONObject(i);
			Object[] obj = new Object[jallColumn.length()];
			JSONObjectImpl jPrimaryKeyWithDataType = new JSONObjectImpl();

			for (int j = 0; j < jallColumn.length(); j++) {
				JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(j);
				String ls_columnName = jkeyColumn.getString("COLUMN_NAME");
				String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
				if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
						// && "NUMBER".equals(jkeyColumn.getString("TYPE_NAME"))
						&& jPrimaryDtl.has(ls_columnName)) {
					obj[j] = jPrimaryDtl.get(ls_columnName);
				} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
						// && "NUMBER".equals(jkeyColumn.getString("TYPE_NAME"))
						&& jDtlRow.has(ls_columnName)) {
					obj[j] = jDtlRow.get(ls_columnName);
				} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
						&& "NUMBER".equals(jkeyColumn.getString("TYPE_NAME"))) {
					if (ll_SrCd == 0) {
						ll_SrCd = getMaxSrCd(connection, asDetailsTable, ls_columnName, jallColumn, jPrimaryDtl);
					}
					ll_SrCd = ll_SrCd + 1;
					obj[j] = ll_SrCd;
				} else {
					obj[j] = getColumnValue(connection, ls_columnName, jDtlRow, jUser, jkeyColumn);
				}
				// System.out.println((1 + i) + "=>" + jkeyColumn.getString("COLUMN_NAME") + "="
				// + obj[j]);

				// For get primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed && "YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
					jPrimaryKeyWithDataType.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDataType.put(ls_columnName, obj[j]);
				}

				/*
				 * Check condition for insert allowed in audit table and if column is not
				 * primary key then create audit data array for every column
				 */
				if (isInsertAuditDataAllowed && "NO".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
					ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName, "") : ls_columnName;
					String newValue = (obj[j] != null) ? obj[j].toString() : "null";

//					System.out.println("Debug: Adding audit data for column: " + ls_columnName + " with value: " + newValue);

					if (!StringUtils.isBlank(ls_columnLabel)) {
						createAuditRowData("ADD", asDetailsTable, ls_columnName, ls_columnLabel, "", "", newValue,
								jPrimaryKeyWithDataType, "M", auditDataArray);
					}
				}
			}

			String ls_resData = insertdata.insertDataWithObj(connection, ls_query, obj);
			JSONObjectImpl insertResDataJson = common.ofGetJsonObject(ls_resData);

			if (!isSuccessStCode(insertResDataJson.getString(STATUS))) {
				connection.rollback();
				return ls_resData;
			}

		}
		return ofGetResponseJson(new JSONArrayImpl().put(jPrimaryDtl), "", "Success", ST0, "G", "common.success_msg")
				.toString();
	}

	/**
	 * Delete Data
	 *
	 * @param connection  a Database Connection Object;
	 * @param jreq        as JSONObject of request;
	 * @param jUser       as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param asTableName as TableName;
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 */
	public String DeleteData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			JSONArrayImpl auditDataArray) throws Exception {
		JSONArrayImpl jallColumn = getColumnDefination(asTableName, connection, true);
		ArrayList<Object> la_Para = new ArrayList<>();
		JSONObjectImpl jPrimaryDtl = new JSONObjectImpl();
		JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
		JSONObjectImpl columnLabels = null;
		String ls_columnLabel = StringUtils.EMPTY;
		String ls_columnName = StringUtils.EMPTY;
		boolean isFirst = true;
		StringBuffer sbQueryData = new StringBuffer();
		StringBuffer sbQuery = new StringBuffer();
		// For getting column labels from a JSON object to put in auditDataArray
		if (isInsertAuditDataAllowed) {
			columnLabels = jreq.optJSONObject("_LABELS_MASTER");
		}
		sbQuery.append("DELETE FROM " + asTableName);
		sbQueryData.append(" WHERE ");
		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
				ls_columnName = jkeyColumn.getString("COLUMN_NAME");
				String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
				if (isFirst) {
					sbQueryData.append(ls_columnName + " = ? ");
					isFirst = false;
				} else {
					sbQueryData.append(" AND " + ls_columnName + " = ? ");
				}
				la_Para.add(jreq.get(ls_columnName));
				jPrimaryDtl.put(ls_columnName, jreq.get(ls_columnName));
				// For get primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed) {
					jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDatatype.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
				}
			}

		}

		/*
		 * check condition for insert allowed in audit table and if column is not
		 * primary key then create audit data array
		 */
		if (isInsertAuditDataAllowed) {
			ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
			createAuditRowData("DEL", asTableName, ls_columnName, ls_columnLabel, "", "", "", jPrimaryKeyWithDatatype,
					"M", auditDataArray);
		}

		sbQuery.append(sbQueryData.toString());
		PreparedStatement pstmt = connection.prepareStatement(sbQuery.toString());
		for (int i = 0; i < la_Para.size(); i++) {
			Object object = la_Para.get(i);
			if (object instanceof Integer) {
				pstmt.setLong((i + 1), Long.valueOf((int) object));
			} else if (object instanceof Long) {
				pstmt.setLong((i + 1), (Long) object);
			} else if (object == null) {
				pstmt.setObject((i + 1), null);
			} else if (object instanceof Double) {
				pstmt.setDouble((i + 1), (double) object);
			} else if (object instanceof Date) {
//				pstmt.setDate((i + 1), (Date) object);
				pstmt.setTimestamp((i + 1), new java.sql.Timestamp(((Date) object).getTime()));
			} else if (object instanceof NClob) {
				pstmt.setNClob((i + 1), (NClob) object);
			} else {
				pstmt.setString((i + 1), (String) object);
			}
		}
		int UpdatedColumn = pstmt.executeUpdate();
		try {
			pstmt.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}
		if (UpdatedColumn != 1) {
			connection.rollback();
//			return ofGetResponseJson(new JSONArrayImpl(), "",
//					"Delete Failed! Invalid Request (" + UpdatedColumn + ").", ST999, "R",
//					"Delete Failed! Invalid Request (" + UpdatedColumn + ").").toString();

			return ofGetResponseJson(new JSONArrayImpl(), "", "Delete Failed! Invalid Request (" + UpdatedColumn + ").",
					ST999, "R", "common.delete_fail", UpdatedColumn).toString();

		}
		return ofGetResponseJson(new JSONArrayImpl(), "", "Success", ST0, "G", "common.success_msg").toString();

	}

	public String DeleteData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName)
			throws Exception {
		return DeleteData(connection, jreq, jUser, asTableName, new JSONArrayImpl());
	}

	/**
	 * Delete Data
	 *
	 * @param connection        a Database Connection Object;
	 * @param jreq              as JSONObject of request;
	 * @param jUser             as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param asTableName       as Master TableName;
	 * @param asDetailTableName as Detail TableName
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 */
	public String DeleteData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			String asDetailTableName, JSONArrayImpl auditDataArray) throws Exception {
		JSONArrayImpl jallColumn = getColumnDefination(asTableName, connection, true);
		ArrayList<Object> la_Para = new ArrayList<>();
		boolean isFirst = true;
		JSONObjectImpl jPrimaryDtl = new JSONObjectImpl();
		JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
		String ls_columnName = StringUtils.EMPTY;
		String ls_columnLabel = StringUtils.EMPTY;
		StringBuffer sbQueryData = new StringBuffer();
		StringBuffer sbQuery = new StringBuffer();
		StringBuffer sbQueryDtl = new StringBuffer();
		JSONObjectImpl columnLabels = null;
		// For getting column labels from a JSON object to put in auditDataArray
		if (isInsertAuditDataAllowed) {
			columnLabels = jreq.optJSONObject("_LABELS_MASTER");
		}
		sbQuery.append("DELETE FROM " + asTableName);
		sbQueryDtl.append("DELETE FROM " + asDetailTableName);
		sbQueryData.append(" WHERE ");
		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
				ls_columnName = jkeyColumn.getString("COLUMN_NAME");
				String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
				if (isFirst) {
					sbQueryData.append(ls_columnName + " = ? ");
					isFirst = false;
				} else {
					sbQueryData.append(" AND " + ls_columnName + " = ? ");
				}
				la_Para.add(jreq.get(ls_columnName));
				jPrimaryDtl.put(ls_columnName, jreq.get(ls_columnName));

				// For getting primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed) {
					jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDatatype.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
				}
			}
		}
		sbQuery.append(sbQueryData.toString());
		sbQueryDtl.append(sbQueryData.toString());

		PreparedStatement pstmt = connection.prepareStatement(sbQuery.toString());
		PreparedStatement pstmtDtl = connection.prepareStatement(sbQueryDtl.toString());
		for (int i = 0; i < la_Para.size(); i++) {
			Object object = la_Para.get(i);
			if (object instanceof Integer) {
				pstmt.setLong((i + 1), Long.valueOf((int) object));
				pstmtDtl.setLong((i + 1), Long.valueOf((int) object));
			} else if (object instanceof Long) {
				pstmt.setLong((i + 1), (Long) object);
				pstmtDtl.setLong((i + 1), (Long) object);
			} else if (object == null) {
				pstmt.setObject((i + 1), null);
				pstmtDtl.setObject((i + 1), null);
			} else if (object instanceof Double) {
				pstmt.setDouble((i + 1), (double) object);
				pstmtDtl.setDouble((i + 1), (double) object);
			} else if (object instanceof Date) {
//				pstmt.setDate((i + 1), (Date) object);
//				pstmtDtl.setDate((i + 1), (Date) object);
				pstmt.setTimestamp((i + 1), new java.sql.Timestamp(((Date) object).getTime()));
				pstmtDtl.setTimestamp((i + 1), new java.sql.Timestamp(((Date) object).getTime()));
			} else if (object instanceof NClob) {
				pstmt.setNClob((i + 1), (NClob) object);
			} else {
				pstmt.setString((i + 1), (String) object);
				pstmtDtl.setString((i + 1), (String) object);
			}
		}
		int detailUpdatedRows = pstmtDtl.executeUpdate();
		int UpdatedColumn = pstmt.executeUpdate();

		// Create audit row data according to detailUpdatedRows
		if (isInsertAuditDataAllowed) {
			for (int i = 0; i < detailUpdatedRows; i++) {
				createAuditRowData("DEL", asDetailTableName, ls_columnName, ls_columnLabel, "", "", "",
						jPrimaryKeyWithDatatype, "D", auditDataArray);
			}

			createAuditRowData("DEL", asTableName, ls_columnName, ls_columnLabel, "", "", "", jPrimaryKeyWithDatatype,
					"M", auditDataArray);
		}

		try {
			pstmtDtl.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}

		try {
			pstmt.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}
		if (UpdatedColumn != 1) {
			connection.rollback();
//			return common.ofGetResponseJson(new JSONArrayImpl(), "",
//					"Delete Failed! Invalid Request (" + UpdatedColumn + ").", ST999, "R",
//					"Delete Failed! Invalid Request (" + UpdatedColumn + ").").toString();
			return ofGetResponseJson(new JSONArrayImpl(), "", "Delete Failed! Invalid Request (" + UpdatedColumn + ").",
					ST999, "R", "common.delete_fail", UpdatedColumn).toString();
		}
		// return common.ofGetResponseJson(new JSONArrayImpl(), "", "Success", ST0, "G",
		// "Success").toString();
		return ofGetResponseJson(new JSONArrayImpl(), "", "Success", ST0, "G", "common.success_msg").toString();
	}

	public String DeleteData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			String asDetailTableName) throws Exception {
		return DeleteData(connection, jreq, jUser, asTableName, asDetailTableName, null);
	}

	/**
	 * Update Data
	 *
	 * @param connection  a Database Connection Object;
	 * @param jreq        as JSONObject of request;
	 * @param jUser       as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param asTableName as Master TableName;
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 */
	public String UpdateData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			JSONArrayImpl auditDataArray) throws Exception {
		JSONArrayImpl jallColumn = getColumnDefination(asTableName, connection, true);
		StringBuffer sbQuery = new StringBuffer();
		StringBuffer sbQueryWhere = new StringBuffer();
		ArrayList<Object> la_UpdPara = new ArrayList<>();
		ArrayList<Object> la_WherePara = new ArrayList<>();
		sbQuery.append("UPDATE " + asTableName + " SET ");
		JSONObjectImpl JUpdatedKey = new JSONObjectImpl();
		JSONObjectImpl jPrimaryKey = new JSONObjectImpl();
		JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
		String ls_columnLabel = StringUtils.EMPTY;
		JSONObjectImpl columnLabels = null;
		// For getting column labels from a JSON object to put in auditDataArray
		if (isInsertAuditDataAllowed) {
			columnLabels = jreq.optJSONObject("_LABELS_MASTER");
		}
		boolean ibFirst = true;
		boolean ibParaFirst = true;
		if (jreq.has("_UPDATEDCOLUMNS")) {
			JSONArrayImpl keysArr = jreq.getJSONArray("_UPDATEDCOLUMNS");
			JSONObjectImpl joldVal = jreq.getJSONObject("_OLDROWVALUE");
			for (int i = 0; i < keysArr.length(); i++) {
				if (joldVal.opt(keysArr.getString(i)) == null) {
					JUpdatedKey.put(keysArr.getString(i), "");
				} else {
					JUpdatedKey.put(keysArr.getString(i), joldVal.opt(keysArr.getString(i)));
				}
			}
		}

		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			String ls_columnName = jkeyColumn.getString("COLUMN_NAME");
			String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
			if (isPrimaryKeyUpdateAllowed && "YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
					&& JUpdatedKey.has(ls_columnName)) {
				if (ibFirst) {
					sbQueryWhere.append(ls_columnName + " = ? ");
					ibFirst = false;
				} else {
					sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
				}
				Object lo_old_data = getColumnValue(connection, ls_columnName, JUpdatedKey, jUser, jkeyColumn, true);
				la_WherePara.add(lo_old_data);
				jPrimaryKey.put(ls_columnName, lo_old_data);
				// For get primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed) {
					jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
				}
				// Check value for old primary key which is going to be updated
				if (ibParaFirst) {
					sbQuery.append(ls_columnName + " = ? ");
					ibParaFirst = false;
				} else {
					sbQuery.append("," + ls_columnName + " = ? ");
				}
				Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
				la_UpdPara.add(objData);

				// Create audit data object if primary key can be updated
				if (isInsertAuditDataAllowed) {
					ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : "";
					createAuditRowData("UPD", asTableName, ls_columnName, ls_columnLabel, "",
							getFormatedValue(lo_old_data), getFormatedValue(objData), jPrimaryKeyWithDatatype, "M",
							auditDataArray);
				}
			} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
				if (ibFirst) {
					sbQueryWhere.append(ls_columnName + " = ?");
					ibFirst = false;
				} else {
					sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
				}
				la_WherePara.add(jreq.get(ls_columnName));
				jPrimaryKey.put(ls_columnName, jreq.get(ls_columnName));
				// For get primary key details with its data type for auditDataArray
				if (isInsertAuditDataAllowed) {
					jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
					jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
				}
			} else if (ENTERED_BY.equals(ls_columnName) || ENTERED_DATE.equals(ls_columnName)
					|| MACHINE_IP.equals(ls_columnName) || MACHINE_NM.equals(ls_columnName)) {//// This Field Update Not
																								//// Allowed

			} else if (LAST_ENTERED_BY.equals(ls_columnName) || LAST_MODIFIED_DATE.equals(ls_columnName)
					|| LAST_MACHINE_NM.equals(ls_columnName) || CONFIRMED.equals(ls_columnName)) {
				if (ibParaFirst) {
					sbQuery.append(ls_columnName + " = ? ");
					ibParaFirst = false;
				} else {
					sbQuery.append("," + ls_columnName + " = ? ");
				}
				Object obj = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
				la_UpdPara.add(obj);
			} else if (JUpdatedKey.has(ls_columnName) && "BLOB".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (ibParaFirst) {
					sbQuery.append(ls_columnName + " = ? ");
					ibParaFirst = false;
				} else {
					sbQuery.append("," + ls_columnName + " = ? ");
				}
				Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
				la_UpdPara.add(objData);

				// Check condition for insert allowed in audit table and create audit data
				// object for BLOB
				if (isInsertAuditDataAllowed) {
					ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : "";
					createAuditRowData("UPD", asTableName, ls_columnName, ls_columnLabel, "BLOB", "",
							getFormatedValue(objData), jPrimaryKeyWithDatatype, "M", auditDataArray);
				}
			} else if (JUpdatedKey.has(ls_columnName) && ("CLOB".equals(jkeyColumn.getString("TYPE_NAME"))
					|| "NCLOB".equals(jkeyColumn.getString("TYPE_NAME")))) {
				if (ibParaFirst) {
					sbQuery.append(ls_columnName + " = ? ");
					ibParaFirst = false;
				} else {
					sbQuery.append("," + ls_columnName + " = ? ");
				}
				Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
				la_UpdPara.add(objData);

				// Check condition for insert allowed in audit table and create audit data
				// object for CLOB, NCLOB
				if (isInsertAuditDataAllowed) {
					ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : "";
					createAuditRowData("UPD", asTableName, ls_columnName, ls_columnLabel,
							jkeyColumn.getString("TYPE_NAME"), "", getFormatedValue(objData), jPrimaryKeyWithDatatype,
							"M", auditDataArray);
				}
			} else if (JUpdatedKey.has(ls_columnName)) {
				Object lo_old_data = null;
				if (StringUtils.isEmpty(String.valueOf(JUpdatedKey.get(ls_columnName)))) {
					if (ibFirst) {
						sbQueryWhere.append(ls_columnName + " IS NULL ");
						ibFirst = false;
					} else {
						sbQueryWhere.append(" AND " + ls_columnName + " IS NULL ");
					}
					lo_old_data = "";
				} else {
					if (ibFirst) {
						sbQueryWhere.append(ls_columnName + " = ? ");
						ibFirst = false;
					} else {
						sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
					}
					Object objData = getColumnValue(connection, ls_columnName, JUpdatedKey, jUser, jkeyColumn, true);
					la_WherePara.add(objData);
					lo_old_data = objData;
				}

				if (ibParaFirst) {
					sbQuery.append(ls_columnName + " = ? ");
					ibParaFirst = false;
				} else {
					sbQuery.append("," + ls_columnName + " = ? ");
				}
				Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
				la_UpdPara.add(objData);

				// Check condition for insert allowed in audit table and create audit data
				// object
				if (isInsertAuditDataAllowed) {
					ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
					createAuditRowData("UPD", asTableName, ls_columnName, ls_columnLabel, "",
							getFormatedValue(lo_old_data), getFormatedValue(objData), jPrimaryKeyWithDatatype, "M",
							auditDataArray);
				}
			}

		}
		sbQuery.append(" WHERE ");
		sbQuery.append(sbQueryWhere.toString());
		// System.out.println(sbQuery.toString());
		// Update primaryData
//		updatePrimaryData("M", jPrimaryKey, auditDataArray);
		PreparedStatement pstmt = connection.prepareStatement(sbQuery.toString());
//		int parameterIndex = 1;
//		for (int i = 0; i < la_UpdPara.size(); i++) {
//			Object object = la_UpdPara.get(i);
//			if (object instanceof Integer) {
//				pstmt.setLong(parameterIndex, Long.valueOf((int) object));
//			} else if (object instanceof Long) {
//				pstmt.setLong(parameterIndex, (Long) object);
//			} else if (object == null) {
//				pstmt.setObject(parameterIndex, null);
//			} else if (object instanceof Blob) {
//				pstmt.setBlob(parameterIndex, (Blob) object);//
//			} else if (object instanceof NClob) {
//				pstmt.setNClob((i + 1), (NClob) object);
//			} else if (object instanceof Clob) {
//				pstmt.setClob(parameterIndex, (Clob) object);//
//			} else if (object instanceof Double) {
//				pstmt.setDouble(parameterIndex, (double) object);
//			} else if (object instanceof Date) {
////				System.out.println("((Date) object).getTime() ==>"+ ((Date) object).getTime());
////				System.out.println("new java.sql.Timestamp(((Date) object).getTime()) ==> "+ new java.sql.Timestamp(((Date) object).getTime()));
//				pstmt.setTimestamp(parameterIndex, new java.sql.Timestamp(((Date) object).getTime()));
////				pstmt.setDate(parameterIndex, (Date) object);
//			} else {
//				pstmt.setString(parameterIndex, (String) object);
//			}
//			// System.out.println(parameterIndex+"=>"+object);
//			parameterIndex += 1;
//
//		}

		int parameterIndex = 0;
		ofsetQueryParameter(parameterIndex, pstmt, la_UpdPara.toArray());
		parameterIndex = la_UpdPara.size();
		ofsetQueryParameter(parameterIndex, pstmt, la_WherePara.toArray());

//		for (int i = 0; i < la_WherePara.size(); i++) {
//			Object object = la_WherePara.get(i);
//			if (object instanceof Integer) {
//				pstmt.setLong(parameterIndex, Long.valueOf((int) object));
//			} else if (object instanceof Long) {
//				pstmt.setLong(parameterIndex, (Long) object);
//			} else if (object == null) {
//				pstmt.setObject(parameterIndex, null);
//			} else if (object instanceof Double) {
//				pstmt.setDouble(parameterIndex, (double) object);
//			} else if (object instanceof NClob) {
//				pstmt.setNClob(parameterIndex, (NClob) object);
//			} else if (object instanceof Date) {
////				System.out.println("(Date) object ==> "+ (Date) object);
////				pstmt.setDate(parameterIndex, (Date) object);
//				pstmt.setTimestamp(parameterIndex, new java.sql.Timestamp(((Date) object).getTime()));
//			} else {
//				pstmt.setString(parameterIndex, (String) object);
//			}
//			// System.out.println(parameterIndex+"=>"+object);
//			parameterIndex += 1;
//		}

		int UpdatedColumn = pstmt.executeUpdate();
		try {
			pstmt.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}
		if (UpdatedColumn != 1) {
			connection.rollback();
//			return common.ofGetResponseJson(new JSONArrayImpl(), "", "Failed to save record (" + UpdatedColumn + ").",
//					ST999, "R", "Failed to save record (" + UpdatedColumn + ").").toString();
			return ofGetResponseJson(new JSONArrayImpl(), "", "Failed to save record (" + UpdatedColumn + ").", ST999,
					"R", "common.save_failed", UpdatedColumn).toString();

		}
//		return common.ofGetResponseJson(new JSONArrayImpl().put(jPrimaryKey), "", "Success", ST0, "G", "Success")
//				.toString();
		return ofGetResponseJson(new JSONArrayImpl().put(jPrimaryKey), "", "Success", ST0, "G", "common.success_msg")
				.toString();

	}

	public String UpdateData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName)
			throws Exception {
		return UpdateData(connection, jreq, jUser, asTableName, new JSONArrayImpl());
	}

	/**
	 * Update Data
	 *
	 * @param connection        a Database Connection Object;
	 * @param jreq              as JSONObject of request;
	 * @param jUser             as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param asTableName       as Master TableName;
	 * @param asDetailTableName as Detail TableName
	 * @return <code>String</code> - Response Data;
	 * @exception Exception if a any error occurs;
	 */
	public String UpdateData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			String as_DetailTableName, JSONArrayImpl auditDataArray) throws Exception {

		String lsResponse = UpdateData(connection, jreq, jUser, asTableName, auditDataArray);
		JSONObjectImpl jobj = common.ofGetJsonObject(lsResponse);

		if (isSuccessStCode(jobj.getString(STATUS))) {
			JSONObjectImpl jPrimaryDtl = jobj.getJSONArray("RESPONSE").getJSONObject(0);
			JSONObjectImpl jDetails = jreq.getJSONObject("DETAILS_DATA");
			String lsDtlResponse = UpdateDetailsData(connection, jDetails, jUser, jPrimaryDtl, as_DetailTableName,
					auditDataArray);
			return lsDtlResponse;
		} else {
			return lsResponse;
		}
	}

	public String UpdateData(Connection connection, JSONObjectImpl jreq, JSONObjectImpl jUser, String asTableName,
			String as_DetailTableName) throws Exception {
		return UpdateData(connection, jreq, jUser, asTableName, as_DetailTableName, null);
	}

	public String UpdateDetailsData(Connection connection, JSONObjectImpl jDetails, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String as_DetailTableName) throws Exception {
		return UpdateDetailsData(connection, jDetails, jUser, jPrimaryDtl, as_DetailTableName, null);
	}

	/**
	 * 
	 * @param connection         a Database Connection Object;
	 * @param jDetails           as JSONObject of request;
	 * @param jUser              as JSONObject of User Details("LOGINUSERDETAILS");
	 * @param jPrimaryDtl        as JSONObject of PrimaryKey Data
	 * @param as_DetailTableName as Update TableName
	 * @return <code>String</code> - Response Data;
	 * @throws Exception
	 */
	public String UpdateDetailsData(Connection connection, JSONObjectImpl jDetails, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String as_DetailTableName, JSONArrayImpl auditDataArray) throws Exception {
		JSONObjectImpl jUpdated = new JSONObjectImpl();
		jUpdated.put("DELETE", 0);
		jUpdated.put("UPDATE", 0);
		jUpdated.put("INSERT", 0);

		JSONArrayImpl jallColumn = getColumnDefination(as_DetailTableName, connection, true);
		JSONArrayImpl jDeleteRow = jDetails.getJSONArray("isDeleteRow");

		if (jDeleteRow.length() > 0 && checkOnlyLabelData(jDeleteRow)) {
			int returnValue = DeleteDTLData(connection, jDeleteRow, jUser, jPrimaryDtl, as_DetailTableName, jallColumn,
					auditDataArray);
			jUpdated.put("DELETE", returnValue);
		}

		JSONArrayImpl jUpdateRow = jDetails.getJSONArray("isUpdatedRow");
		if (jUpdateRow.length() > 0 && checkOnlyLabelData(jUpdateRow)) {
			String ls_resUpdate = UpdateDTLData(connection, jUpdateRow, jUser, jPrimaryDtl, as_DetailTableName,
					jallColumn, auditDataArray);
			JSONObjectImpl jobjinsRes = common.ofGetJsonObject(ls_resUpdate);
			if (!isSuccessStCode(jobjinsRes.getString(STATUS))) {
				return ls_resUpdate;
			}
			jUpdated.put("UPDATE", jobjinsRes.getJSONArray("RESPONSE").getInt(0));
		}

		JSONArrayImpl jInsertRow = jDetails.getJSONArray("isNewRow");
		if (jInsertRow.length() > 0 && checkOnlyLabelData(jInsertRow)) {
			String ls_resInsert = insertDTLData(connection, jInsertRow, jUser, jPrimaryDtl, as_DetailTableName,
					jallColumn, auditDataArray);
			JSONObjectImpl jobjinsRes = common.ofGetJsonObject(ls_resInsert);
			if (!isSuccessStCode(jobjinsRes.getString(STATUS))) {
				return ls_resInsert;
			}
			jUpdated.put("INSERT", jInsertRow.length());
		}
//		return common.ofGetResponseJson(new JSONArrayImpl().put(jUpdated), "", "Success", ST0, "G", "Success")
//				.toString();
		return ofGetResponseJson(new JSONArrayImpl().put(jUpdated), "", "Success", ST0, "G", "common.success_msg")
				.toString();
	}

	private boolean checkOnlyLabelData(JSONArrayImpl jRowList) {
		return !(jRowList.length() == 1 && ((JSONObjectImpl) jRowList.get(0)).has("_LABELS_DETAILS_DATA"));
	}

	public String UpdateDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn) throws Exception {
		return UpdateDTLData(connection, jarrReq, jUser, jPrimaryDtl, asDetailsTable, jallColumn, null);
	}

	public String UpdateDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn, JSONArrayImpl auditDataArray)
			throws Exception {
		int updRowcnt = 0;
		JSONObjectImpl columnLabels = null;
		// For getting column labels from isUpdatedRow[] from details_data object to put
		// in auditDataArray
		if (isInsertAuditDataAllowed) {
			if (jarrReq != null) {
				for (int i = 0; i < jarrReq.length(); i++) {
					JSONObjectImpl objJson = jarrReq.getJSONObject(i);
					if (objJson.has("_LABELS_DETAILS_DATA")) {
						columnLabels = objJson.getJSONObject("_LABELS_DETAILS_DATA");
						jarrReq.remove(i);
					}
				}
			}
		}
		for (int x = 0; x < jarrReq.length(); x++) {
			JSONObjectImpl jreq = jarrReq.getJSONObject(x);
			JSONObjectImpl jPrimaryData = new JSONObjectImpl();
			JSONArrayImpl jUpdateColumn = new JSONArrayImpl();
			StringBuffer sbQuery = new StringBuffer();
			StringBuffer sbQueryWhere = new StringBuffer();
			ArrayList<Object> la_UpdPara = new ArrayList<>();
			ArrayList<Object> la_WherePara = new ArrayList<>();
			JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
			String ls_columnLabel = StringUtils.EMPTY;
			sbQuery.append("UPDATE " + asDetailsTable + " SET ");
			JSONObjectImpl JUpdatedKey = new JSONObjectImpl();
			boolean ibFirst = true;
			boolean ibParaFirst = true;
			if (jreq.has("_UPDATEDCOLUMNS")) {
				JSONArrayImpl keysArr = jreq.getJSONArray("_UPDATEDCOLUMNS");
				JSONObjectImpl joldVal = jreq.getJSONObject("_OLDROWVALUE");
				for (int i = 0; i < keysArr.length(); i++) {
					if (joldVal.opt(keysArr.getString(i)) == null) {
						JUpdatedKey.put(keysArr.getString(i), "");
					} else {
						JUpdatedKey.put(keysArr.getString(i), joldVal.opt(keysArr.getString(i)));
					}
				}
			}
			for (int i = 0; i < jallColumn.length(); i++) {
				JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
				String ls_columnName = jkeyColumn.getString("COLUMN_NAME");
				String ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
				if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN")) && jPrimaryDtl.has(ls_columnName)) {
					if (ibFirst) {
						sbQueryWhere.append(ls_columnName + " = ? ");
						ibFirst = false;
					} else {
						sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
					}
					la_WherePara.add(jPrimaryDtl.get(ls_columnName));
					jPrimaryData.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
					// Create audit data object if primary key can be updated
					if (isInsertAuditDataAllowed) {
						jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
						jPrimaryKeyWithDatatype.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
					}
				} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
					if (ibFirst) {
						sbQueryWhere.append(ls_columnName + " = ? ");
						ibFirst = false;
					} else {
						sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
					}
					la_WherePara.add(jreq.get(ls_columnName));
					jPrimaryData.put(ls_columnName, jreq.get(ls_columnName));
					// For get primary key details with its data type for auditDataArray
					if (isInsertAuditDataAllowed) {
						jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
						jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
					}
				} else if (LAST_ENTERED_BY.equals(ls_columnName) || LAST_MODIFIED_DATE.equals(ls_columnName)
						|| LAST_MACHINE_NM.equals(ls_columnName) || CONFIRMED.equals(ls_columnName)) {
					if (ibParaFirst) {
						sbQuery.append(ls_columnName + " = ? ");
						ibParaFirst = false;
					} else {
						sbQuery.append("," + ls_columnName + " = ? ");
					}
					Object obj = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
					la_UpdPara.add(obj);
				} else if (JUpdatedKey.has(ls_columnName) && "BLOB".equals(jkeyColumn.getString("TYPE_NAME"))) {
					if (ibParaFirst) {
						sbQuery.append(ls_columnName + " = ? ");
						ibParaFirst = false;
					} else {
						sbQuery.append("," + ls_columnName + " = ? ");
					}
					Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
					la_UpdPara.add(objData);
					// Create audit data object for update for BLOB if allowed
					if (isInsertAuditDataAllowed) {
						ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
						jUpdateColumn.put(createAuditJsonData(ls_columnName, ls_columnLabel, "BLOB", "", ""));
					}
				} else if (JUpdatedKey.has(ls_columnName) && ("CLOB".equals(jkeyColumn.getString("TYPE_NAME"))
						|| "NCLOB".equals(jkeyColumn.getString("TYPE_NAME")))) {
					if (ibParaFirst) {
						sbQuery.append(ls_columnName + " = ? ");
						ibParaFirst = false;
					} else {
						sbQuery.append("," + ls_columnName + " = ? ");
					}
					Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
					la_UpdPara.add(objData);
					// Create audit data object for update for CLOB or NCLOB if allowed
					if (isInsertAuditDataAllowed) {
						ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
						jUpdateColumn.put(createAuditJsonData(ls_columnName, ls_columnLabel,
								jkeyColumn.getString("TYPE_NAME"), "", ""));
					}
				} else if (JUpdatedKey.has(ls_columnName)) {
					Object lo_oldValue = null;
					if (StringUtils.isEmpty(String.valueOf(JUpdatedKey.get(ls_columnName)))) {
						if (ibFirst) {
							sbQueryWhere.append(ls_columnName + " IS NULL ");
							ibFirst = false;
						} else {
							sbQueryWhere.append(" AND " + ls_columnName + " IS NULL ");
						}
					} else {
						if (ibFirst) {
							sbQueryWhere.append(ls_columnName + " = ? ");
							ibFirst = false;
						} else {
							sbQueryWhere.append(" AND " + ls_columnName + " = ? ");
						}
						Object objData = getColumnValue(connection, ls_columnName, JUpdatedKey, jUser, jkeyColumn,
								true);
						la_WherePara.add(objData);
						// la_WherePara.add(JUpdatedKey.get(ls_columnName));
						lo_oldValue = objData;
					}

					if (ibParaFirst) {
						sbQuery.append(ls_columnName + " = ? ");
						ibParaFirst = false;
					} else {
						sbQuery.append("," + ls_columnName + " = ? ");
					}
					Object objData = getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn);
					la_UpdPara.add(objData);
					// la_UpdPara.add(jreq.get(ls_columnName));

					// Create audit json data for new value and old value
					if (isInsertAuditDataAllowed) {
						ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
						jUpdateColumn.put(
								createAuditJsonData(ls_columnName, ls_columnLabel, jkeyColumn.getString("TYPE_NAME"),
										String.valueOf(lo_oldValue), String.valueOf(objData)));
					}
				}
			}

			sbQuery.append(" WHERE ");
			sbQuery.append(sbQueryWhere.toString());
			// System.out.println(sbQuery.toString());
			PreparedStatement pstmt = connection.prepareStatement(sbQuery.toString());

//			int parameterIndex = 1;
//			for (int i = 0; i < la_UpdPara.size(); i++) {
//				Object object = la_UpdPara.get(i);
//				if (object instanceof Integer) {
//					pstmt.setLong(parameterIndex, Long.valueOf((int) object));
//				} else if (object instanceof Long) {
//					pstmt.setLong(parameterIndex, (Long) object);
//				} else if (object == null) {
//					pstmt.setObject(parameterIndex, null);
//				} else if (object instanceof Double) {
//					pstmt.setDouble(parameterIndex, (double) object);
//				} else if (object instanceof NClob) {
//					pstmt.setNClob(parameterIndex, (NClob) object);
//				} else if (object instanceof Clob) {
//					pstmt.setClob(parameterIndex, (Clob) object);
//				} else if (object instanceof Date) {
//					pstmt.setTimestamp(parameterIndex, new java.sql.Timestamp(((Date) object).getTime()));
////					pstmt.setDate(parameterIndex, (Date) object);
//				} else {
//					pstmt.setString(parameterIndex, (String) object);
//				}
//				// System.out.println(parameterIndex+"=>"+object);
//				parameterIndex += 1;
//
//			}
			int parameterIndex = 0;
			ofsetQueryParameter(parameterIndex, pstmt, la_UpdPara.toArray());
			parameterIndex = la_UpdPara.size();
			ofsetQueryParameter(parameterIndex, pstmt, la_WherePara.toArray());

//			for (int i = 0; i < la_WherePara.size(); i++) {
//				Object object = la_WherePara.get(i);
//				if (object instanceof Integer) {
//					pstmt.setLong(parameterIndex, Long.valueOf((int) object));
//				} else if (object instanceof Long) {
//					pstmt.setLong(parameterIndex, (Long) object);
//				} else if (object == null) {
//					pstmt.setObject(parameterIndex, null);
//				} else if (object instanceof Double) {
//					pstmt.setDouble(parameterIndex, (double) object);
//				} else if (object instanceof NClob) {
//					pstmt.setNClob(parameterIndex, (NClob) object);
//				} else if (object instanceof Clob) {
//					pstmt.setClob(parameterIndex, (Clob) object);
//				} else if (object instanceof Date) {
//					pstmt.setTimestamp(parameterIndex, new java.sql.Timestamp(((Date) object).getTime()));
////					pstmt.setDate(parameterIndex, (Date) object);
//				} else {
//					pstmt.setString(parameterIndex, (String) object);
//				}
//				// System.out.println(parameterIndex+"=>"+object);
//				parameterIndex += 1;
//			}
			int UpdatedColumn = pstmt.executeUpdate();
			try {
				pstmt.close();
			} catch (Exception exception) {
				// TODO: handle exception
			}
			if (UpdatedColumn != 1) {
				connection.rollback();
//				return common.ofGetResponseJson(new JSONArrayImpl(), "", "Failed to save record (" + UpdatedColumn + ").",
//								ST999, "R", "Failed to save record (" + UpdatedColumn + ").").toString();
				return ofGetResponseJson(new JSONArrayImpl(), "", "Failed to save record (" + UpdatedColumn + ").",
						ST999, "R", "common.save_failed", UpdatedColumn).toString();
			}
			updRowcnt += 1;

			/*
			 * Create audit row data according to updRowcnt after the update was successful
			 * and add all changes for old value and new values
			 */
			if (isInsertAuditDataAllowed) {
				for (int i = 0; i < jUpdateColumn.length(); i++) {
					JSONObjectImpl jupdData = jUpdateColumn.getJSONObject(i);
					createAuditRowData("UPD", asDetailsTable, jupdData.getString("COLUMN_NAME"), ls_columnLabel, "",
							jupdData.getString("OLD_VALUE"), jupdData.getString("NEW_VALUE"), jPrimaryKeyWithDatatype,
							"D", auditDataArray);
				}
			}
		}
//		return common.ofGetResponseJson(new JSONArrayImpl().put(updRowcnt), "", "Success", ST0, "G", "Success")
//				.toString();
		return ofGetResponseJson(new JSONArrayImpl().put(updRowcnt), "", "Success", ST0, "G", "common.success_msg")
				.toString();

	}

	public int DeleteDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn, JSONArrayImpl auditDataArray)
			throws Exception {
		boolean isFirst;
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("DELETE FROM " + asDetailsTable);
		sbQuery.append(" WHERE ");
		JSONObjectImpl columnLabels = null;
		// Create a JSON object for column labels from detals_data object
		if (isInsertAuditDataAllowed) {
			if (jarrReq != null) {
				for (int k = 0; k < jarrReq.length(); k++) {
					JSONObjectImpl objJson = jarrReq.getJSONObject(k);
					if (objJson.has("_LABELS_DETAILS_DATA")) {
						columnLabels = objJson.getJSONObject("_LABELS_DETAILS_DATA");
						jarrReq.remove(k);
					}
				}
			}
		}
		PreparedStatement pstmt = null;
		for (int i = 0; i < jarrReq.length(); i++) {
			JSONObjectImpl jreq = jarrReq.getJSONObject(i);
			ArrayList<Object> la_Para = new ArrayList<>();
			String ls_columnName = StringUtils.EMPTY;
			String ls_columnLabel = StringUtils.EMPTY;
			String ls_columnDataType = StringUtils.EMPTY;
			JSONObjectImpl jPrimaryData = new JSONObjectImpl();
			JSONObjectImpl jPrimaryKeyWithDatatype = new JSONObjectImpl();
			if (i == 0) {
				isFirst = true;
				for (int j = 0; j < jallColumn.length(); j++) {
					JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(j);
					ls_columnName = jkeyColumn.getString("COLUMN_NAME");
					ls_columnDataType = jkeyColumn.getString("TYPE_NAME");
					if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN")) && jPrimaryDtl.has(ls_columnName)) {
						if (isFirst) {
							sbQuery.append(ls_columnName + " = ? ");
							isFirst = false;
						} else {
							sbQuery.append(" AND " + ls_columnName + " = ? ");
						}

						la_Para.add(jPrimaryDtl.get(ls_columnName));
						jPrimaryData.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
						// For get primary key details with its value and data type for auditDataArray
						if (isInsertAuditDataAllowed) {
							jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
							jPrimaryKeyWithDatatype.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
						}
					} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
						if (isFirst) {
							sbQuery.append(ls_columnName + " = ? ");
							isFirst = false;
						} else {
							sbQuery.append(" AND " + ls_columnName + " = ? ");
						}

						la_Para.add(jreq.get(ls_columnName));
						jPrimaryData.put(ls_columnName, jreq.get(ls_columnName));
						// For get primary key details with its value and data type for auditDataArray
						if (isInsertAuditDataAllowed) {
							jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
							jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
						}
					}
				}
				pstmt = connection.prepareStatement(sbQuery.toString());
			} else {
				for (int j = 0; j < jallColumn.length(); j++) {
					JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(j);
					ls_columnName = jkeyColumn.getString("COLUMN_NAME");
					if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN")) && jPrimaryDtl.has(ls_columnName)) {
						la_Para.add(jPrimaryDtl.get(ls_columnName));
						jPrimaryData.put(ls_columnName, jPrimaryDtl.get(ls_columnName));
						// For get primary key details with its data type for auditDataArray
						if (isInsertAuditDataAllowed) {
							jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
							jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
						}
					} else if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))) {
						la_Para.add(jreq.get(ls_columnName));
						jPrimaryData.put(ls_columnName, jreq.get(ls_columnName));
						if (isInsertAuditDataAllowed) {
							jPrimaryKeyWithDatatype.put("TYPE_NAME_" + ls_columnName, ls_columnDataType);
							jPrimaryKeyWithDatatype.put(ls_columnName, jreq.get(ls_columnName));
						}
					}
				}
			}

			for (int z = 0; z < la_Para.size(); z++) {
				Object object = la_Para.get(z);
				if (object instanceof Integer) {
					pstmt.setLong((z + 1), Long.valueOf((int) object));
				} else if (object instanceof Long) {
					pstmt.setLong((z + 1), (Long) object);
				} else if (object == null) {
					pstmt.setObject((z + 1), null);
				} else if (object instanceof Double) {
					pstmt.setDouble((z + 1), (double) object);
				} else if (object instanceof Date) {
//					pstmt.setDate((z + 1), (Date) object);
					pstmt.setTimestamp((z + 1), new java.sql.Timestamp(((Date) object).getTime()));
				} else {
					pstmt.setString((z + 1), (String) object);
				}
			}

			pstmt.addBatch();
			// Create Audit Data for every data which is delete from detail table
			if (isInsertAuditDataAllowed) {
				ls_columnLabel = columnLabels != null ? columnLabels.optString(ls_columnName) : ls_columnName;
				createAuditRowData("DEL", asDetailsTable, ls_columnName, ls_columnLabel, "", "", "",
						jPrimaryKeyWithDatatype, "D", auditDataArray);
			}
		}
		int UpdatedColumn[] = pstmt.executeBatch();
		// int UpdatedColumn = pstmt.executeUpdate();
		int UpdaCol = 0;
		for (int i = 0; i < UpdatedColumn.length; i++) {
			UpdaCol += UpdatedColumn[i];
		}
		try {
			pstmt.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}
		return UpdaCol;
		// return common.ofGetResponseJson(new JSONArray().put(UpdatedColumn), "",
		// "Success", ST0, "G", "Success").toString();

	}

	public int DeleteDTLData(Connection connection, JSONArrayImpl jarrReq, JSONObjectImpl jUser,
			JSONObjectImpl jPrimaryDtl, String asDetailsTable, JSONArrayImpl jallColumn) throws Exception {
		return DeleteDTLData(connection, jarrReq, jUser, jPrimaryDtl, asDetailsTable, jallColumn, null);
	}

	private Object getColumnValue(Connection connection, String ls_columnName, JSONObjectImpl jreq,
			JSONObjectImpl jUser, JSONObjectImpl jkeyColumn) throws Exception {
		return getColumnValue(connection, ls_columnName, jreq, jUser, jkeyColumn, false);
	}

	private Object getColumnValue(Connection connection, String ls_columnName, JSONObjectImpl jreq,
			JSONObjectImpl jUser, JSONObjectImpl jkeyColumn, boolean isWhereData) throws Exception {

		String objvalue = StringUtils.EMPTY;
//		String dbResData = StringUtils.EMPTY;
//		String  ls_resEncrupt= StringUtils.EMPTY;
//		JSONObject responseJson;

		Object obj = null;
		if (!isWhereData && (ENTERED_BY.equals(ls_columnName) || LAST_ENTERED_BY.equals(ls_columnName))) {
			obj = jUser.get("USERNAME");
		} else if (!isWhereData && (ENTERED_DATE.equals(ls_columnName) || LAST_MODIFIED_DATE.equals(ls_columnName))) {
			long millis = System.currentTimeMillis();
			java.sql.Date Todaydate = new java.sql.Date(millis);
			obj = Todaydate;
		} else if (!isWhereData && (MACHINE_NM.equals(ls_columnName) || LAST_MACHINE_NM.equals(ls_columnName))) {
			obj = jUser.get("MACHINE_NAME");
		} else if (!isWhereData && (VERIFIED_BY.equals(ls_columnName))) {

			if (isInsertVerificationFields) {
				obj = jUser.get("USERNAME");
			} else {
				obj = "";
			}
		} else if (!isWhereData && (VERIFIED_MACHINE_NM.equals(ls_columnName))) {

			if (isInsertVerificationFields) {
				obj = jUser.get("MACHINE_NAME");
			} else {
				obj = "";
			}
		} else if (!isWhereData && VERIFIED_DATE.equals(ls_columnName)) {
			if (isInsertVerificationFields) {
				long millis = System.currentTimeMillis();
				java.sql.Date Todaydate = new java.sql.Date(millis);
				obj = Todaydate;
			} else {
				obj = null;
			}
		} else if (!isWhereData && CONFIRMED.equals(ls_columnName)) {

			if (CONFIRMATION_PARA == PARA_CONFIRM_AUTO) {
				obj = "Y";
			} else if (CONFIRMATION_PARA == PARA_CONFIRM_MANUAL && "Y".equals(jreq.opt(ls_columnName))) {
				obj = "Y";
			} else if (CONFIRMATION_PARA == PARA_CONFIRM_MANUAL && "0".equals(jreq.opt(ls_columnName))) {
				obj = "0";
			} else if (CONFIRMATION_PARA == PARA_CONFIRM_MANUAL && "P".equals(jreq.opt(ls_columnName))) {
				obj = "P";
			} else if (CONFIRMATION_PARA == PARA_CONFIRM_MANUAL) {
				obj = jreq.get(CONFIRMED);
			} else {
				obj = "N";
			}

		} else {
			if ("CHAR".equals(jkeyColumn.getString("TYPE_NAME")) || "VARCHAR2".equals(jkeyColumn.getString("TYPE_NAME"))
					|| "VARCHAR".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (jreq.opt(ls_columnName) == null) {
					obj = "";

				} else if (setMobileEncryptData.contains(ls_columnName)) {
					objvalue = (String) jreq.opt(ls_columnName);
					obj = getEncryptedMobileNo(objvalue);

				} else if (setCardEncryptData.contains(ls_columnName)) {
					objvalue = (String) jreq.opt(ls_columnName);
					obj = getEncryptedCardNo(objvalue);

				} else {
					obj = String.valueOf(jreq.opt(ls_columnName));
				}
			} else if ("NVARCHAR2".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (jreq.opt(ls_columnName) == null) {
					obj = "";
				} else {
					obj = jreq.opt(ls_columnName);
				}
			} else if ("DATE".equals(jkeyColumn.getString("TYPE_NAME"))) {
				String ls_data = (String) jreq.opt(ls_columnName);
				// System.out.println("ls_data ==> "+ ls_data);
				if (StringUtils.isBlank(ls_data)) {
					obj = null;
				} else {

					obj = getSqlDateFromString(ls_data);
				}
			} else if ("NUMBER".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (StringUtils.isBlank((String) jreq.opt(ls_columnName))) {
					obj = null;
				} else {
					obj = Double.valueOf((String) jreq.opt(ls_columnName));
				}

			} else if ("BLOB".equals(jkeyColumn.getString("TYPE_NAME"))) {

				if (StringUtils.isBlank((String) jreq.opt(ls_columnName))) {
					obj = null;
				} else {
					obj = base64StringtoBlob(connection, (String) jreq.opt(ls_columnName));
				}
			} else if ("NCLOB".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (StringUtils.isBlank((String) jreq.opt(ls_columnName))) {
					obj = null;
				} else {
					obj = StringtoNClob(connection, (String) jreq.opt(ls_columnName));
				}
			} else if ("CLOB".equals(jkeyColumn.getString("TYPE_NAME"))) {
				if (StringUtils.isBlank((String) jreq.opt(ls_columnName))) {
					// if (StringUtils.isBlank((String) jreq.opt(ls_columnName).toString() ) ) {
					obj = null;
				} else {
					obj = StringtoClob(connection, (String) jreq.opt(ls_columnName));
				}
			} else {
				obj = jreq.get(ls_columnName);
			}
		}
		return obj;
	}

	protected JSONArrayImpl getColumnDefination(String as_tableName, Connection connection,
			boolean isRequiredPrimaryKey) throws Exception {
		JSONArrayImpl jallColumn = new JSONArrayImpl();
		JSONObjectImpl jPrimaryKey = new JSONObjectImpl();
		DatabaseMetaData databasemetadata = connection.getMetaData();

		String ls_SchemaName = SCHEMANAME;
		String ls_tableName = as_tableName;
		if (ls_tableName.indexOf(".") >= 0) {
			ls_tableName = ls_tableName.substring(ls_tableName.indexOf(".") + 1);
		}
//		try {
//			ls_SchemaName=connection.getMetaData().getConnection().getSchema();
//		} catch (Exception e) {
//			ls_SchemaName=connection.getSchema();
//		}
		ResultSet columnsResultSet = databasemetadata.getColumns(null, ls_SchemaName, ls_tableName, null);
		if (isRequiredPrimaryKey) {
			ResultSet primaryColumns = databasemetadata.getPrimaryKeys(null, ls_SchemaName, ls_tableName);
			while (primaryColumns.next()) {
				jPrimaryKey.put(primaryColumns.getString("COLUMN_NAME"), primaryColumns.getString("PK_NAME"));
			}
			try {
				primaryColumns.close();
			} catch (Exception exception) {
			}
		}
		while (columnsResultSet.next()) {
			JSONObjectImpl jcolumn = new JSONObjectImpl();
			jcolumn.put("COLUMN_NAME", columnsResultSet.getString("COLUMN_NAME"));
			jcolumn.put("DATA_TYPE", columnsResultSet.getInt("DATA_TYPE"));
			jcolumn.put("TYPE_NAME", columnsResultSet.getString("TYPE_NAME"));
			jcolumn.put("COLUMN_SIZE", columnsResultSet.getInt("COLUMN_SIZE"));
			if (jPrimaryKey.has(columnsResultSet.getString("COLUMN_NAME"))) {
				jcolumn.put("ISPRIMARYKEYCOLUMN", "YES");
				jcolumn.put("PRIMARYKEY_NAME", jPrimaryKey.getString(columnsResultSet.getString("COLUMN_NAME")));
			} else {
				jcolumn.put("ISPRIMARYKEYCOLUMN", "NO");
				jcolumn.put("PRIMARYKEY_NAME", "");
			}
			jallColumn.put(jcolumn);

		}
		try {
			columnsResultSet.close();
		} catch (Exception exception) {
			// TODO: handle exception
		}
		return jallColumn;
	}

	protected long getMaxCd(Connection connection, String asTableName, String asColumnName, int seqPara,
			String sequnceName) throws Exception {
		long ll_MaxCd = 0L;
		if (seqPara == PARA_USESEQUENCES && StringUtils.isEmpty(sequnceName)) {
			throw new NullArgumentException(sequnceName);
		}

		String ls_query;
		if (seqPara == PARA_USESEQUENCES) {
			ls_query = "SELECT " + sequnceName + ".NEXTVAL FROM DUAL";
		} else if (seqPara == PARA_USE_MAXCD) {
			ls_query = "SELECT MAX(" + asColumnName + ") FROM " + asTableName;
		} else {
			throw new InvalidValueException("seqPara");
		}

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(ls_query);
		if (rs.next()) {
			ll_MaxCd = rs.getLong(1);
		}

		if (seqPara == PARA_USE_MAXCD) {
			ll_MaxCd = ll_MaxCd + 1;
		}
		try {
			rs.close();
		} catch (Exception exception) {
		}
		try {
			stmt.close();
		} catch (Exception exception) {
		}
		return ll_MaxCd;
	}

	public long getMaxSrCd(Connection connection, String asTableName, String asColumnName, JSONArrayImpl jallColumn,
			JSONObjectImpl jPrimaryDtl) throws Exception {

		long ll_MaxCd = 0L;

		String ls_query = "SELECT MAX(" + asColumnName + ") FROM " + asTableName;
		String ls_where = null;
		ArrayList<Object> la_Para = new ArrayList<>();

		for (int i = 0; i < jallColumn.length(); i++) {
			JSONObjectImpl jkeyColumn = jallColumn.getJSONObject(i);
			if ("YES".equals(jkeyColumn.getString("ISPRIMARYKEYCOLUMN"))
					&& jPrimaryDtl.has(jkeyColumn.getString("COLUMN_NAME"))
					&& StringUtils.isNotBlank(jPrimaryDtl.getString(jkeyColumn.getString("COLUMN_NAME")))) {
				if (ls_where == null) {
					ls_where = " WHERE " + jkeyColumn.getString("COLUMN_NAME") + "= ? ";
				} else {
					ls_where += " AND " + jkeyColumn.getString("COLUMN_NAME") + "= ? ";
				}
				la_Para.add(jPrimaryDtl.get(jkeyColumn.getString("COLUMN_NAME")));
			}
		}
		ls_query += (ls_where == null) ? "" : ls_where;
		PreparedStatement pstmt = connection.prepareStatement(ls_query);
		for (int i = 0; i < la_Para.size(); i++) {
			Object object = la_Para.get(i);
			if (object instanceof Integer) {
				pstmt.setLong((i + 1), Long.valueOf((int) object));
			} else if (object instanceof Long) {
				pstmt.setLong((i + 1), (Long) object);
			} else if (object == null) {
				pstmt.setObject((i + 1), null);
			} else if (object instanceof Double) {
				pstmt.setDouble((i + 1), (double) object);
			} else if (object instanceof Date) {
//				pstmt.setDate((i + 1), (Date) object);
				pstmt.setTimestamp((i + 1), new java.sql.Timestamp(((Date) object).getTime()));
			} else {
				pstmt.setString((i + 1), (String) object);
			}
		}
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			ll_MaxCd = rs.getLong(1);
		}
		// ll_MaxCd = ll_MaxCd + 1;
		try {
			rs.close();
		} catch (Exception exception) {
		}
		try {
			pstmt.close();
		} catch (Exception exception) {
		}
		return ll_MaxCd;
	}

	public JSONObjectImpl setConfigurationJson(String as_actionType, String as_master_data, String as_detail_data) {
		String ls_access_i = "4";
		String ls_access_u = "4";
		String ls_access_d = "4";
		String ls_sequence_para = "1";
		String ls_sequence_name = "";

		return setConfigurationJson(as_actionType, as_master_data, as_detail_data, ls_sequence_para, ls_sequence_name,
				ls_access_i, ls_access_u, ls_access_d);
	}

	public JSONObjectImpl setConfigurationJson(String as_actionType, String as_master_data, String as_detail_data,
			String as_sequence_para, String as_sequence_name, String as_access_i, String as_access_u,
			String as_access_d) throws NullArgumentException {
		JSONObjectImpl AccessJson = null;
		JSONObjectImpl DMLConfigJson = null;

		if (as_actionType.contains("MD")) {

			if (StringUtils.isBlank(as_master_data)) {
				throw new NullArgumentException("MASTER_DATA");
			} else if (StringUtils.isBlank(as_detail_data)) {
				throw new NullArgumentException("DETAIL_DATA");
			} else if (StringUtils.isBlank(as_sequence_para)) {
				throw new NullArgumentException("SEQUENCE_PARA");
			} else if (StringUtils.isBlank(as_sequence_name) && as_sequence_para.contains("0")) {
				throw new NullArgumentException("SEQUENCE_NAME");
			}
		}
		// master type
		else if (as_actionType.contains("M")) {
			if (StringUtils.isBlank(as_master_data)) {
				throw new NullArgumentException("MASTER_DATA");
			} else if (StringUtils.isBlank(as_sequence_para)) {
				throw new NullArgumentException("SEQUENCE_PARA");
			} else if (StringUtils.isBlank(as_sequence_name) && as_sequence_para.contains("0")) {
				throw new NullArgumentException("SEQUENCE_NAME");
			}
		}
		// detail type
		else if (as_actionType.contains("D")) {
			if (StringUtils.isBlank(as_detail_data)) {
				throw new NullArgumentException("DETAIL_DATA");
			}

			else if (StringUtils.isBlank(as_sequence_para)) {
				throw new NullArgumentException("SEQUENCE_PARA");
			} else if (StringUtils.isBlank(as_sequence_name) && as_sequence_para.contains("0")) {
				throw new NullArgumentException("SEQUENCE_NAME");
			}
		} else {
			throw new NullArgumentException("ACTION_TYPE");
		}

		if (StringUtils.isBlank(as_sequence_name)) {
			as_sequence_name = "";
		}

		AccessJson = new JSONObjectImpl();
		AccessJson.put("I", Integer.parseInt(as_access_i));
		AccessJson.put("U", Integer.parseInt(as_access_u));
		AccessJson.put("D", Integer.parseInt(as_access_d));

		DMLConfigJson = new JSONObjectImpl();
		DMLConfigJson.put("ACTION_TYPE", as_actionType);
		DMLConfigJson.put("MASTER_TABLE", as_master_data);
		DMLConfigJson.put("DETAILS_TABLE", as_detail_data);
		DMLConfigJson.put("SEQUENCE_PARA", Integer.parseInt(as_sequence_para));
		DMLConfigJson.put("SEQUENCE_NAME", as_sequence_name);
		DMLConfigJson.put("ACCESS", AccessJson);

		return DMLConfigJson;
	}

	public NClob StringtoNClob(Connection connection, String as_Str) throws Exception {
		if (StringUtils.isNotBlank(as_Str)) {
			NClob ncret = connection.createNClob();
			ncret.setString(1, as_Str);
			return ncret;
		}
		return null;
	}

	public Clob StringtoClob(Connection connection, String as_Str) throws Exception {
		if (StringUtils.isNotBlank(as_Str)) {
			Clob cret = connection.createClob();
			cret.setString(1, as_Str);
			return cret;
		}
		return null;
	}

	public Blob base64StringtoBlob(Connection connection, String as_base64) throws Exception {
		if (StringUtils.isNotBlank(as_base64)) {
			byte[] decodedByte = Base64.getDecoder().decode(as_base64);
			Blob bret = connection.createBlob();
			bret.setBytes(1, decodedByte);
			// b = new SerialBlob(decodedByte);
			return bret;
		}
		return null;
	}

	public InputStream base64StringtoInputStream(String as_base64) {
		InputStream is = null;
		byte[] decodedByte;
		try {
			decodedByte = Base64.getDecoder().decode(as_base64);
			is = new ByteArrayInputStream(decodedByte);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return is;
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getEncryptedMobileNo(String mob) {
		if (StringUtils.isEmpty(mob)) {
			return "";
		}
		return common.ofGetJsonObject(selectData.getSelectData(SELECTMOBILEDATAENC, mob)).getJSONArray("RESPONSE")
				.getJSONObject(0).getString("ENCURPTMOBDATA");
	}

	private String getEncryptedCardNo(String card) {
		if (StringUtils.isEmpty(card)) {
			return "";
		}
		return common.ofGetJsonObject(selectData.getSelectData(SELECTCARDDATAENC, card)).getJSONArray("RESPONSE")
				.getJSONObject(0).getString("ENCURPTCARDDATA");
	}

	public void setConfirmationRules(int rule) {
		CONFIRMATION_PARA = rule;
	}

	public String setMobileEncryptValueDML(Object... encrupytkey) throws Exception {

		String ls_encruptKey = StringUtils.EMPTY;
		setMobileEncryptData = new ArrayList<>();
		for (int j = 0; j < encrupytkey.length; j++) {
			ls_encruptKey = (String) common.getDataAtIndex(j, encrupytkey);
			setMobileEncryptData.add(ls_encruptKey);
		}
		return setMobileEncryptData.toString();

	}

	public String setCardEncryptValueDML(Object... encrupytkey) throws Exception {

		String ls_encruptKey = StringUtils.EMPTY;
		setCardEncryptData = new ArrayList<>();
		for (int j = 0; j < encrupytkey.length; j++) {
			ls_encruptKey = (String) common.getDataAtIndex(j, encrupytkey);
			setCardEncryptData.add(ls_encruptKey);
		}

		return setCardEncryptData.toString();
	}

	public void setMultiDetail(boolean lb_data) {
		isSingleMstMultiDtl = lb_data;
	}

	public void setSchemaName(String schemaName) {
		if (StringUtils.isNotBlank(schemaName)) {
			SCHEMANAME = schemaName;
		}
	}

	// For update primary key
	protected void setPrimaryKeyUpdateAllowed(boolean lb_update) {
		isPrimaryKeyUpdateAllowed = lb_update;
	}

	// For insert data in audit table for INSERT, UPDATE or DELETE operation
	protected void setInsertAuditDataAllowed(boolean lb_insert) {
		isInsertAuditDataAllowed = lb_insert;
	}

	/*
	 * @param action a action performed (INSERT, UPDATE, DELETE)
	 * 
	 * @param tableName a table name
	 * 
	 * @param columnName a column name of table name
	 * 
	 * @param columnLabel a column label getting from request
	 * 
	 * @param columnType
	 * 
	 * @param old_value
	 * 
	 * @param new_value
	 * 
	 * @param jPrimaryKey a primary key of the record
	 * 
	 * @param master_details_flag as The flag indicates master or detail table
	 * 
	 * @param audiDataArray as the json array to store audit data This method is
	 * used to create a JSON object for auditDataArray
	 */
	public void createAuditRowData(String action, String tableName, String columnName, String columnLabel,
			String columnType, String old_value, String new_value, JSONObjectImpl jPrimaryKey,
			String master_details_flag, JSONArrayImpl auditDataArray) {
		JSONObjectImpl objJson = createAuditRowData(action, tableName, columnName, columnLabel, columnType, old_value,
				new_value, jPrimaryKey, master_details_flag, false);
		auditDataArray.put(objJson);
	}
//	
//	public JSONArrayImpl createAuditRowData(String action, String tableName, String columnName, String columnLabel, String columnType, 
//			String old_value, String new_value, String table_key_value1, String table_key_value2, String table_key_value3, 
//			String table_char_key1, String table_char_key2, String table_char_key3) {
//		JSONArrayImpl auditDataArray = new JSONArrayImpl();
//		JSONObjectImpl objJson = new JSONObjectImpl();
//		//Split schema name from table name
//		String[] parts = tableName.split("\\.");
//		String table_name = parts.length > 1 ? parts[1] : tableName;
//		
//		objJson.put("ACTION", action);
//		objJson.put("TABLE_NAME", table_name);
//		objJson.put("COLUMN_NAME", columnName);
//		objJson.put("COLUMN_LABEL", columnLabel);
//		objJson.put("COLUMN_STYLE", columnType);
//		objJson.put("OLD_VALUE", old_value);
//		objJson.put("NEW_VALUE", new_value);
//		objJson.put("TABLE_KEY_VALUE1", table_key_value1);
//	    objJson.put("TABLE_KEY_VALUE2", table_key_value2);
//	    objJson.put("TABLE_KEY_VALUE3", table_key_value3);
//	    objJson.put("TABLE_CHAR_KEY1", table_char_key1);
//	    objJson.put("TABLE_CHAR_KEY2", table_char_key2);
//	    objJson.put("TABLE_CHAR_KEY3", table_char_key3);
//	    auditDataArray.put(objJson);
//	    return auditDataArray;
//	}
//	
//	private JSONObjectImpl createAuditRowData(String action, String tableName, String columnName, String columnLabel, String columnType, 
//			String old_value, String new_value, JSONObjectImpl jPrimaryKey, String master_details_flag, 
//			boolean isPrimaryUpdated) {
//		JSONObjectImpl objJson = new JSONObjectImpl();
//		//Split schema name from table name
//		String[] parts = tableName.split("\\.");
//		String table_name = parts.length > 1 ? parts[1] : tableName;
//		
//		objJson.put("ACTION", action);
//		objJson.put("TABLE_NAME", table_name);
//		objJson.put("COLUMN_NAME", columnName);
//		objJson.put("COLUMN_LABEL", columnLabel);
//		objJson.put("COLUMN_STYLE", columnType);
//		objJson.put("OLD_VALUE", old_value);
//		objJson.put("NEW_VALUE", new_value);
//		
//		extractKeyValuePairs(jPrimaryKey, objJson);
//		if (!objJson.has("TABLE_KEY_VALUE1")) {
//			objJson.put("TABLE_KEY_VALUE1", JSONObjectImpl.NULL);
//		}
//		if (!objJson.has("TABLE_KEY_VALUE2")) {
//			objJson.put("TABLE_KEY_VALUE2", JSONObjectImpl.NULL);
//		}
//		if (!objJson.has("TABLE_KEY_VALUE3")) {
//			objJson.put("TABLE_KEY_VALUE3", JSONObjectImpl.NULL);
//		}
//		if (!objJson.has("TABLE_CHAR_KEY1")) {
//			objJson.put("TABLE_CHAR_KEY1", JSONObjectImpl.NULL);
//		}
//		if (!objJson.has("TABLE_CHAR_KEY2")) {
//			objJson.put("TABLE_CHAR_KEY2", JSONObjectImpl.NULL);
//		}
//		if (!objJson.has("TABLE_CHAR_KEY3")) {
//			objJson.put("TABLE_CHAR_KEY3", JSONObjectImpl.NULL);
//		}
//		
//	    objJson.put("PRIMARY_UPD", isPrimaryUpdated ? "Y" : "N");
//		objJson.put("MASTER_DETAILS_FLAG", master_details_flag);
//		
//		return objJson;
//	}

	/*
	 * @param action ADD/UPD/DEL
	 * 
	 * @param tableName master or detail table name
	 * 
	 * @param columnName column name as required (according to previous data)
	 * 
	 * @param columnLabel column label from request (according to previous data)
	 * 
	 * @param columnType as previous data (refer table)
	 * 
	 * @param old_value null or (refer table, could be change while delete and
	 * insert operation)
	 * 
	 * @param new_value inserted value from request or (according previous data,
	 * refer table)
	 * 
	 * @param table_key_value1 if 1st primary key is number data type or (null /
	 * blank)
	 * 
	 * @param table_key_value2 if 2nd primary key is number data type or (null /
	 * blank)
	 * 
	 * @param table_key_value3 if 3rd primary key is number data type or (null /
	 * blank)
	 * 
	 * @param table_char_key1 if 1st primary key is char/ varchar/ varchar2 data
	 * type or (null / blank)
	 * 
	 * @param table_char_key2 if 2nd primary key is char/ varchar/ varchar2 data
	 * type or (null / blank)
	 * 
	 * @param table_char_key3 if 3rd primary key is char/ varchar/ varchar2 data
	 * type or (null / blank)
	 */
	protected JSONObjectImpl createAuditRowData(String action, String tableName, String columnName, String columnLabel,
			String columnType, String old_value, String new_value, String table_key_value1, String table_key_value2,
			String table_key_value3, String table_char_key1, String table_char_key2, String table_char_key3) {
		return createAuditRowData(action, tableName, columnName, columnLabel, columnType, old_value, new_value,
				table_key_value1, table_key_value2, table_key_value3, table_char_key1, table_char_key2, table_char_key3,
				null, null, false);
	}

	private JSONObjectImpl createAuditRowData(String action, String tableName, String columnName, String columnLabel,
			String columnType, String old_value, String new_value, JSONObjectImpl jPrimaryKey,
			String master_details_flag, boolean isPrimaryUpdated) {
		return createAuditRowData(action, tableName, columnName, columnLabel, columnType, old_value, new_value, "", "",
				"", "", "", "", jPrimaryKey, master_details_flag, isPrimaryUpdated);
	}

	private JSONObjectImpl createAuditRowData(String action, String tableName, String columnName, String columnLabel,
			String columnType, String old_value, String new_value, String table_key_value1, String table_key_value2,
			String table_key_value3, String table_char_key1, String table_char_key2, String table_char_key3,
			JSONObjectImpl jPrimaryKey, String master_details_flag, boolean isPrimaryUpdated) {
		JSONObjectImpl objJson = new JSONObjectImpl();
		// Split schema name from table name if available
		String[] parts = tableName.split("\\.");
		String table_name = parts.length > 1 ? parts[1] : tableName;

		objJson.put("ACTION", action);
		objJson.put("TABLE_NAME", table_name);
		objJson.put("COLUMN_NAME", columnName);
		objJson.put("COLUMN_LABEL", columnLabel);
		objJson.put("COLUMN_STYLE", columnType);
		objJson.put("OLD_VALUE", old_value);
		objJson.put("NEW_VALUE", new_value);

		// Replace empty string with values with "0"
		table_key_value1 = table_key_value1.isEmpty() ? "0" : table_key_value1;
		table_key_value2 = table_key_value2.isEmpty() ? "0" : table_key_value2;
		table_key_value3 = table_key_value3.isEmpty() ? "0" : table_key_value3;

		if (jPrimaryKey != null) {
			extractKeyValuePairs(jPrimaryKey, objJson);
			if (!objJson.has("TABLE_KEY_VALUE1")) {
				objJson.put("TABLE_KEY_VALUE1", "0");
			}
			if (!objJson.has("TABLE_KEY_VALUE2")) {
				objJson.put("TABLE_KEY_VALUE2", "0");
			}
			if (!objJson.has("TABLE_KEY_VALUE3")) {
				objJson.put("TABLE_KEY_VALUE3", "0");
			}
			if (!objJson.has("TABLE_CHAR_KEY1")) {
				objJson.put("TABLE_CHAR_KEY1", JSONObjectImpl.NULL);
			}
			if (!objJson.has("TABLE_CHAR_KEY2")) {
				objJson.put("TABLE_CHAR_KEY2", JSONObjectImpl.NULL);
			}
			if (!objJson.has("TABLE_CHAR_KEY3")) {
				objJson.put("TABLE_CHAR_KEY3", JSONObjectImpl.NULL);
			}
		} else {
			objJson.put("TABLE_KEY_VALUE1", table_key_value1);
			objJson.put("TABLE_KEY_VALUE2", table_key_value2);
			objJson.put("TABLE_KEY_VALUE3", table_key_value3);
			objJson.put("TABLE_CHAR_KEY1", table_char_key1);
			objJson.put("TABLE_CHAR_KEY2", table_char_key2);
			objJson.put("TABLE_CHAR_KEY3", table_char_key3);
		}

		if (master_details_flag != null) {
			objJson.put("MASTER_DETAILS_FLAG", master_details_flag);
		}

		if (isPrimaryUpdated) {
			objJson.put("PRIMARY_UPD", "Y");
		} else {
			objJson.put("PRIMARY_UPD", "N");
		}

		return objJson;
	}

	/*
	 * @param jPrimaryKey a primary key of the record
	 * 
	 * @param objJson a JSON object to store key-value pairs This method is used to
	 * separate primary key values by their data types and assign them to
	 * corresponding keys in the JSON object
	 */
	private void extractKeyValuePairs(JSONObjectImpl jPrimaryKey, JSONObjectImpl objJson) {
		int numberKeyIndex = 1;
		int charKeyIndex = 1;

		for (String key : jPrimaryKey.keySet()) {
			// if key condition matches then skip to assign value
			if (key.startsWith("TYPE_NAME_") || key.equalsIgnoreCase(COMP_CD) || key.equalsIgnoreCase(BRANCH_CD)
					|| key.equalsIgnoreCase("ENTERED_BRANCH_CD") || key.equalsIgnoreCase("ENTERED_COMP_CD"))
				continue;

			String value = jPrimaryKey.getString(key);
			String type = jPrimaryKey.getString("TYPE_NAME_" + key);

			if ("NUMBER".equalsIgnoreCase(type)) {
				if (numberKeyIndex == 1) {
					objJson.put("TABLE_KEY_VALUE1", value.isEmpty() ? "0" : value);
				} else if (numberKeyIndex == 2) {
					objJson.put("TABLE_KEY_VALUE2", value.isEmpty() ? "0" : value);
				} else if (numberKeyIndex == 3) {
					objJson.put("TABLE_KEY_VALUE3", value.isEmpty() ? "0" : value);
				}
				numberKeyIndex++;
			} else if ("CHAR".equalsIgnoreCase(type) || "VARCHAR".equalsIgnoreCase(type)
					|| "VARCHAR2".equalsIgnoreCase(type)) {
				if (charKeyIndex == 1) {
					objJson.put("TABLE_CHAR_KEY1", value);
				} else if (charKeyIndex == 2) {
					objJson.put("TABLE_CHAR_KEY2", value);
				} else if (charKeyIndex == 3) {
					objJson.put("TABLE_CHAR_KEY3", value);
				}
				charKeyIndex++;
			}
		}
	}

	/*
	 * @param columnName a column name of table name
	 * 
	 * @param columnLabel a column label from request
	 * 
	 * @param columnType
	 * 
	 * @param old_value an old value which is going to be update
	 * 
	 * @param new_value a value which is replacing old value This method is used to
	 * create a JSON object for old and new values based on the column name
	 */
	private JSONObjectImpl createAuditJsonData(String columnName, String columnLabel, String columnType,
			String old_value, String new_value) {
		JSONObjectImpl objJson = new JSONObjectImpl();
		objJson.put("COLUMN_NAME", columnName);
		objJson.put("COLUMN_LABEL", columnLabel);
		objJson.put("COLUMN_STYLE", columnType);
		objJson.put("OLD_VALUE", old_value);
		objJson.put("NEW_VALUE", new_value);
		return objJson;
	}

	protected void insertAuditData(String ls_response, Connection connection, JSONArrayImpl auditDataArray)
			throws Exception {
		insertAuditData(ls_response, connection, auditDataArray, "N");
	}

	/*
	 * @param ls_response a response from executing methods
	 * 
	 * @param connection a Database connection object
	 * 
	 * @param auditDataArray an array from getting created by methods or manually
	 * created This method is used to insert data into audit table
	 */
	protected void insertAuditData(String ls_response, Connection connection, JSONArrayImpl auditDataArray, String flag)
			throws Exception {
		JSONObjectImpl resDataJson = common.ofGetJsonObject(ls_response);
		String branchCd = StringUtils.EMPTY;
		if (ST0.equals(resDataJson.getString(STATUS))) {
			long ll_tran_cd = 0;
			JSONObjectImpl loginUserDetails = getRequestUniqueData.getLoginUserDetailsJson();
			String loginBranchCd = loginUserDetails.getString(BRANCH_CD);

			if (isSchemaEasyNetBanking()) {
				ll_tran_cd = getMaxCd(connection, "ACT_AUDIT_TRAIL", "TRAN_CD", PARA_USE_MAXCD, null);
				AUDITINSERTQUERY = AUDITINSERTQUERYFORNETBANKING;
			} else {
				ll_tran_cd = getMaxCd(connection, "ACT_AUDIT_TRAIL", "TRAN_CD", PARA_USESEQUENCES,
						"EASY_BANK.SEQ_ACT_AUDIT_TRAIL");
			}

			for (int i = 0; i < auditDataArray.length(); i++) {
				JSONObjectImpl objJson = auditDataArray.getJSONObject(i);

				if ("Y".equalsIgnoreCase(flag) && !loginBranchCd.equals(objJson.getString(BRANCH_CD))) {
					branchCd = objJson.getString(BRANCH_CD);
					objJson.remove(BRANCH_CD);
				} else {
					branchCd = loginBranchCd;
				}

				String ls_resData = insertdata.insertDataWithObj(connection, AUDITINSERTQUERY,
						getRequestUniqueData.getCompCode(), branchCd, ll_tran_cd, (i + 1),
						objJson.getString("TABLE_KEY_VALUE1"), objJson.getString("TABLE_KEY_VALUE2"),
						objJson.getString("TABLE_KEY_VALUE3"), objJson.getString("ACTION"),
						objJson.getString("TABLE_NAME"), objJson.getString("COLUMN_NAME"),
						objJson.getString("COLUMN_LABEL"), objJson.getString("COLUMN_STYLE"),
						objJson.getString("OLD_VALUE"), objJson.getString("NEW_VALUE"),
						getRequestUniqueData.getUserName(), getRequestUniqueData.getMachineName(),
						objJson.getString("TABLE_CHAR_KEY1"), objJson.getString("TABLE_CHAR_KEY2"),
						objJson.getString("TABLE_CHAR_KEY3"), objJson.optString("REQUEST_CD", "0"),
						objJson.optString("THROUGH_CHANNEL", StringUtils.EMPTY),
						objJson.optString("VERIFIED_BY", StringUtils.EMPTY),
						objJson.optString("VERIFIED_DATE", StringUtils.EMPTY),
						objJson.optString("VERIFIED_MACHINE_NAME", StringUtils.EMPTY));
				JSONObjectImpl insertResDataJson = common.ofGetJsonObject(ls_resData);

				if (!isSuccessStCode(insertResDataJson.getString(STATUS))) {
					throw new CustomeException(insertResDataJson.toString(), insertResDataJson.getString("MESSAGE"));
				}
			}
		}
	}

	private boolean isSchemaEasyNetBanking() {
		if (SCHEMANAME.equalsIgnoreCase("EASY_NETBANKING")) {
			return true;
		}
		return false;
	}

	/* This method is used to get formated value from object */
	private String getFormatedValue(Object obj) {
		if (obj instanceof Date) {
			try {
				java.util.Date utilDate = new java.util.Date(((Date) obj).getTime());
				DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				return df.format(utilDate);
			} catch (Exception e) {
				return String.valueOf(obj);
			}
		}
		return String.valueOf(obj);
	}

	// For INSERT THE verifified by , verified machinenm , verified date in table
	// null or from login object.
	protected void setAllVerificationFields(boolean lb_isInsertVerificationFeilds) {
		isInsertVerificationFields = lb_isInsertVerificationFeilds;
	}

}