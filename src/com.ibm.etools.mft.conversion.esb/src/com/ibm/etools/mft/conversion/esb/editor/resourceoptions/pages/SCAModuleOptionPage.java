/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;

/**
 * @author Zhongming Chen
 * 
 */
public class SCAModuleOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage {

	protected WESBConversionDataType model;
	protected SCAModule module;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public SCAModuleOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.module = (SCAModule) detail;
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		if (module.getErrorMessage() != null) {
			return NLS.bind(WESBConversionMessages.previewMessage_unsupportedScaModule, module.getErrorMessage());
		} else {
			return WESBConversionMessages.previewMessageSCAModule;
		}
	}

}
