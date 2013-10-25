/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.controller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.definition.WesbConversionType;
import com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep;
import com.ibm.etools.mft.wizard.editor.model.IConfigurationStep;
import com.ibm.mb.common.model.Group;

/**
 * @author Zhongming Chen
 * 
 */
public class SummaryStep implements IConfigurationStep {

	private WESBConversionDataType model;
	private Group definition;
	private List<Group> groups = new ArrayList<Group>();
	private boolean completed;
	private Controller controller;

	/**
	 * @param model
	 * @param controller
	 * 
	 */
	public SummaryStep(WESBConversionDataType model, Controller controller) {
		this.controller = controller;
		this.model = model;
		this.definition = ((WesbConversionType) DefinitionModelManager.getConversionType(Controller.WESBTYPE)).getSummary();
		groups.add(this.definition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IConfigurationStep#getGroups()
	 */
	@Override
	public List<Group> getGroups() {
		return groups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#getLabel()
	 */
	@Override
	public String getLabel() {
		return WESBConversionMessages.stepReview;
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
		return WESBConversionMessages.stepReviewDesc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#isComplete
	 * ()
	 */
	@Override
	public boolean isComplete() {
		return completed;
	}

	public void setCompleted(boolean b) {
		completed = b;
	}

	@Override
	public Boolean isEnabled() {
		return model.getResult() != null;
	}

	@Override
	public IStatus pageIsGoingToChange(IBaseConfigurationStep nextStep) {
		// TODO Auto-generated method stub
		return null;
	}

}
