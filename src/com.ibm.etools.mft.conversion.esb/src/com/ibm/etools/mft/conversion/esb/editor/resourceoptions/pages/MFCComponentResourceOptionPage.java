/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.MFCComponentResource;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;

/**
 * @author Zhongming Chen
 * 
 */
public class MFCComponentResourceOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	protected WESBConversionDataType model;
	protected MFCComponentResource component;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public MFCComponentResourceOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.component = (MFCComponentResource) detail;
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		return WESBConversionMessages.previewMessageMFCComponent;
	}

}
