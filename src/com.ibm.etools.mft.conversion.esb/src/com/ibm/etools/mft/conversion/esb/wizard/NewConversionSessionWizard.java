package com.ibm.etools.mft.conversion.esb.wizard;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.ibm.bpm.common.projectImport.WorkspaceModifyOperation;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.model.ObjectFactory;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;

/**
 * @author Zhongming Chen
 *
 */
public class NewConversionSessionWizard extends BasicNewResourceWizard {

	private IStructuredSelection fSelection;
	NewConversionSessionFileWizardPage pageOne;
	private List<IProject> projectsToConvert = new ArrayList<IProject>();

	public NewConversionSessionWizard() {
		super();
	}

	@Override
	public boolean performFinish() {

		final List<Object> results = new ArrayList<Object>();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				IFile file = pageOne.getConversionSessionFile();
				WESBConversionDataType data = ConversionUtils.createNewConversion();
				for (IProject p : getProjectsToConvert()) {
					WESBProject wesbp = new WESBProject();
					wesbp.setTargetName(ConversionUtils.getDefaultTargetProjectName(p.getName()));
					wesbp.setName(p.getName());
					wesbp.setToConvert(true);
					data.getSourceProjects().add(wesbp);
				}
				String s = ConversionUtils.saveModel(new ObjectFactory().createWesbConversionData(data),
						WESBConversionConstants.PACKAGE_WESB_CONVERSION_TYPE);

				if (file.exists()) {
					if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.NewConversionSessionWizard_Confirm,
							NLS.bind(WESBConversionMessages.NewConversionSessionWizard_FileExists, file.getName()))) {
						results.add(Boolean.FALSE);
						return;
					}
				}

				try {
					if (file.exists()) {
						file.setContents(new ByteArrayInputStream(s.getBytes("UTF-8")), true, true, monitor);
						IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findEditor(new FileEditorInput(file));
						if (editor != null) {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
						}
					} else {
						file.create(new ByteArrayInputStream(s.getBytes("UTF-8")), true, monitor);
					}
				} catch (UnsupportedEncodingException e) {
				}

				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
			}
		};

		try {
			getContainer().run(false, true, op);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (results.size() > 0) {
			return false;
		}
		return true;
	}

	protected List<IProject> getProjectsToConvert() {
		return projectsToConvert;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.fSelection = selection;

		super.init(workbench, selection);

		// Set the wizard title. This is the (blue) title bar
		setWindowTitle(WESBConversionMessages.NewConversionSessionWizard_windowTitle);

		projectsToConvert.clear();
		for (Object o : fSelection.toList()) {
			if (o instanceof IResource) {
				projectsToConvert.add(((IResource) o).getProject());
			}
		}
	}

	public void addPages() {

		pageOne = new NewConversionSessionFileWizardPage("", fSelection); //$NON-NLS-1$
		addPage(pageOne);

	}

	/**
	 * 
	 * Use this method to activate the progressive disclosure of the
	 * participating fields of each wizard page.
	 * 
	 * @author dremond@ca.ibm.com
	 */
	public void createPageControls(Composite pageContainer) {

		super.createPageControls(pageContainer);

	}

}
