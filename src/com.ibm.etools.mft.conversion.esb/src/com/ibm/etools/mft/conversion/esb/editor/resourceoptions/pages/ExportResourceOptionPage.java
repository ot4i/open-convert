/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.ExportResource;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;

/**
 * @author Zhongming Chen
 * 
 */
public class ExportResourceOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	private WESBConversionDataType model;
	private ExportResource export;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public ExportResourceOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.export = (ExportResource) detail;
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		return WESBConversionMessages.previewMessageExport;
	}

}
