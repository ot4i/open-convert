/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2013" 
 *  crc="1448658572" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *   
 *   
 *  (C) Copyright IBM Corp. 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 **********************************************************************/
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.List;

import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.conversion.esb.model.GlobalConfigurationType;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.wizard.editor.property.editors.AbstractCustomPropertyEditor;
import com.ibm.etools.patterns.IPatternConstants;
import com.ibm.etools.patterns.model.edit.event.EventActionsConfigurationManager;
import com.ibm.etools.patterns.model.edit.event.IPOVEditorEvent;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class WESBConversionParameterEditor extends AbstractCustomPropertyEditor {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public void changed() {
		setChanged();
		List<IPOVEditorEvent> events = EventActionsConfigurationManager.getEventsForMethod(
				IPatternConstants.VALUE_CHANGED_EVENT_TYPE, getValue());
		notifyObservers(events);
	}

	protected WESBConversionDataType getModel() {
		WESBConversionDataType data = (WESBConversionDataType) getPropertyEditorHelper().getController().getModel();
		if (data.getGlobalConfiguration() == null) {
			data.setGlobalConfiguration(new GlobalConfigurationType());
		}
		return data;
	}

	public ConversionLog getLog() {
		return ((Controller) getPropertyEditorHelper().getController()).getLog();
	}

	public Controller getController() {
		return (Controller) getPropertyEditorHelper().getController();
	}
}
