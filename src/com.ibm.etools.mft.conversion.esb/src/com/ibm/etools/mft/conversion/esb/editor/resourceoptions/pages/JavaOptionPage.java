/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBJavas;

/**
 * @author Zhongming Chen
 * 
 */
public class JavaOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	protected WESBConversionDataType model;
	protected WESBJavas javas;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public JavaOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.javas = (WESBJavas) detail;
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		return WESBConversionMessages.previewJavas;
	}

}
