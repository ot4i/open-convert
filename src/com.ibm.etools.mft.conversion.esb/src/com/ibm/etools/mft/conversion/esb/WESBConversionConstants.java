/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="186021502" > 
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
package com.ibm.etools.mft.conversion.esb;

/**
 * @author Zhongming Chen
 * 
 */
public interface WESBConversionConstants {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	// Extension point ids
	public static final String WESB_RESOURCE_CONVERTER_ID = "com.ibm.etools.mft.conversion.esb.WESBResourceConverter"; //$NON-NLS-1$
	public static final String MEDIATION_PRIMITIVE_HANDLER_ID = "com.ibm.etools.mft.conversion.esb.MediationPrimitiveConverter"; //$NON-NLS-1$
	public static final String BINDING_CONVERTER_ID = "com.ibm.etools.mft.conversion.esb.BindingConverter"; //$NON-NLS-1$
	public static final String CONVERSION_RESULT_RENDERER_ID = "com.ibm.etools.mft.conversion.esb.ResultRenderer"; //$NON-NLS-1$
	public static final String LOG_ENTRY_RENDERER_ID = "com.ibm.etools.mft.conversion.esb.LogEntryRenderer"; //$NON-NLS-1$

	// Extension point attribute ids
	public static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$

	public static final String XSD_EXTENSION = "xsd"; //$NON-NLS-1$
	public static final String WSDL_EXTENSION = "wsdl"; //$NON-NLS-1$

	public static final String INPUT_NODE_SUFFIX = "_input"; //$NON-NLS-1$
	public static final String OUTPUT_NODE_SUFFIX = "_output"; //$NON-NLS-1$
	public static final String REPLY_NODE_SUFFIX = "_reply"; //$NON-NLS-1$
	public static final String ROUTE_TO_LABEL_SUFFIX = "_routeToLabel"; //$NON-NLS-1$
	public static final String REQUEST_FLOW_SUFFIX = "_Request"; //$NON-NLS-1$
	public static final String REQUEST_RESPONSE_FLOW_SUFFIX = "_Request_Response"; //$NON-NLS-1$
	public static final String IN_TERMINAL = "In"; //$NON-NLS-1$
	public static final String OUT_TERMINAL = "Out"; //$NON-NLS-1$
	public static final String FAIL_TERMINAL = "Failure"; //$NON-NLS-1$
	public static String PROP_MFC_FILE_PATH = "PROP_MFC_FILE_PATH"; //$NON-NLS-1$

	public static final String MODULE = "MODULE"; //$NON-NLS-1$
	public static final String LIB = "LIB"; //$NON-NLS-1$
	public static final String IIB_APPLICATION = "iibApplication"; //$NON-NLS-1$
	public static final String IIB_SERVICE = "iibService"; //$NON-NLS-1$

	public static final String PACKAGE_MFC = "com.ibm.etools.mft.conversion.esb.model.mfc"; //$NON-NLS-1$
	public static String PACKAGE_WESB_CONVERSION_TYPE = "com.ibm.etools.mft.conversion.esb.model"; //$NON-NLS-1$
	public static String PACKAGE_MAPPING = "com.ibm.etools.mft.conversion.esb.model.mapping"; //$NON-NLS-1$

	public static final String MEDIATION_MODULE_NATURE = "com.ibm.etools.mft.conversion.esb.mediationmodule"; //$NON-NLS-1$
	public static final String WESB_LIBRARY_NATURE = "com.ibm.etools.mft.conversion.esb.wesblibrary"; //$NON-NLS-1$

	public static final String ESB_LIB_NATURE = "com.ibm.wbit.project.sharedartifactmodulenature"; //$NON-NLS-1$
	public static final String ESB_MODULE_NATURE = "com.ibm.wbit.project.generalmodulenature"; //$NON-NLS-1$

	public static final String SUFFIX_OF_MEDIATION_PRIMITIVE_IN_RESPONSE_FLOW = "_In_Response_Flow"; //$NON-NLS-1$

	public static String TYPE_PROJECT = "project"; //$NON-NLS-1$
	public static String TYPE_MAPS = "maps"; //$NON-NLS-1$
	public static String TYPE_MODULE = "MODULE"; //$NON-NLS-1$

	public static String CONVERSION_SESSION_EXT = ".conversion"; //$NON-NLS-1$
}
