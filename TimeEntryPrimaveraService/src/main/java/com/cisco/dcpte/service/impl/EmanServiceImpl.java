/**
 * 
 */
package com.cisco.dcpte.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.cisco.dcpte.eman.EmanServiceDetails;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.service.IEmanService;
import com.cisco.dcpte.util.CommonDCPUtils;
import com.cisco.framework.AbstractService;
import com.cisco.framework.ServiceConstants;
import com.cisco.framework.ServiceContext;
import com.cisco.framework.TransactionContext;
import com.cisco.framework.rest.core.BasicResponseHolder;

/**
 * @author egandi
 * 
 */
public class EmanServiceImpl extends AbstractService implements IEmanService {
	private static final Logger LOGGER = Logger
			.getLogger(EmanServiceImpl.class);

	private String contentType = "application/json";

	/**
	 * Method name: getEmanStatus Aim: This method is called to get the eman
	 * status This Method passes the parameters to another Class.
	 * 
	 * @param httpVerb
	 * @param projectProfileResponse
	 * @return BasicResponseHolder
	 */

	public BasicResponseHolder getEmanStatus() {

		BasicResponseHolder responseHolder = new BasicResponseHolder();
		getHeaderDetails(responseHolder);
		ProjectProfileResponse projectProfileResponse = EmanServiceDetails
				.getEmanDetails();
		if (projectProfileResponse != null) {
			responseHolder.setData(projectProfileResponse);
		}
		return responseHolder;
	}

	public void getHeaderDetails(BasicResponseHolder responseHolder) {
		CommonDCPUtils commonUtils = new CommonDCPUtils();

		TransactionContext transcContext = ServiceContext
				.getTransactionContext();
		Map<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders = (Map<String, String>) transcContext
				.getProperty(ServiceConstants.TRANSPORT_IN_HEADERS);
		LOGGER.debug("HttpHeader:" + httpHeaders);

		/* test for null values */
		if (httpHeaders != null) {
			contentType = commonUtils.getContentType(httpHeaders);
		}
		LOGGER.debug("ContentType:" + contentType);
		responseHolder.setContentType(contentType);
	}
}
