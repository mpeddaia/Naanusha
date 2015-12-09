package com.cisco.dcpte.api;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.PrimaveraConnectionManager;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.p6.util.PrimaveraUtil;
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.dcpte.util.P6Constants;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.network.NetworkException;

/**
 * 
 * @author egandi
 * @version 1.2 This class gets activity details for Project ID
 * 
 */

public class GetActivityDetailsForProjectId {
	private static final Logger LOGGER = Logger
			.getLogger(GetActivityDetailsForProjectId.class);
	private static BOIterator<Activity> activities = null;
	private static Activity activity = null;
	private static BOIterator<Project> projects = null;
	private static Project project =  null;
	
	private static EnterpriseLoadManager elm = null;
	private static Session session = null;
	private static String timeZone;
	static
 {
		Properties prop = new Properties();
		InputStream in = null;

		try {
			String propFile = "config.properties";
			in = new GetActivityDetailsForProjectId().getClass()
					.getClassLoader().getResourceAsStream(propFile);
			prop.load(in);
			timeZone = prop.getProperty("timeZone");

			LOGGER.info("The TimeZone value getting from config.properties file is : "
							+ timeZone);
		} catch (Exception fnfe) {
			LOGGER.error("FileNotFoundException",fnfe);
		}finally{
			try{
				if(in!= null){
					in.close();
				}
			}catch(IOException ioe){
				LOGGER.error("Exception",ioe);
			}
		}
	}
	
	/**
	 * Method name: getActivitiesByPID 
	 * 
	 * @param projectSnapShot
	 * @param projectId
	 * @param activityId
	 * @param cecId
	 * @throws ServerException, NetworkException, ClientException
	 */
	
	public static void getActivitiesByPID(ProjectSnapShot projectSnapShot , String projectId , String activityId, String cecId) throws ServerException, NetworkException, ClientException  {
		// Using PrimaveraConnectionManager class to get the database instance
		PrimaveraConnectionManager.getInstance();
		session = PrimaveraConnectionManager.getSession();
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		
		LOGGER.info("Session Object :" +session ); 
		GetActivityDetailsForWbsAPI getActivitiesForWbs = new GetActivityDetailsForWbsAPI();
		String whereProjectId = "Id = '"+projectId.trim()+"'";
		String whereActivityId = "Id = '"+activityId.trim()+"'";
		
		projectSnapShot.setActivityName("");
		projectSnapShot.setActivityStartDate("");
		projectSnapShot.setActivityEndDate("");
		projectSnapShot.setActivityObjectId("");
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		
		elm = session.getEnterpriseLoadManager(); 
		projects = elm.loadProjects(new String[] { "Id", "Name", "Status" },whereProjectId, null);
		LOGGER.info("projects :" + projects.hasNext());  
		if(projects.hasNext()){
			
			project = projects.next();
			activities =  project.loadAllActivities(new String[] { "Id","Status","ObjectId", "ProjectId", "Name" ,"ActualStartDate","ActualFinishDate","RemainingEarlyStartDate","RemainingEarlyFinishDate"} , whereActivityId , null);
			
			if(activities.hasNext()){
				
				activity = activities.next();
				
				if(activity.getName() != null) {
					projectSnapShot.setActivityName(activity.getName());
				}
				if(activity.getObjectId() != null) {
					projectSnapShot.setActivityObjectId(activity.getObjectId().toString());
				}
				
				if(activity.getStatus() != null){
					CommonDCPUtils.getActivtyDates(activity, projectSnapShot);
				}			
				
				String teAllowedFlagOTL = projectSnapShot.getTeAllowedFlag();
				LOGGER.info("teAllowedFlagOTL :"+teAllowedFlagOTL);
				String chargeableFlag = getActivitiesForWbs.getChargeableFlagForActivity(activity);
				LOGGER.info("chargeableFlag :"+chargeableFlag);
							
				if(P6Constants.CHARGEABLE_FLAG_YES.equalsIgnoreCase(chargeableFlag) && P6Constants.ELIGIBLE_FOR_TE_Y.equalsIgnoreCase(teAllowedFlagOTL)){					
					projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_Y);
				}else if(P6Constants.CHARGEABLE_FLAG_YES.equalsIgnoreCase(chargeableFlag) && P6Constants.ELIGIBLE_FOR_TE_E.equalsIgnoreCase(teAllowedFlagOTL)){
					projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_E);
				}else{
					projectSnapShot.setTeAllowedFlag(P6Constants.ELIGIBLE_FOR_TE_N);
				}
				
				String p6EffortLogged ="0";
				String otlEffortLogged="0";
				
				otlEffortLogged =projectSnapShot.getEffortLogged();
				LOGGER.info("otlEffortLogged = "+otlEffortLogged);
				if(cecId != null && !(cecId.isEmpty())){
				   p6EffortLogged = PrimaveraUtil.getEffortedLoggedForResource(activity, cecId);
				}
				LOGGER.info("p6EffortLogged = "+p6EffortLogged);
				if(null != p6EffortLogged && !(p6EffortLogged.isEmpty())){
					
					Double p6EffortLoggedTemp = Double.parseDouble(p6EffortLogged);
					Double otlEffortLoggedTemp = Double.parseDouble(otlEffortLogged);
					
					Double totalEffortLoggedTemp = p6EffortLoggedTemp+otlEffortLoggedTemp;
					
					DecimalFormat df = new DecimalFormat("#0.00");
					String totalEffortLogged = df.format(totalEffortLoggedTemp);
					LOGGER.info("totalEffortLogged = "+totalEffortLogged);
					
					projectSnapShot.setEffortLogged(totalEffortLogged);
					
					
				}
			}
		}
	} 	
}