/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3213952397" > 
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

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.extension.render.DefaultLogEntryRenderer;

/**
 * @author Zhongming Chen
 *
 */
public class ConversionUIManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static ConversionUIManager instance = null;

	private HashMap<String, IConversionResultRenderer> resultRenderers = new HashMap<String, IConversionResultRenderer>();

	private HashMap<String, ILogEntryRenderer> logEntryRenderers = new HashMap<String, ILogEntryRenderer>();

	private ILogEntryRenderer defaultLogEntryRenderer = new DefaultLogEntryRenderer();

	public synchronized static ConversionUIManager getInstance() {
		if (instance == null) {
			instance = new ConversionUIManager();
		}
		return instance;
	}

	private ConversionUIManager() {
		loadResultRenderer();
		loadLogEntryRenderer();
	}

	private void loadLogEntryRenderer() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		logEntryRenderers.put(defaultLogEntryRenderer.getType(), defaultLogEntryRenderer);

		try {
			IConfigurationElement[] handlerConfigurations = extensionRegistry
					.getConfigurationElementsFor(WESBConversionConstants.LOG_ENTRY_RENDERER_ID);
			for (IConfigurationElement current : handlerConfigurations) {
				ILogEntryRenderer handler = (ILogEntryRenderer) current
						.createExecutableExtension(WESBConversionConstants.CLASS_ATTRIBUTE_NAME);

				logEntryRenderers.put(handler.getType(), handler);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void loadResultRenderer() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		try {
			IConfigurationElement[] handlerConfigurations = extensionRegistry
					.getConfigurationElementsFor(WESBConversionConstants.CONVERSION_RESULT_RENDERER_ID);
			for (IConfigurationElement current : handlerConfigurations) {
				IConversionResultRenderer handler = (IConversionResultRenderer) current
						.createExecutableExtension(WESBConversionConstants.CLASS_ATTRIBUTE_NAME);

				resultRenderers.put(handler.getId(), handler);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	public HashMap<String, IConversionResultRenderer> getResultRenderers() {
		return resultRenderers;
	}

	public HashMap<String, ILogEntryRenderer> getLogEntryRenderers() {
		return logEntryRenderers;
	}

}
