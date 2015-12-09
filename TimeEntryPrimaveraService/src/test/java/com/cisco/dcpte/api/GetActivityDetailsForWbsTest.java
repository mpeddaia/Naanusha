package com.cisco.dcpte.api;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.primavera.ServerException;
import com.primavera.integration.client.ClientException;
import com.primavera.integration.network.NetworkException;

public class GetActivityDetailsForWbsTest {

	
	public GetActivityDetailsForWbsTest() {
		/*
		 * This constructor should not be modified. Any initialization code
		 * should be placed in the setUp() method instead.
		 */

	}

	/**
	 * Used to set up the test. This method is called by JUnit before each of
	 * the tests are executed.
	 *
	 * @throws Exception the exception
	 * @author Parasoft Jtest 9.5
	 */
	@Before
	public void setUp() throws Exception {
		/*
		 * Add any necessary initialization code here (e.g., open a socket).
		 * Call Repository.putTemporary() to provide initialized instances of
		 * objects to be used when testing.
		 */

	}

	/**
	 * Used to clean up after the test. This method is called by JUnit after
	 * each of the tests have been completed.
	 *
	 * @throws Exception the exception
	 * @author Parasoft Jtest 9.5
	 */
	@After
	public void tearDown() throws Exception {
		try {
			/*
			 * Add any necessary cleanup code here (e.g., close a socket).
			 */
		} finally {
		}
	}

	
	public static void main(String[] args) {

		org.junit.runner.JUnitCore
				.main("com.cisco.primavera.api.GetActivityDetailsForWbsTest");
	}
	
	
	@Test
	public void testActivitiesByWbsId() throws ServerException, NetworkException, ClientException {
		
		String wsbId="682526";
		String taskTypes="Pre-Sales";
		String cecId ="sganjam";
		String excludeWbsCodes=null;
		
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	//DCPRM logic
	@Test
	public void testActivitiesByWbsIdDCPRM() throws ServerException, NetworkException, ClientException {
		
		String wsbId="682526";
		String taskTypes="Pre-Sales";
		String cecId ="DCPRM123";
		String excludeWbsCodes=null;
		
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	@Test
	public void testActivitiesByWbsIdWithoutTaskType() throws ServerException, NetworkException, ClientException {
		
		String wsbId="682526";
		String taskTypes=null;
		String cecId ="sganjam";
		String excludeWbsCodes="1000.3";
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testActivitiesByWbsIdForNoWBS() throws ServerException, NetworkException, ClientException {
		
		String wsbId="39133";
		String taskTypes="Hardware,Exception";
		String cecId ="prawasth";
		String excludeWbsCodes="1000.3";
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testActivitiesByWbsIdForAllStatus() throws ServerException, NetworkException, ClientException {
		
		String wsbId="39134";
		String taskTypes="Hardware,Pre-Sales";
		String cecId ="pmuthumu";
		String excludeWbsCodes=null;
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	
	@Test
	public void testActivitiesByWbsIdForNoTaskTypeMatch() throws ServerException, NetworkException, ClientException {
		
		String wsbId="682526";
		String taskTypes="Travel1";//wrong tasktype
		String cecId ="sganjam";
		String excludeWbsCodes=null;
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());

		}
	}
	@Test
	public void testActivitiesByWbsIdNull() throws ServerException, NetworkException, ClientException {
		
		String wsbId=null;
		String taskTypes="Travel1";
		String cecId ="sashwin";
		String excludeWbsCodes=null;
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		ErrorDetail errorDetail = response.getErrorDetail().get(0);
		
		if ((errorDetail) instanceof ErrorDetail) {
			Assert.assertNotNull( response.getErrorDetail().get(0));

		}
	}
	@Test
	public void testActivitiesByWbsNotFound() throws ServerException, NetworkException, ClientException {
		
		String wsbId="391335";
		String taskTypes="Travel1";
		String cecId ="sashwin";
		String excludeWbsCodes=null;
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		ErrorDetail errorDetail = response.getErrorDetail().get(0);		
		if ((errorDetail) instanceof ErrorDetail) {
			Assert.assertNotNull( response.getErrorDetail().get(0));

		}
	}
	@Test
	public void testActivitiesByNotstarted() throws ServerException, NetworkException, ClientException {
		
		String wsbId="39257";
		String taskTypes="Exception,Hardware,T & E";
		String cecId ="sashwin";
		String excludeWbsCodes ="1000.3";
       
		GetActivityDetailsForWbs getActWbs = new GetActivityDetailsForWbs();
		ProjectProfileResponse response = getActWbs.getActivityDetails(cecId, wsbId, taskTypes,excludeWbsCodes);				
		
		if ((response) instanceof ProjectProfileResponse) {
			Assert.assertNotNull(response.getProjectSnapShot());
		}
	}
	
	public Class<GetActivityDetailsForWbsTest> getTestedClass() {
		return GetActivityDetailsForWbsTest.class;
	}



}
