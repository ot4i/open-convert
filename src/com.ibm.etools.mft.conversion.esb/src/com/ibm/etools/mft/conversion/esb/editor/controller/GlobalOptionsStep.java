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
public class GlobalOptionsStep extends BaseStep {

	/**
	 * @param model
	 * @param controller
	 * 
	 */
	public GlobalOptionsStep(WESBConversionDataType model, Controller controller) {
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
		return WESBConversionMessages.stepGlobalOption;
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
		return WESBConversionMessages.stepGlobalOptionDesc;
	}

	@Override
	protected Group getDefinition() {
		return ((WesbConversionType) DefinitionModelManager.getConversionType(Controller.WESBTYPE)).getGlobalOptions();
	}

	@Override
	public IStatus pageIsGoingToChange(IBaseConfigurationStep nextStep) {
		// TODO Auto-generated method stub
		return null;
	}

}
