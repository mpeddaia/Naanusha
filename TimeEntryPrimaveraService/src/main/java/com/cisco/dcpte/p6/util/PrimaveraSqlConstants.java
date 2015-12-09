package com.cisco.dcpte.p6.util;

public class PrimaveraSqlConstants {
	
	public static final String GET_EXCEPTION_TASKS_LIST = "{ call admuser.XXCAS_PRJ_TE_WS_PKG.get_excptn_task_list (?, ?,  ? ,?,  ?) }";
	
	public static final String GET_ACTIVITIES_OF_WBS = "{ call admuser.XXCAS_PRJ_TE_WS_PKG.get_wbs_activities (?, ?,  ?, ?,  ?, ?) }";
	
	public static final String GET_TOP_LEVEL_WBS = "{ call admuser.XXCAS_PRJ_TE_WS_PKG.getTopLevelWBS (?, ?,  ?, ?,  ?) }";
}
