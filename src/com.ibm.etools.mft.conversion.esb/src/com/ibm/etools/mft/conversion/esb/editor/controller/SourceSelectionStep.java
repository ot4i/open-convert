/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WESBConversionPlugin;
import com.ibm.etools.mft.conversion.esb.extension.resource.WESBConversionManager;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.definition.WesbConversionType;
import com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep;
import com.ibm.mb.common.model.Group;
import com.ibm.ws.ffdc.FFDCFilter;

/**
 * @author Zhongming Chen
 * 
 */
public class SourceSelectionStep extends BaseStep {

	private boolean changed = true;

	/**
	 * @param model
	 * @param controller
	 * 
	 */
	public SourceSelectionStep(WESBConversionDataType model, Controller controller) {
		super(model, controller);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep#getLabel()
	 */
	@Override
	public String getLabel() {
		return WESBConversionMessages.stepSource;
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
		return WESBConversionMessages.stepSourceDesc;
	}

	@Override
	protected Group getDefinition() {
		return ((WesbConversionType) DefinitionModelManager.getConversionType(Controller.WESBTYPE)).getSourceSelection();
	}

	@Override
	public boolean isComplete() {
		for (WESBProject p : model.getSourceProjects()) {
			if (p.isToConvert()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IStatus pageIsGoingToChange(IBaseConfigurationStep nextStep) {
		if (changed) {
			FFDCFilter.missingMedNodes.clear();

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			try {
				pmd.run(false, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						new WESBConversionManager(model, controller.getLog()).preview(new NullProgressMonitor());
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				changed = false;
			}

			if (FFDCFilter.missingMedNodes.size() > 0) {
				List<String> ss = new ArrayList<String>(FFDCFilter.missingMedNodes);
				Collections.sort(ss);
				StringBuffer sb = new StringBuffer();
				for (String s : ss) {
					sb.append("\t"); //$NON-NLS-1$
					sb.append(s);
					sb.append("\n"); //$NON-NLS-1$
				}
				String msg = NLS.bind(WESBConversionMessages.SourceSelectionStep_MissingMedNodeMessage, sb.toString());
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						WESBConversionMessages.SourceSelectionStep_MissingMedNodeShortMessage, msg);
				changed = true;

				controller.getResourceConfigurationStep().setCompleted(false);
				controller.getEditor().getNavigationbar().selectPreviousItem();
				controller.refreshNavigationBar();

				return new Status(Status.CANCEL, WESBConversionPlugin.getDefault().getBundle().getSymbolicName(),
						WESBConversionMessages.SourceSelectionStep_MissingMedNodeShortMessage);
			}

		}
		return Status.OK_STATUS;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}
