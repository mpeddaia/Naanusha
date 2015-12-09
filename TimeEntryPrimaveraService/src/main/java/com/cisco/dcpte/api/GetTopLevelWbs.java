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
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.dcpte.util.DCPConstants;

/**
 * @author egandi Class 
 * Name: GetTopLevelWbs 
 * Aim: Implementing logic for getTopLevelWbs webservice
 */

public class GetTopLevelWbs {

	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(GetTopLevelWbs.class);


	/**
	 * method gets all Top Level WBS details for given projectId.
	 * 
	 * @param projectId
	 *            the project id
	 * @return ProjectResponse object(It Returns list of Top Level WBS Details)
	 */
	public static ProjectProfileResponse getTopLevelWbs(String projectNumber,
			String excludeWbsCodes, String wbsName) {

		ProjectProfileResponse projectProfileResponse = new ProjectProfileResponse();
		
		LOGGER.info("processing getTopLevelWbs");
		try{
			Connection conn = null;
			OracleCallableStatement callableStmt = null;
				
			try{
				long startTime1 = System.nanoTime();
				conn = DAOCommonUtils.getP6DBConnection();
				LOGGER.debug("Connection Object :"+conn);
				if (conn != null){
					callableStmt = (OracleCallableStatement) conn.prepareCall(PrimaveraSqlConstants.GET_TOP_LEVEL_WBS);
				}
				LOGGER.info("Executing Function:"+PrimaveraSqlConstants.GET_TOP_LEVEL_WBS);
				long endTime1 = System.nanoTime();
				LOGGER.info("Time taken by connection -> "+(endTime1 - startTime1)/1000000+"ms");	
				/* Input parameters */	
				long startTime3 = System.nanoTime();	
				
				callableStmt.setString("p_project_num_i", projectNumber);
					
				callableStmt.setString("p_exclusionWbsCode_i", excludeWbsCodes);
				
				callableStmt.setString("p_wbsName_i", wbsName);
					
				/* Output parameters */
				callableStmt.registerOutParameter("x_cursor_o", OracleTypes.CURSOR);
				LOGGER.debug("set registerOutParameter 5:CURSOR");
					
				callableStmt.registerOutParameter("x_error_details_o",
						OracleTypes.ARRAY, "XXCAS_O.XXCAS_PRJ_ERROR_DETAIL_TAB");
				LOGGER.debug("set registerOutParameter 6:ERROR_DETAILS_SUMMARY");
				long endTime3 = System.nanoTime();
				LOGGER.info("Time taken by regitser out -> "+(endTime3 - startTime3)/1000000+"ms");
				/** Putting logger mechanism */
				LOGGER.info("Input parameters are - "+projectNumber+"~"+wbsName+"~"+excludeWbsCodes);
				long startTime = System.nanoTime();
			
				try {
					callableStmt.execute();				
					LOGGER.info("Stored Proc Executed successfully!");
				} catch (SQLException e) {
					LOGGER.error("Error while executing stored Proc"+ PrimaveraSqlConstants.GET_TOP_LEVEL_WBS,e);
					throw e;
				}
				LOGGER.info("Procedure executed for these input parameters - "+projectNumber+"~"+wbsName+"~"+excludeWbsCodes);
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
				
				ResultSet wbs = (ResultSet) callableStmt.getObject("x_cursor_o");
				LOGGER.debug("Start filling object");
					
				if(errorStatus.equalsIgnoreCase("S")){
					while (wbs.next()) {
						try {	
								//Setting it directly to a string and then checking for null as this approach retrieves details only once
								SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							    SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd");
							    ProjectSnapShot projectSnapShot = new ProjectSnapShot();
							    projectSnapShot.setWbsId("");
							    projectSnapShot.setWbsName("");
							    projectSnapShot.setIsLeafNode("");
							    projectSnapShot.setWbsPlannedStartDate("");
							    projectSnapShot.setWbsPlannedEndDate("");
								
								String wbsId = wbs.getString("wbsId");
								if(null!= wbsId){
									projectSnapShot.setWbsId(wbsId);
								}
								
								String wbsNamee = wbs.getString("wbsName");
								if(null!= wbsNamee)
								{
									projectSnapShot.setWbsName(wbsNamee);  
								}
								
								String isLeafNode = wbs.getString("isLeafNode");
								if(null!= isLeafNode){								
									projectSnapShot.setIsLeafNode(isLeafNode);
								}
								
								String wbsPlannedStartDate = wbs.getString("wbsPlannedStartDate");
								if (null!= wbsPlannedStartDate) {
								    Date date = sdfIn.parse(wbsPlannedStartDate);
									String s = sdfOut.format(date);
									wbsPlannedStartDate = s;
									projectSnapShot.setWbsPlannedStartDate(wbsPlannedStartDate);
								}							
									
								String wbsPlannedEndDate = wbs.getString("wbsPlannedEndDate");
								if (null!= wbsPlannedEndDate) {
									 Date date = sdfIn.parse(wbsPlannedEndDate);
									String s = sdfOut.format(date);
									wbsPlannedEndDate = s;
									projectSnapShot.setWbsPlannedEndDate(wbsPlannedEndDate);
								}															
								projectProfileResponse.getProjectSnapShot().add(projectSnapShot);						   
							}catch (Exception e) {
								LOGGER.error("Error", e);
							} 	
					   	}
					long endTime2 = System.nanoTime();
					LOGGER.info("Time taken by iteration -> "+(endTime2 - startTime2)/1000000+"ms");
					}
					return projectProfileResponse;
			} catch (SQLException se) {
					LOGGER.error("SQL Exception:",se);
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
						LOGGER.error("SQL Exception: ",e);
					}
				}
			}	
		}catch (Exception e){
			LOGGER.error("Exception: ",e);
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
