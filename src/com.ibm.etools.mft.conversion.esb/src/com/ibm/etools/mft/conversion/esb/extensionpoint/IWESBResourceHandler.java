/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1461664013" > 
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

import com.ibm.etools.mft.conversion.esb.model.WESBResource;

/**
 * @author Zhongming Chen
 *
 */
public interface IWESBResourceHandler {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	/**
	 * Conversion preview
	 * 
	 * @param resourceMap
	 * 
	 * @return a list of artifact that can be migrated
	 */
	public WESBResource preview(ConversionContext context);

	/**
	 * Migrate action.
	 * 
	 * @param patternInstance
	 * @param model
	 */
	public void convert(ConversionContext context) throws Exception;

}
