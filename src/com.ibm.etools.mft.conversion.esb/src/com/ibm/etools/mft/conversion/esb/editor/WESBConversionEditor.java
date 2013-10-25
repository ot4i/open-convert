package com.ibm.etools.mft.conversion.esb.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.wizard.editor.internal.ui.IWizardEditorNavigationBar;
import com.ibm.etools.mft.wizard.editor.model.IController;
import com.ibm.etools.mft.wizard.editor.ui.WizardEditor;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBConversionEditor extends WizardEditor {
	@Override
	protected IController loadController(IEditorInput input) throws CoreException {
		return new Controller(this, ((FileEditorInput) input).getFile());
	}

	public IWizardEditorNavigationBar getNavigationbar() {
		return fNavigationBar;
	}

	public void refreshNavigatoinBar() {
		getNavigationbar().refresh();
	}
}