package com.cisco.dcpte.service;

import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.framework.Service;
import com.cisco.framework.rest.core.BasicResponseHolder;
import com.cisco.framework.rest.core.HttpVerb;

/**
 * The Interface ITimeEntryPrimaveraService.
 */
public interface ITimeEntryPrimaveraService extends Service {

	/**
	 * getActivitiesOfWBS.
	 * 
	 * @author zkhaliq
	 * @param projectProfileResponse
	 *            the request
	 * @param verb
	 *            the verb
	 * @return the basic response holder
	 */
	public BasicResponseHolder getActivitiesOfWBS(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);
	
	public BasicResponseHolder getActivitiesOfWBSAPI(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);
	/**
	 * getTopLevelWBS.
	 * 
	 * @author zkhaliq
	 * @param projectProfileResponse
	 *            the request
	 * @param verb
	 *            the verb
	 * @return the basic response holder
	 */
	public BasicResponseHolder getTopLevelWBSAPI(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);

	public BasicResponseHolder getTopLevelWBS(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);

	/**
	 * getExceptionTasksList.
	 * 
	 * @author naanusha
	 * @param projectProfileResponse
	 *            the request
	 * @param verb
	 *            the verb
	 * @return the basic response holder
	 */
	public BasicResponseHolder getExceptionTasksList(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);
	/**
	 * updateTimeCardDetails.
	 * 
	 * @author egandi
	 * @param projectProfileResponse
	 *            the request
	 * @param verb
	 *            the verb
	 * @return the basic response holder
	 */
	public BasicResponseHolder updateTimeCardDetails(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponseinput);
	
	/**
	 * getMyActivities.
	 * 
	 * @author egandi
	 * @param projectProfileResponse
	 *            the request
	 * @param verb
	 *            the verb
	 * @return the basic response holder
	 */
	public BasicResponseHolder getActivities(final HttpVerb verb,
			final ProjectProfileResponse projectProfileResponse);


	/**
	 * getEmanStatus.
	 * 
	 * @author egandi
	
	 * @return the basic response holder
	 */
	public BasicResponseHolder getEmanStatus();	
}