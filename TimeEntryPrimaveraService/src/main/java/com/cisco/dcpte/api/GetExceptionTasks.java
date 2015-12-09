package com.cisco.dcpte.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.DAOCommonUtils;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.p6.util.PrimaveraSqlConstants;
import com.cisco.dcpte.p6.util.PrimaveraUtil;
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.dcpte.util.DCPConstants;
import com.cisco.dcpte.util.P6Constants;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.network.NetworkException;

/**
 * @author naanusha
 * Class Name: GetExceptionTasks 
 * Aim: Implementing logic for getExceptionTasksList webservice
 */

public class GetExceptionTasks {
		
	private static final Logger LOGGER = Logger.getLogger(GetExceptionTasks.class);
	
	/**
	 * @param projectNumber
	 * @param cecId
	 * @param taskTypes
	 * @throws ClientException 
	 * @throws NetworkException 
	 * @throws ServerException 
	 * 
	 * @return projectProfileResponse	
	 */
	public static ProjectProfileResponse getExceptionTasks(String projectNumber, String cecId, String taskTypes, String transId){
		
		ProjectProfileResponse projectProfileResponse = new ProjectProfileResponse();
			
		LOGGER.info("processing getExceptionTasks");
		LOGGER.debug(":"+transId+":Start of getExceptionTasks method");
		try{
			Connection conn = null;
			OracleCallableStatement callableStmt = null;
				
			try{
				long startTime1 = System.nanoTime();
				conn = DAOCommonUtils.getP6DBConnection();
				LOGGER.debug("Connection Object :"+conn);
				if (conn != null){
					callableStmt = (OracleCallableStatement) conn.prepareCall(PrimaveraSqlConstants.GET_EXCEPTION_TASKS_LIST);
				}
				LOGGER.info("Executing Function:"+PrimaveraSqlConstants.GET_EXCEPTION_TASKS_LIST);
				long endTime1 = System.nanoTime();
				LOGGER.info("Time taken by connection -> "+(endTime1 - startTime1)/1000000+"ms");	
				/* Input parameters */	
				long startTime3 = System.nanoTime();
				callableStmt.setString("p_project_num_i", projectNumber);
				
				callableStmt.setString("p_cec_id_i", cecId);
					
				callableStmt.setString("p_incl_task_type_i", taskTypes);
					
				/* Output parameters */
				callableStmt.registerOutParameter("x_cursor_o", OracleTypes.CURSOR);
				LOGGER.debug("set registerOutParameter 4:CURSOR");
					
				callableStmt.registerOutParameter("x_error_details_o",
						OracleTypes.ARRAY, "XXCAS_O.XXCAS_PRJ_ERROR_DETAIL_TAB");
				LOGGER.debug("set registerOutParameter 5:ERROR_DETAILS_SUMMARY");
				long endTime3 = System.nanoTime();
				LOGGER.info("Time taken by regitser out -> "+(endTime3 - startTime3)/1000000+"ms");
				/** Putting logger mechanism */
				LOGGER.info("Input parameters are - "+projectNumber+"~"+cecId+"~"+taskTypes);
				long startTime = System.nanoTime();
			
				try {
					callableStmt.execute();				
					LOGGER.info("Stored Proc Executed successfully!");
				} catch (SQLException e) {
					LOGGER.error(":"+transId+":Error while executing stored Proc"+ PrimaveraSqlConstants.GET_EXCEPTION_TASKS_LIST,e);
					throw e;
				}
				LOGGER.info("Procedure executed for these input parameters - "+projectNumber+"~"+cecId+"~"+taskTypes);
				long endTime = System.nanoTime();
				LOGGER.info("Time taken by procedure -> "+(endTime - startTime)/1000000+"ms");
				
				long startTime2 = System.nanoTime();
					
				ARRAY errorDetails = (ARRAY) callableStmt.getArray("x_error_details_o");
					
				ErrorDetail errDet  = new ErrorDetail();
				String errorStatus = "";
				if (errorDetails != null && errorDetails.length() != 0) {
//					DAOCommonUtils daoCommonUtils = new DAOCommonUtils();
					errDet = CommonDCPUtils.getErrorsList(errorDetails);
					errorStatus = errDet.getReturnStatus();
					if(!(errorStatus.equalsIgnoreCase("S"))){
					projectProfileResponse.getErrorDetail().add(errDet);
					return projectProfileResponse;
					}
				}
				
				ResultSet activities = (ResultSet) callableStmt.getObject("x_cursor_o");
				LOGGER.debug("Start filling object");
					
				if(errorStatus.equalsIgnoreCase("S")){
					while (activities.next()) {
						try {	
								//Setting it directly to a string and then checking for null as this approach retrieves details only once
								SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							    SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd");
							    ProjectSnapShot projectSnapShot = new ProjectSnapShot();
							    projectSnapShot.setActivityId("");
							    projectSnapShot.setActivityName("");
							    projectSnapShot.setEffortLogged("0");
							    projectSnapShot.setActivityStartDate("");
							    projectSnapShot.setActivityEndDate("");
							    projectSnapShot.setActivityChargeable("");
							    projectSnapShot.setLowestTaskNumber("");
								
								String activityId = activities.getString("activityId");
								if(null!= activityId){
									projectSnapShot.setActivityId(activityId);
								}
								
								projectSnapShot.setActivityObjectId("");
								int activityObjectId = activities.getInt("activityObjectId");
								projectSnapShot.setActivityObjectId(String.valueOf(activityObjectId));
								
								String activityName = activities.getString("activityName");
								if(null!= activityName)
								{
									projectSnapShot.setActivityName(activityName);  
								}
								
								String effortLogged = activities.getString("effortLogged");
								if(null!= effortLogged){								
									projectSnapShot.setEffortLogged(effortLogged);
								}
								
								projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_Y);
								
								String activityStartDate = activities.getString("activityStartDate");
								if (null!= activityStartDate) {
								    Date date = sdfIn.parse(activityStartDate);
									String s = sdfOut.format(date);
									activityStartDate = s;
									projectSnapShot.setActivityStartDate(activityStartDate);
								}							
									
								String activityEndDate = activities.getString("activityEndDate");
								if (null!= activityEndDate) {
									 Date date = sdfIn.parse(activityEndDate);
									String s = sdfOut.format(date);
									activityEndDate = s;
									projectSnapShot.setActivityEndDate(activityEndDate);
								}						
									
								String chargeableFlag = activities.getString("chargeablFlag");
								if(chargeableFlag != null && !chargeableFlag.isEmpty() ){
									projectSnapShot.setActivityChargeable(chargeableFlag);
								}
									
								String opLowestTaskNo = activities.getString("opLowestTaskNo");
								if(null!= opLowestTaskNo){								
									projectSnapShot.setLowestTaskNumber(opLowestTaskNo);
								}
										
								String endCustomerId = activities.getString("endCustomerId");
								if("0".equals(endCustomerId) || null == endCustomerId){
									projectSnapShot.setEndCustomerId("");
								}else{
									projectSnapShot.setEndCustomerId(endCustomerId);
								}
									
								String endCustomerName = activities.getString("endCustomerName");
								if("-".equals(endCustomerName) || null == endCustomerName){
									projectSnapShot.setEndCustomerName("");					
								}else{
									projectSnapShot.setEndCustomerName(endCustomerName);
								}
								
								String primaryCustomerId = activities.getString("primaryCustomerId");
								if("0".equals(primaryCustomerId) || null == primaryCustomerId){
									projectSnapShot.setPrimaryCustomerId("");
								}else{
									projectSnapShot.setPrimaryCustomerId(primaryCustomerId);
								}
									
								String primaryCustomerName = activities.getString("primaryCustomerName");
								if("-".equals(primaryCustomerName) || null == primaryCustomerName){
									projectSnapShot.setPrimaryCustomerName("");					
								}else{
									projectSnapShot.setPrimaryCustomerName(primaryCustomerName);
								}										
								projectProfileResponse.getProjectSnapShot().add(projectSnapShot);						   
							}catch (Exception e) {
								LOGGER.error(":"+transId+":Error", e);
							} 	
					   	}
					long endTime2 = System.nanoTime();
					LOGGER.info("Time taken by iteration -> "+(endTime2 - startTime2)/1000000+"ms");
					}
					LOGGER.debug(":"+transId+":End of getExceptionTasks method");
					return projectProfileResponse;
			} catch (SQLException se) {
					LOGGER.error(":"+transId+":SQL Exception:",se);
					ErrorDetail error = new ErrorDetail();
					error.setErrorCode(DCPConstants.STORED_PROC_EXCEPTION_ERROR_CODE);
					error.setErrorMessage(se.getMessage());
					error.setReturnStatus(DCPConstants.STATUS_ERROR);
					projectProfileResponse.getErrorDetail().add(error);
					return projectProfileResponse;
			} finally {
				if (conn != null) {
					try {
						conn.close();
						callableStmt.close();							
					} catch (SQLException e) {
						LOGGER.error(":"+transId+":SQL Exception: ",e);
					}
				}
			}	
		}catch (Exception e){
			LOGGER.error(":"+transId+":Exception: ",e);
			String errorMessage = e.getMessage();
			ErrorDetail errorDet = new ErrorDetail(); 
			errorDet.setErrorMessage(errorMessage);
			errorDet.setErrorCode(DCPConstants.CONNECTION_EXCEPTION_ERROR_CODE);
			errorDet.setReturnStatus(DCPConstants.STATUS_ERROR);
			projectProfileResponse.getErrorDetail().add(errorDet);
			return projectProfileResponse;
		}
	}
}