package com.ibm.etools.mft.conversion.esb.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeImportDataModel;
import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeImportWizardPage;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;

public class WESBProjectInterchangeImportWizardPage extends ProjectInterchangeImportWizardPage {

	private Button launchConverionEditor = null;
	private boolean allowLaunchConversionEditor;

	public WESBProjectInterchangeImportWizardPage(ProjectInterchangeImportDataModel model, String pageName,
			boolean allowLaunchConversionEditor) {
		super(model, pageName);
		this.allowLaunchConversionEditor = allowLaunchConversionEditor;
		setImageDescriptor(WESBConversionImages.getImageDescriptor(WESBConversionImages.IMAGE_WESB_IMPORTER_WIZ));
	}

	@Override
	protected void createButtonsGroup(Composite parent) {
		super.createButtonsGroup(parent);
		if (allowLaunchConversionEditor) {
			launchConverionEditor = new Button(parent, SWT.CHECK);
			launchConverionEditor.setText(WESBConversionMessages.launchConversionEditor);
			launchConverionEditor.setSelection(true);
		}
	}

	public boolean launchConverionEditor() {
		if (allowLaunchConversionEditor) {
			return launchConverionEditor.getSelection();
		} else {
			return false;
		}
	}
}
