/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="2427084215" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2010, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeImportDataModel;
import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeImportWizard;
import com.ibm.etools.common.frameworks.internal.datamodel.WTPOperation;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WESBProjectInterchangeImportUtil;

public class WESBProjectInterchangeImportWizard extends ProjectInterchangeImportWizard {
	public WESBProjectInterchangeImportWizard(boolean allowLaunchConversionEditor) {
		super();
		this.allowLaunchConversionEditor = allowLaunchConversionEditor;
	}

	public WESBProjectInterchangeImportWizard() {
		super();
		this.allowLaunchConversionEditor = true;
	}

	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private WESBProjectInterchangeImportWizardPage launchEditorPage = null;

	private boolean allowLaunchConversionEditor;

	private HashSet<IProject> projectsToConvert = new HashSet<IProject>();

	@Override
	protected void init() {
		super.init();
		setWindowTitle(WESBConversionMessages.importWESBPI);
	}

	@Override
	protected void postPerformFinish() throws InvocationTargetException {
		super.postPerformFinish();
		if (!WESBProjectInterchangeImportUtil.widShellShared()) {
			Map selectedProjects = (Map) model.getProperty(ProjectInterchangeImportDataModel.SELECTED_PROJECT_MAP);
			Set<String> projectNames = selectedProjects.keySet();
			IProject[] projects = workspaceRoot.getProjects();
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (projectNames.contains(project.getName())) {
					projectsToConvert.add(project);
				}
			}
			if (launchEditorPage.launchConverionEditor()) {
				launchEditor(projectsToConvert);
			}
		}
	}

	private void launchEditor(HashSet<IProject> projectsToConvert) {
		NewConversionSessionWizard w = new NewConversionSessionWizard();
		w.init(PlatformUI.getWorkbench(), new StructuredSelection(projectsToConvert.toArray()));
		WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), w);
		wd.open();
	}

	@Override
	public void doAddPages() {
		launchEditorPage = new WESBProjectInterchangeImportWizardPage((ProjectInterchangeImportDataModel) model,
				"pageOne", allowLaunchConversionEditor); //$NON-NLS-1$
		addPage(launchEditorPage);
	}

	// private void addSelectedProjects(WESBProjectEditor projectEditor,
	// HashSet<IProject> projectsToConvert) {
	// Projects model = projectEditor.getModel();
	// ConversionUtils.addProjectsToModel(projectsToConvert, model);
	// projectEditor.setValue(ConversionUtils.saveModel(new
	// ObjectFactory().createWesbProjects(model),
	// ConversionUtils.PACKAGE_WESB_CONVERSION_TYPE));
	// projectEditor.getSite().valueChanged();
	// projectEditor.setFocus();
	// }
	//
	@Override
	protected WTPOperation createBaseOperation() {
		if (!WESBProjectInterchangeImportUtil.widShellShared()) {
			return new WESBProjectInterchangeImportOperation(model);
		} else {
			return super.createBaseOperation();
		}
	}

	public HashSet<IProject> getProjectsToConvert() {
		return projectsToConvert;
	}
}
