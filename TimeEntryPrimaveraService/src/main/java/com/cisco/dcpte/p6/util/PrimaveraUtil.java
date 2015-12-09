package com.cisco.dcpte.p6.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.util.P6Constants;
import com.primavera.ServerException;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.BusinessObjectException;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.ActivityCodeAssignment;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.ResourceAssignment;
import com.primavera.integration.client.bo.object.UDFValue;
import com.primavera.integration.network.NetworkException;

public class PrimaveraUtil {
	private static final Logger LOGGER = Logger
			.getLogger(PrimaveraUtil.class);
	private static String timeZone;
		static Properties prop = null;
		static InputStream in;
		static boolean propsLoaded = false;

		/**
		 * 
		 * Aim: static block to load properties file
		 * 
		 */
		static {

			in = PrimaveraUtil.class
					.getResourceAsStream("/CommonDCP.properties");
			prop = new Properties();
			

			Properties prop1 = new Properties();
			InputStream in1 = null;

			try {
				String propFile = "config.properties";
				in1 = new PrimaveraUtil().getClass()
						.getClassLoader().getResourceAsStream(propFile);
				prop1.load(in1);
				timeZone = prop1.getProperty("timeZone");

				LOGGER.info("The TimeZone value getting from config.properties file is : "
								+ timeZone);
			} catch (Exception fnfe) {
				LOGGER.error("FileNotFoundException",fnfe);
			}finally{
				try{
					if(in1!= null){
						in1.close();
					}
				}catch(IOException ioe){
					LOGGER.error("Exception",ioe);
				}
			}
		

		}

	/**
	 * Method name: getChargeableFlag 
	 * 
	 * @param activity
	 * @throws BusinessObjectException, ServerException, NetworkException
	 * 
	 * @return chargeable
	 * 		 
	 */
	
	public static String getChargeableFlag(Activity activity) throws BusinessObjectException, ServerException, NetworkException {
		LOGGER.info("In getChargeableFlag");
		String[] fields = { "ActivityCodeDescription",
				 "ActivityCodeObjectId", "ActivityCodeTypeName",
				 "ActivityCodeValue", "ActivityId", "ActivityName",
				 "ProjectId", "ProjectObjectId","ActivityCodeTypeScope" };
		String chargeable ="";
		 String sWhereClause = "ActivityCodeTypeName = '"+ P6Constants.CHARGEABLE + "' And ActivityCodeTypeScope = '"+P6Constants.ACTIVITY_CODE_SCOPE_GLOBAL+"'";
		
		 BOIterator<ActivityCodeAssignment> activityCodeAssignmentBO = activity.loadActivityCodeAssignments(fields,sWhereClause, null);
		 if(activityCodeAssignmentBO.hasNext()) {
			 ActivityCodeAssignment activityCodeAssignment = activityCodeAssignmentBO.next();
			 
			 if(activityCodeAssignment.getActivityCodeValue() != null ){
				 chargeable =activityCodeAssignment.getActivityCodeValue().toString();
			 }
		}
		 return chargeable;
	}
	
	/**
	 * Method name: getCustomerPartyInfo 
	 * 
	 * @param prj2
	 * @param projectSnapShot
	 * @throws BusinessObjectException, ServerException, NetworkException
	 * 		 
	 */
	
	public static void getCustomerPartyInfo(Project prj2, ProjectSnapShot projectSnapShot) throws BusinessObjectException, ServerException, NetworkException {	
		LOGGER.info("In getCustomerPartyInfo");
		projectSnapShot.setPrimaryCustomerId("");
		projectSnapShot.setPrimaryCustomerName("");
		projectSnapShot.setEndCustomerId("");
		projectSnapShot.setEndCustomerName("");
		String customerWhereClause = "UDFTypeTitle ='"+P6Constants.END_CUSTOMER_ID+"' or UDFTypeTitle ='"+P6Constants.END_CUSTOMER_NAME+"' or UDFTypeTitle ='"+P6Constants.PRIMARY_CUSTOMER_ID+"' or UDFTypeTitle ='"+P6Constants.PRIMARY_CUSTOMER_NAME+"'";
		BOIterator<UDFValue> udfBoi1= prj2.loadAllUDFValues(new String[] { "UDFTypeTitle","Text", "Description" }, customerWhereClause, null);
		while(udfBoi1.hasNext()){
			UDFValue udfValue1 = udfBoi1.next();
			if (udfValue1 != null) {
				
				String udfTitle=udfValue1.getUDFTypeTitle();
				
				if(udfTitle.equalsIgnoreCase(P6Constants.END_CUSTOMER_ID)){
					if(udfValue1.getText() !=null){
						if("0".equals(udfValue1.getText())){
							projectSnapShot.setEndCustomerId("");
						}else{
							projectSnapShot.setEndCustomerId(udfValue1.getText());
						}
					}
					
				}
				if(udfTitle.equalsIgnoreCase(P6Constants.END_CUSTOMER_NAME)){
					if(udfValue1.getText() !=null){
						if("-".equals(udfValue1.getText())){
							projectSnapShot.setEndCustomerName("");					
							}else{
						projectSnapShot.setEndCustomerName(udfValue1.getText());
						}
					}
					
				}
				
				if(udfTitle.equalsIgnoreCase(P6Constants.PRIMARY_CUSTOMER_ID)){
					if(udfValue1.getText() !=null){
						if("0".equals(udfValue1.getText())){
						projectSnapShot.setPrimaryCustomerId("");
						}else{
						projectSnapShot.setPrimaryCustomerId(udfValue1.getText());
						}
					}
					
				}
				
				if(udfTitle.equalsIgnoreCase(P6Constants.PRIMARY_CUSTOMER_NAME)){
					if(udfValue1.getText() !=null){
						if("-".equals(udfValue1.getText())){
						projectSnapShot.setPrimaryCustomerName("");
						}else{
						projectSnapShot.setPrimaryCustomerName(udfValue1.getText());
						}
					}
					
				}
			}
		}
	}
	/**
	 * Method name: getEffortedLoggedForResource 
	 * 
	 * @param activity
	 * @param cecId
	 * @throws BusinessObjectException, ServerException, NetworkException
	 * @return effortLogged
	 * 		 
	 */
	
	public static String getEffortedLoggedForResource(Activity activity,
			String cecId) throws BusinessObjectException, ServerException, NetworkException {
		LOGGER.info("In getEffortedLoggedForResource");
		String effortLogged = "0";
		
		String whereResourceId ="ResourceId='"+cecId+"'";
		BOIterator<ResourceAssignment> resourceAsgnmntBoi = activity.loadResourceAssignments(new String[] { "ResourceId", "RemainingUnits","ActualUnits" }, whereResourceId, null);
		
		if (resourceAsgnmntBoi.hasNext()) {
			ResourceAssignment resource = resourceAsgnmntBoi.next();
			if (resource != null && resource.getActualUnits() != null) {
				effortLogged = resource.getActualUnits().toString();
			}
		}
		return effortLogged;
	}	
}
