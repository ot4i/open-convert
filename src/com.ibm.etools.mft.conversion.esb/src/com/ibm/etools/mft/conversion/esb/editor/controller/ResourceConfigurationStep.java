/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.controller;

import org.eclipse.core.runtime.IStatus;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.definition.WesbConversionType;
import com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep;
import com.ibm.mb.common.model.Group;

/**
 * @author Zhongming Chen
 * 
 */
public class ResourceConfigurationStep extends BaseStep {

	/**
	 * @param model
	 * @param controller
	 * 
	 */
	public ResourceConfigurationStep(WESBConversionDataType model, Controller controller) {
		super(model, controller);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#getLabel()
	 */
	@Override
	public String getLabel() {
		return WESBConversionMessages.stepResourceOption;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#getDescription
	 * ()
	 */
	@Override
	public String getDescription() {
		return WESBConversionMessages.stepResourceOptionDesc;
	}

	@Override
	protected Group getDefinition() {
		return ((WesbConversionType) DefinitionModelManager.getConversionType(Controller.WESBTYPE)).getResourceConfiguration();
	}

	@Override
	public IStatus pageIsGoingToChange(IBaseConfigurationStep nextStep) {
		// TODO Auto-generated method stub
		return null;
	}

}
