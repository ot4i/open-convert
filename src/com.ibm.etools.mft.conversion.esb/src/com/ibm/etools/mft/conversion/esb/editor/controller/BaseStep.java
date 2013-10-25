/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.controller;

import java.util.ArrayList;
import java.util.List;

import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.wizard.editor.model.IConfigurationStep;
import com.ibm.mb.common.model.Group;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class BaseStep implements IConfigurationStep {

	protected WESBConversionDataType model;
	protected Group definition;
	protected List<Group> groups = new ArrayList<Group>();
	protected boolean isCompleted;
	protected Controller controller;

	/**
	 * @param model
	 * @param controller
	 * 
	 */
	public BaseStep(WESBConversionDataType model, Controller controller) {
		this.model = model;
		this.definition = getDefinition();
		this.controller = controller;
		groups.add(this.definition);
	}

	abstract protected Group getDefinition();

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
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#isComplete
	 * ()
	 */
	@Override
	public boolean isComplete() {
		return isCompleted;
	}

	public void setCompleted(boolean b) {
		isCompleted = b;
	}

	@Override
	public Boolean isEnabled() {
		return null;
	}
}
