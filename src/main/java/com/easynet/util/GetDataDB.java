/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.easynet.util;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.easynet.bean.GetRequestUniqueData;
import com.easynet.configuration.PropConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.LoggerImpl;

import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

/**
 *
 * @author Sagar Umate
 * @date-12/01/2020 This class ate used to get data from database.
 */

@Qualifier("getDataDB")
@Repository
public abstract class GetDataDB extends CommonBase{

	@Autowired
	GetRequestUniqueData getRequestUniqueData;

	@Autowired
	PropConfiguration propConfiguration;

	static Logger logger=LoggerFactory.getLogger(GetDataDB.class);

	@Autowired
	JdbcTemplate jdbcTemplate;	

	/**
	 *This is the abstract method which will must implements in child class because of 
	 *you need to write custom logic for this method to call DB procedure as per service. 
	 * */
	public abstract String ofGetResponseData(String input);

	//	public  String ofGetResponseData(String input) {
	//		String ls_return = null;
	//		String ls_req_flag = null;
	//		String ls_req_type = null;
	//		Connection con = null;
	//		CallableStatement cs = null;
	//		LoggerImpl loggerImpl=null;
	//
	//		try {
	//			loggerImpl=new LoggerImpl();
	//
	//			if (input.trim().substring(0, 1).equals("[")) {
	//				input = (String) input.substring(1, input.length() - 1);
	//			} else {
	//				input = (String) input;
	//			}
	//
	//			JSONObject Jobj = new JSONObject(input);
	//			ls_req_type = Jobj.get("ACTION").toString();
	//			ls_req_flag = "R";
	//
	//			con=jdbcTemplate.getDataSource().getConnection();
	//			
	//			cs=con.prepareCall("{CALL PACK_MOB_PROCESS.PROC_MOB_TRN(?,?,?,?,?,?)}");            
	//			cs.setString(1, ls_req_type);
	//			cs.setString(2, ls_req_flag);            
	//			cs.setString(3, input);
	//			cs.setString(4, "S");
	//			cs.registerOutParameter(5, Types.VARCHAR);
	//			cs.registerOutParameter(6, Types.CLOB);
	//			// execute stored procedure
	//			cs.execute();
	//
	//			Clob clob_data = cs.getClob(6); 
	//			if(clob_data==null) {	
	//				ls_return=ofGetFailedMsg("common.exception",ST999, "Null response get from procedure PACK_MOB_PROCESS.PROC_MOB_TRN.", "(ENP039)");				
	//			}else{
	//				ls_return = clob_data.getSubString(1, (int) clob_data.length());
	//			}
	//
	//		} catch (SQLException ex) {
	//			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP002)","SQLException : ",null );
	//					
	//		} catch (Exception ex) {
	//			
	//			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP001)" );
	//			           
	//		} finally {
	//			//It's important to close the statement when you are done with
	//			if (cs != null) {
	//				try {
	//					cs.close();
	//				} catch (SQLException e) {
	//					/* ignored */
	//				}
	//			}
	//
	//			if (con != null) {
	//				try {
	//					con.close();
	//				} catch (SQLException e) {
	//					/* ignored */
	//				}
	//			}
	//		}
	//		return ls_return;
	//	}

	public String ofGetResponseData(String as_proc_name ,String input) {
		String ls_return = null;       
		Connection con = null;
		CallableStatement cs = null;
		LoggerImpl loggerImpl=null;

		try {
			loggerImpl=new LoggerImpl();

			if (input.trim().substring(0, 1).equals("[")) {
				input = (String) input.substring(1, input.length() - 1);
			} else {
				input = (String) input;
			}

			con=jdbcTemplate.getDataSource().getConnection();
			cs=con.prepareCall("{CALL "+as_proc_name+"(?,?)}");                      
			cs.setString(1, input);                        
			cs.registerOutParameter(2, Types.CLOB);
			// execute stored procedure
			cs.execute();

			Clob clob_data = cs.getClob(2); 
			if(clob_data==null) {
				ls_return=ofGetFailedMSg("common.exception",ST999, "Null response get from procedure "+as_proc_name+".", "(ENP712)");								
			}else{
				ls_return = clob_data.getSubString(1, (int) clob_data.length());
			}

		} catch (SQLException ex) {
			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP094)","SQLException : ",null );

		} catch (Exception ex) {
			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP095)" );

		} finally {
			//It's important to close the statement when you are done with
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					/* ignored */
				}
			}

			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					/* ignored */
				}
			}
		}
		return ls_return;
	}

	/**
	 *This method used for call DB process with different procedure name.
	 *@param as_proc_name -procedure name.
	 *@param as_action -action name.
	 *@param input -input data.
	 *@return return the response data of procedure.
	 * */
	public String ofGetResponseData(String as_proc_name ,String as_action,String input) {
		String ls_return = null;       
		Connection con = null;
		CallableStatement cs = null;
		LoggerImpl loggerImpl=null;

		try {
			loggerImpl=new LoggerImpl();

			if (input.trim().substring(0, 1).equals("[")) {
				input = (String) input.substring(1, input.length() - 1);
			} else {
				input = (String) input;
			}

			con=jdbcTemplate.getDataSource().getConnection();
			cs=con.prepareCall("{CALL "+as_proc_name+"(?,?,?)}");                      
			cs.setString(1, as_action);   
			cs.setString(2, input); 
			cs.registerOutParameter(3, Types.CLOB);
			// execute stored procedure
			cs.execute();

			Clob clob_data = cs.getClob(3); 
			if(clob_data==null) {
				ls_return=ofGetFailedMSg("common.exception",ST999,"Null response get from procedure "+as_proc_name+".", "(ENP679)");								
			}else{
				ls_return = clob_data.getSubString(1, (int) clob_data.length());
			}

		} catch (SQLException ex) {
			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP680)","SQLException : ",null );						

		} catch (Exception ex) {
			ls_return= getExceptionMSg(ex, logger, loggerImpl, "IN:ofGetResponseData", "(ENP681)" );

		} finally {
			//It's important to close the statement when you are done with
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					/* ignored */
				}
			}

			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					/* ignored */
				}
			}
		}
		return ls_return;
	}

//	public int ofsetQueryParameter(Integer li_startParaIndex,Object statementObject,Object... as_input) throws SQLException {
//		PreparedStatement statementExecuteObj = null;
//
//		if(as_input!=null) {
//
//			if(statementObject instanceof CallableStatement ) {
//				statementExecuteObj=(CallableStatement)statementObject;				
//			}else if(statementObject instanceof PreparedStatement ) {
//				statementExecuteObj=(PreparedStatement)statementObject;
//			}
//
//			for (Object object : as_input) {
//
//				li_startParaIndex ++;
//				if (object instanceof Integer) {
//					statementExecuteObj.setLong(li_startParaIndex, Long.valueOf((int) object));
//				} else if (object instanceof Long) {
//					statementExecuteObj.setLong(li_startParaIndex, (Long) object);
//				} else if (object == null) {
//					statementExecuteObj.setObject(li_startParaIndex, null);
//				} else if (object instanceof Double) {
//					statementExecuteObj.setDouble(li_startParaIndex, (double) object);
//				} else if (object instanceof Date) {
//					statementExecuteObj.setTimestamp(li_startParaIndex, new java.sql.Timestamp(((Date) object).getTime()));
//				}else if(object instanceof Blob){
//					statementExecuteObj.setBlob(li_startParaIndex, (Blob) object);
//				}else if(object instanceof Clob){
//					statementExecuteObj.setClob(li_startParaIndex, (Clob) object);				
//				}else if(object instanceof Array) {
//					statementExecuteObj.setArray(li_startParaIndex, (Array) object); 
//				}else {
//					statementExecuteObj.setString(li_startParaIndex, (String) object);
//				}
//				
//				
//			}		
//		}
//		return li_startParaIndex;
//	}
	
	public int ofsetQueryParameter(Integer li_startParaIndex,Object statementObject,Object... as_input) throws SQLException {
		PreparedStatement statementExecuteObj = null;

		if(as_input!=null) {

			if(statementObject instanceof CallableStatement ) {
				statementExecuteObj=(CallableStatement)statementObject;				
			}else if(statementObject instanceof PreparedStatement ) {
				statementExecuteObj=(PreparedStatement)statementObject;
			}

			for (Object object : as_input) {

				li_startParaIndex ++;
				if (object instanceof Integer) {
					statementExecuteObj.setLong(li_startParaIndex, Long.valueOf((int) object));
				} else if (object instanceof Long) {
					statementExecuteObj.setLong(li_startParaIndex, (Long) object);
				} else if (object == null) {
					statementExecuteObj.setObject(li_startParaIndex, null);
				} else if (object instanceof Double) {
					statementExecuteObj.setDouble(li_startParaIndex, (double) object);
				} else if (object instanceof Date) {
					statementExecuteObj.setTimestamp(li_startParaIndex, new java.sql.Timestamp(((Date) object).getTime()));
				}else if(object instanceof Blob){
					statementExecuteObj.setBlob(li_startParaIndex, (Blob) object);
				}else if(object instanceof Clob){
					statementExecuteObj.setClob(li_startParaIndex, (Clob) object);				
				}else if(object instanceof NClob) {
					statementExecuteObj.setNClob(li_startParaIndex, (NClob) object);
				}else if(object instanceof Array) {
					statementExecuteObj.setArray(li_startParaIndex, (Array) object); 
				}else {
					statementExecuteObj.setString(li_startParaIndex, (String) object);
				}
			}		
		}
		return li_startParaIndex;
	}
	
	public JSONArrayImpl getResultSetData(ResultSet resultSet) throws JSONException, SQLException {
		JSONArrayImpl responseDataJlist = new JSONArrayImpl();

		ResultSetMetaData resultSetMetaData=resultSet.getMetaData();
		int li_columnCount=resultSetMetaData.getColumnCount();
	
		while (resultSet.next()){
			JSONObjectImpl responseDataJson = new JSONObjectImpl();
			
			for(int i=1;i<=li_columnCount;i++) {
				String ColumnName=resultSetMetaData.getColumnName(i);
				Object columnValue=resultSet.getObject(ColumnName);
				
				if(columnValue == null) {
					responseDataJson.put(ColumnName,"");		
				}else if(columnValue instanceof Clob) {
					responseDataJson.put(ColumnName,((Clob)columnValue).getSubString(1, (int) ((Clob)columnValue).length()));
				}else if(columnValue instanceof Blob) {
					responseDataJson.put(ColumnName,readBlobAsString((Blob)columnValue));
				}else if(columnValue instanceof Array) {
					JSONArrayImpl jRetValue = new JSONArrayImpl();					
					Array returnarray=(Array) columnValue;
					if(returnarray != null) {
						Object[] obj = (Object[]) returnarray.getArray();
						for (Object object : obj) {
							if(object instanceof String) {
								jRetValue.put(object);
							}else if(object instanceof STRUCT) {	
								jRetValue.put(structToJSONObject((STRUCT) object));
							}else {
								jRetValue.put(String.valueOf(object));
							}
						}
					}
					returnarray.free();	
					responseDataJson.put(ColumnName, jRetValue);
				}else {
					responseDataJson.put(ColumnName,columnValue);					
				}									
			}	
			
			responseDataJlist.put(responseDataJson);
		}
		return responseDataJlist;
	}
	
	public String readBlobAsString(Blob logBlob) throws SQLException {

		if (logBlob == null) {
			return StringUtils.EMPTY;
		}

		byte[] ba = logBlob.getBytes(1L, (int) (logBlob.length()));
		if (ba == null) {
			return StringUtils.EMPTY;
		}
		String baString = Base64.encodeBase64String(ba);		
		return baString;
	}

	public int ofsetProcOutParameter(Integer li_startParaIndex,CallableStatement callableStatement,Object... as_outParainput) throws SQLException {

		li_startParaIndex=li_startParaIndex==null?0:li_startParaIndex;
		
		if(as_outParainput!=null) {

			for (Object object : as_outParainput) {
				
				li_startParaIndex ++;
				
				callableStatement.registerOutParameter(li_startParaIndex,(int) object);				
			}		
		}
		return li_startParaIndex;
	}
	
	private static JSONObjectImpl structToJSONObject(STRUCT struct) throws SQLException {
		
		JSONObjectImpl jsonObject = new JSONObjectImpl();
		
		Object[] attributes = struct.getAttributes();
		StructDescriptor structDescriptor = struct.getDescriptor();
		ResultSetMetaData structDesc = structDescriptor.getMetaData();
		for (int i = 1; i <= structDesc.getColumnCount(); i++) {
			jsonObject.put(structDesc.getColumnName(i), attributes[i - 1] == null ? "" : attributes[i - 1]);
		}

		return jsonObject;
    }

}
