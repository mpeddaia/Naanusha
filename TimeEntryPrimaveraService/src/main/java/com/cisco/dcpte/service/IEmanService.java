/**
 * 
 */
package com.cisco.dcpte.service;

import com.cisco.framework.Service;
import com.cisco.framework.rest.core.BasicResponseHolder;

/**
 * @author egandi
 *
 */
public interface IEmanService extends Service {
	
	/**
	 * getEmanStatus.
	 * 
	 * @author egandi
	
	 * @return the basic response holder
	 */
	public BasicResponseHolder getEmanStatus();

}
