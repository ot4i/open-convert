/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;

/**
 * @author Zhongming Chen
 * 
 */
public class SchemaOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public SchemaOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		return WESBConversionMessages.previewSchemas;
	}

}
