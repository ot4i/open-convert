/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3595995783" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2010, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import com.ibm.etools.mft.conversion.esb.extension.resource.WESBLibConverter;
import com.ibm.etools.mft.conversion.esb.extension.resource.WESBModuleConverter;

/**
 * @author Zhongming Chen
 *
 */
public class WESBResourceManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static IWESBResourceHandler wesbLibHandler = new WESBLibConverter();
	private static IWESBResourceHandler mediationModuleHandler = new WESBModuleConverter();

	public static IWESBResourceHandler getWESBLibConverter() {
		return wesbLibHandler;
	}

	public static IWESBResourceHandler getWESBModuleConverter() {
		return mediationModuleHandler;
	}

}
