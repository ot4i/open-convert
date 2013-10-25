/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.ImportResource;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;

/**
 * @author Zhongming Chen
 * 
 */
public class ImportResourceOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	private WESBConversionDataType model;
	private ImportResource theImport;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public ImportResourceOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.theImport = (ImportResource) detail;
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		return WESBConversionMessages.previewMessageImport;
	}

}
