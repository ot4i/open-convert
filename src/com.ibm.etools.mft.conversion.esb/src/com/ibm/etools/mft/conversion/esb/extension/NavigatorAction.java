/*************************************************************************
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-E11
 * 5724-E26
 * (C) Copyright IBM Corporation 2010, 2013
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been deposited
 * with the U.S. Copyright office
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extension;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.wizard.NewConversionSessionWizard;

/**
 * @author Zhongming Chen
 * 
 */
public class NavigatorAction implements IWorkbenchWindowActionDelegate {
	private HashSet<IProject> projectsToConvert = new HashSet<IProject>();

	@Override
	public void run(IAction action) {
		if (projectsToConvert.size() > 0) {
			NewConversionSessionWizard w = new NewConversionSessionWizard();
			w.init(PlatformUI.getWorkbench(), new StructuredSelection(projectsToConvert.toArray()));
			WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), w);
			wd.setHelpAvailable(false);
			wd.open();
			try {
				IProject wesbp = ConversionUtils.getProject("WESB_Conversions"); //$NON-NLS-1$
				if (wesbp.exists() && wesbp.members().length <= 1) {
					ConversionUtils.deleteProject(wesbp);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		projectsToConvert.clear();
		action.setEnabled(hasProjectsToConvert(selection));
	}

	private boolean hasProjectsToConvert(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structureSelection = (IStructuredSelection) selection;
			for (Object o : structureSelection.toList()) {
				IProject project = (IProject) o;
				if (!ConversionUtils.isESBProject(project)) {
					projectsToConvert.clear();
					return false;
				}
				projectsToConvert.add(project);
			}
		}
		return projectsToConvert.size() > 0;
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// Do nothing
	}
}
