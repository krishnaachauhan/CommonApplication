package com.easynet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.easynet.impl.JSONObjectImpl;
import com.easynet.impl.JSONArrayImpl;
import com.easynet.impl.LoggerImpl;
import com.easynet.util.GetDataDB;
import com.easynet.util.common;
import static com.easynet.util.ConstantKeyValue.*;

@Repository
public class UpdateData extends GetDataDB{
	
	static Logger LOGGER=LoggerFactory.getLogger(UpdateData.class);
	
	public String doUpdateData(String Query , Object... as_input) throws SQLException
	{
		String ls_return = null;       
		Connection connection = null;
		LoggerImpl loggerImpl=null;
		String ls_updateResData = StringUtils.EMPTY;
		JSONObjectImpl updateResDataJson;

		try {
			loggerImpl=new LoggerImpl();
			
			connection=getDbConnection();
			
			ls_updateResData=doUpdateData(connection,Query,as_input);
			
			updateResDataJson=common.ofGetJsonObject(ls_updateResData);

			if (isSuccessStCode(updateResDataJson.getString(STATUS))) {
				connection.commit();									
			}else {
				connection.rollback();
			}					
			return ls_updateResData;
			
		} catch (Exception exception) {
			connection.rollback();
			ls_return= getExceptionMSg(exception,LOGGER,loggerImpl,"IN:doUpdateData", "(ENP027)" );
			    
		} finally {
			//It's important to close the statement when you are done with
			closeDbObject(connection);			
		}
		return  ls_return;
	}
	
	public String doUpdateData(Connection connection ,String Query , Object... as_input) throws Exception{
		
		PreparedStatement preparedStatement = null;
		String ls_return = null;
		LoggerImpl loggerImpl=null;
		int parameterIndex=0;
		int rowcount;
		
		try {
			
			loggerImpl=new LoggerImpl();			
			preparedStatement=connection.prepareStatement(Query);
			
			ofsetQueryParameter(parameterIndex, preparedStatement, as_input);
			
			rowcount = preparedStatement.executeUpdate();
			
			if(rowcount == 0){
				//return common.ofGetResponseJson(new JSONArrayImpl(), "", "Data Updation Fail...", ST999,"G","Data Updation Fail...").toString();
				return ofGetResponseJson(new JSONArrayImpl(), "", "Data Updation Fail...", ST999,"G","common.update_fail").toString();
			}
			
			//return common.ofGetResponseJson(new JSONArrayImpl(), "", "Data Updated Successfully.", ST0,"G","Data Updated Successfully.").toString();
			return ofGetResponseJson(new JSONArrayImpl(), "", "Data Updated Successfully.", ST0,"G","common.update_suc").toString();
			
		}catch (SQLException sqlException){			
				if(sqlException.getErrorCode() == 00001){
				ls_return =  common.ofGetResponseJson(new JSONArrayImpl(), "", "Record Already Exists.", ST99,"R","Record Already Exists.").toString();

			}else{											
			ls_return= getExceptionMSg(sqlException, LOGGER, loggerImpl, "IN:doUpdateData", "(ENP028)","SQLException : ",null );			
			}
		} catch (Exception exception) {
			ls_return= getExceptionMSg(exception, LOGGER, loggerImpl, "IN:doUpdateData", "(ENP029)" );
			    
		} finally {
			//It's important to close the statement when you are done with
			closeDbObject(preparedStatement);
		}
		return ls_return;
	}

	@Override
	public String ofGetResponseData(String input) {
		// TODO Auto-generated method stub
		return null;
	}
}
