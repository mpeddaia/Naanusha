package com.cisco.dcpte.api;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.cisco.dcpte.dao.PrimaveraConnectionManager;
import com.cisco.dcpte.model.ErrorDetail;
import com.cisco.dcpte.model.ObjectFactory;
import com.cisco.dcpte.model.ProjectProfileResponse;
import com.cisco.dcpte.model.ProjectSnapShot;
import com.cisco.dcpte.util.DCPConstants;
import com.cisco.dcpte.util.P6Constants;
import com.primavera.ServerException;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.BusinessObjectException;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.ProjectCode;
import com.primavera.integration.client.bo.object.ProjectCodeAssignment;
import com.primavera.integration.client.bo.object.WBS;
import com.primavera.integration.network.NetworkException;

/**
 * @author egandi Class 
 * Name: GetTopLevelWbs 
 * Aim: Implementing logic for getTopLevelWbs webservice
 */

public class GetTopLevelWbsAPI {

	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(GetTopLevelWbsAPI.class);

	public static int wbsLevelCount = 0;
	private static ObjectFactory objFactory = new ObjectFactory();

	/**
	 * method gets all Top Level WBS details for given projectId.
	 * 
	 * @param projectId
	 *            the project id
	 * @return ProjectResponse object(It Returns list of Top Level WBS Details)
	 */
	public static ProjectProfileResponse getTopLevelWbs(String projectId,
			String excludeWbsCodes, String wbsName) {

		ProjectProfileResponse response = new ProjectProfileResponse();
		ErrorDetail error = objFactory.createErrorDetail();
		Session session;

		try {
			session = PrimaveraConnectionManager.getSession();
			EnterpriseLoadManager elm = session.getEnterpriseLoadManager();
			String whereClause;
			BOIterator<Project> boIterator;
			whereClause = "Id='" + projectId + "'";
			// load project for getting all TopLevel WBSElements associated with
			// the Project
			boIterator = elm.loadProjects(new String[] { "Id", "ObjectId",
					"Name" }, whereClause, null);
			if (boIterator.hasNext()) {
				while (boIterator.hasNext()) {
					Project project = (Project) boIterator.next();
					BOIterator<WBS> boiWbs = null;

					String wbsWhereClause = null;
					if (wbsName != null && !wbsName.isEmpty()) {
						wbsWhereClause = "lower(Name) like '%"
								+ wbsName.toLowerCase() + "%'";
					}

					// load all topLevel WBS Elements
					if (project != null) {
						boiWbs = project.loadWBSChildren(new String[] { "Code",
								"ParentObjectId", "ProjectId", "Name",
								"ObjectId", "StartDate", "FinishDate" },
								wbsWhereClause, "Name asc");
					}
					while (boiWbs.hasNext()) {
						WBS wbs = (WBS) boiWbs.next();
						LOGGER.info("WBS Code :" + wbs.getCode());
						if (excludeWbsCodes != null) {
							List<String> excludeWbsCodesList = Arrays
									.asList(excludeWbsCodes.split(","));
							if (!excludeWbsCodesList.contains(wbs.getCode())) {
								response = getWbsDetails(response, wbs);
							}
						} else {
							response = getWbsDetails(response, wbs);
						}
					}
				}
			} else {
				error.setErrorMessage(P6Constants.PROJECT_NOT_AVAILBLE);
				error.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
				response.getErrorDetail().add(error);
				return response;
			}
			LOGGER.error("session :" + session);
		} catch (Exception exception) {
			LOGGER.error("Exception :", exception);
			error.setErrorMessage(exception.getMessage());
			error.setErrorCode(DCPConstants.GENERAL_EXCEPTION_ERROR_CODE);
			response.getErrorDetail().add(error);
			return response;
		}

		if (response.getProjectSnapShot().isEmpty()) {
			error.setErrorCode(DCPConstants.WBS_ERROR_CODE);
			error.setErrorMessage(DCPConstants.WBS_ERROR_MSG);
			error.setReturnStatus(DCPConstants.BUSINESS_STATUS_ERROR);
			response.getErrorDetail().add(error);
		}
		return response;
	}

	/**
	 * method gets get WBS details for given WBS.
	 * 
	 * @param ProjectProfileResponse
	 * @param WBS
	 * @return ProjectProfileResponse
	 */
	private static ProjectProfileResponse getWbsDetails(
			ProjectProfileResponse response, WBS wbs)
			throws BusinessObjectException, ServerException, NetworkException {

		ProjectSnapShot projectSnapShot = null;
		int wbsLevel = getWbsLevelCount(wbs);
		String projectLevel = getWBSLevelOfProject(wbs.getProjectId());
		// String existingWbsName = wbs.getName();

		if (projectLevel != null) {
			Integer levelInt = Integer.parseInt(projectLevel);
			int prjLevel = levelInt.intValue();
			if (prjLevel >= wbsLevel) {

				projectSnapShot = getWbsDetails(wbs);

				if (projectSnapShot != null) {
					if (wbsLevel == prjLevel - 1) {
						projectSnapShot.setIsLeafNode("true");
					}
					response.getProjectSnapShot().add(projectSnapShot);
				}
				wbsLevelCount = 0;
			} else {
				wbsLevelCount = 0;
			}// end if

		} else {

			projectSnapShot = getWbsDetails(wbs);

			if (projectSnapShot != null) {
				response.getProjectSnapShot().add(projectSnapShot);
			}
			wbsLevelCount = 0;
		}
		return response;
	}

	/**
	 * Get the WBS Level of the Project
	 * 
	 * @return String
	 */
	public static String getWBSLevelOfProject(String projectId) {

		ProjectCodeAssignment projectCodeAssgn = null;
		String whereClause = null;
		Session session = null;
		String codeValue = null;
		String projWbsLevel = null;
		try {
			if (projectId != null) {

				PrimaveraConnectionManager.getInstance();
				session = PrimaveraConnectionManager.getSession();
				EnterpriseLoadManager elm = session.getEnterpriseLoadManager();
				whereClause = "ProjectId='" + projectId
						+ "' AND ProjectCodeTypeName ='"
						+ P6Constants.PROJECT_CODE_TYPE + "'";

				// load project Code Assignment of the specific Project
				BOIterator<ProjectCodeAssignment> boiProjAssgn = elm
						.loadProjectCodeAssignments(new String[] {
								"ProjectCodeTypeName", "ProjectCodeValue",
								"ProjectCodeDescription",
								"ProjectCodeTypeObjectId", "ProjectObjectId" },
								whereClause, null);
				if (boiProjAssgn.hasNext()) {
					while (boiProjAssgn.hasNext()) {

						projectCodeAssgn = boiProjAssgn.next();

						ProjectCode projectCode = projectCodeAssgn
								.loadProjectCode(new String[] { "ObjectId",
										"CodeTypeName", "CodeValue",
										"Description", "CodeTypeObjectId" });
						BOIterator<ProjectCode> boiChildProjcodes = projectCode
								.loadProjectCodeChildren(new String[] {
										"ObjectId", "CodeTypeName",
										"CodeValue", "Description",
										"CodeTypeObjectId" }, null, null);
						while (boiChildProjcodes.hasNext()) {

							ProjectCode childProjectCode = boiChildProjcodes
									.next();
							codeValue = childProjectCode.getCodeValue();
							if (codeValue
									.equalsIgnoreCase(P6Constants.PROJECT_CODE_VALUE_WBS)) {
								projWbsLevel = childProjectCode
										.getDescription();
							}
						}

					}
				} else{
					projWbsLevel = null;
				}
			}
		} catch (Exception exception) {
			LOGGER.error("Exception :", exception);
		}
		return projWbsLevel;
	}

	/**
	 * Get on which level the Given WBS is
	 * 
	 * @return int
	 */
	public static int getWbsLevelCount(WBS wbs) {
		WBS parentWbs = null;

		try {
			parentWbs = wbs.loadParentWBS(new String[] { "Name", "ObjectId" });
			if (parentWbs != null) {
				wbsLevelCount++;
				getWbsLevelCount(parentWbs);
			}
		} catch (Exception exception) {
			LOGGER.error("Exception :", exception);

		}
		return wbsLevelCount;
	}

	/**
	 * method gets get WBS details for given WBS.
	 * 
	 * @param WBS
	 * @return ProjectSnapShot
	 */
	private static ProjectSnapShot getWbsDetails(WBS wbs)
			throws BusinessObjectException, ServerException, NetworkException {
		LOGGER.info("WBS Name :" + wbs.getName());
		ProjectSnapShot projectSnapShot = objFactory.createProjectSnapShot();
		if (wbs.getObjectId() != null) {
			projectSnapShot.setWbsId(wbs.getObjectId().toString());
		}

		if (wbs.getName() != null) {
			projectSnapShot.setWbsName(wbs.getName().toString());
		}
		if (wbs.getStartDate() != null) {
			projectSnapShot.setWbsPlannedStartDate(wbs.getStartDate()
					.toString());
		}
		if (wbs.getFinishDate() != null) {
			projectSnapShot
					.setWbsPlannedEndDate(wbs.getFinishDate().toString());
		}
		BOIterator<WBS> boichildWbs = wbs.loadWBSChildren(new String[] {
				"Name", "ObjectId", "StartDate", "FinishDate" }, null, null);
		if (boichildWbs.hasNext()) {
			projectSnapShot.setIsLeafNode("false");
		} else {
			projectSnapShot.setIsLeafNode("true");
		}

		return projectSnapShot;
	}

}
