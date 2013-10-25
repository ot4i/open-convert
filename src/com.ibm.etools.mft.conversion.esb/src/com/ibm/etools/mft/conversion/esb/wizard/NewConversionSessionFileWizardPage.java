package com.ibm.etools.mft.conversion.esb.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.navigator.AbstractVirtualFolder;
import com.ibm.etools.mft.navigator.resource.element.FlowNamespace;
import com.ibm.etools.mft.navigator.resource.element.MessageSetFolder;
import com.ibm.etools.mft.navigator.resource.element.lib.LibraryNamespace;
import com.ibm.etools.mft.navigator.resource.element.lib.NewShortcut;
import com.ibm.etools.mft.navigator.utils.NavigatorUtils;
import com.ibm.etools.mft.navigator.workingsets.WorkingSetActionGroup;
import com.ibm.etools.mft.util.WMQIConstants;
import com.ibm.etools.mft.util.ui.workingsets.WorkingSetFilterToggleControl;
import com.ibm.etools.mft.util.ui.workingsets.WorkingSetFilterToggleListener;
import com.ibm.etools.mft.util.ui.workingsets.WorkingSetFilterToggleSelectionListener;
import com.ibm.etools.mft.util.workingsets.WorkingSetUtil;

/**
 * @author Zhongming Chen
 *
 */
public class NewConversionSessionFileWizardPage extends WizardPage implements SelectionListener, ModifyListener {

	public static final String WESB_CONVERSION_PROJECT_NAME = "WESB_Conversions"; //$NON-NLS-1$
	private IStructuredSelection fSelection;
	private IProject fInitialProject = null;

	/**
	 * Text field for providing the project container
	 */
	private Combo fProjCombo;

	private Text fNameText;

	/**
	 * Browse button for retrieving the project
	 */
	private Button fProjButton;

	private Composite projectComp;
	private String conversionSessionName;

	protected NewConversionSessionFileWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		setPageComplete(false);
		fSelection = selection;
		setTitle(WESBConversionMessages.NewConversionSessionFileWizardPage_windowTitle);
		setDescription(WESBConversionMessages.NewConversionSessionFileWizardPage_windowTitleDesc);
		setImageDescriptor(WESBConversionImages.getImageDescriptor(WESBConversionImages.WIZARD_NEW_CONVERSION_SESSION));
	}

	@Override
	public void createControl(Composite parent) {

		IProject p = ConversionUtils.getProject(WESB_CONVERSION_PROJECT_NAME);
		if (!p.exists()) {
			try {
				p.create(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		if (!p.isAccessible()) {
			try {
				p.open(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		layout.numColumns = 1;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		projectComp = new Composite(comp, SWT.NONE);
		GridData pageData = new GridData(SWT.FILL, SWT.TOP, true, false);
		projectComp.setLayoutData(pageData);
		GridLayout projectCompLayout = new GridLayout();
		projectCompLayout.numColumns = 3;
		projectCompLayout.marginHeight = 0;
		projectCompLayout.marginWidth = 0;
		projectComp.setLayout(projectCompLayout);

		// create the project label
		Label srcLabel = new Label(projectComp, SWT.NONE);
		srcLabel.setText(WESBConversionMessages.NewConversionSessionFileWizardPage_project);

		// create the project list combo box
		fProjCombo = new Combo(projectComp, SWT.BORDER | SWT.READ_ONLY);

		GridData fProjComboGD = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		fProjComboGD.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		fProjCombo.setLayoutData(fProjComboGD);

		Label desc = new Label(comp, SWT.WRAP);
		GridData d = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		d.widthHint = parent.getBorderWidth();
		d.heightHint = 60;
		desc.setLayoutData(d);
		desc.setText(WESBConversionMessages.NewConversionSessionFileWizardPage_desc);

		// initialize dialog units for use to determine button size
		initializeDialogUnits(projectComp);

		// create the new project button
		fProjButton = new Button(projectComp, SWT.PUSH);
		fProjButton.setText(WESBConversionMessages.NewConversionSessionFileWizardPage_newButton);

		GridData projButtonGD = new GridData();
		// projButtonGD.heightHint =
		// convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		projButtonGD.widthHint = getButtonWidthHint(fProjButton);
		fProjButton.setLayoutData(projButtonGD);

		Label nameLabel = new Label(projectComp, SWT.NONE);
		nameLabel.setText(WESBConversionMessages.NewConversionSessionFileWizardPage_conversionSessionName);

		// create the decision name text box
		fNameText = new Text(projectComp, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		fNameText.setLayoutData(gd);

		fInitialProject = p;

		// fill combo box with projects in current workspace
		refreshProjects(false);

		fProjCombo.setText(WESB_CONVERSION_PROJECT_NAME);

		final WorkingSetFilterToggleControl wsFilter = new WorkingSetFilterToggleControl();
		// Add listeners to refresh the project combo if working set filter
		// preference changes
		wsFilter.addToggleListener(new WorkingSetFilterToggleListener() {
			public void workingSetFilterEnabled() {
				String prevSelection = fProjCombo.getText();
				refreshProjects(false);
				// Restore selection
				fProjCombo.setText(prevSelection);
			}

			public void workingSetFilterDisabled() {
				String prevSelection = fProjCombo.getText();
				refreshProjects(true);
				// Restore selection
				fProjCombo.setText(prevSelection);
			}
		});
		// Add a listener to update info text if user picks a destination folder
		// outside of working set
		WorkingSetFilterToggleSelectionListener projTextListener = new WorkingSetFilterToggleSelectionListener(wsFilter) {
			// We need to override the existing class because we need to convert
			// the selection to a project
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// If no working set is enabled or filter is being performed, we
				// don't need to do anything
				if (!WorkingSetUtil.getHelper().hasWorkingSetEnabled() || wsFilter.isFilteringEnabled())
					return;
				Combo o = (Combo) arg0.getSource();
				checkIResource(ResourcesPlugin.getWorkspace().getRoot().getProject(o.getText()));
			}
		};
		fProjCombo.addSelectionListener(projTextListener);

		if (fInitialProject != null) {

			fNameText.setFocus();
		}

		fNameText.addModifyListener(this);

		fProjButton.addSelectionListener(this);
		fProjCombo.addSelectionListener(this);
		fProjCombo.addModifyListener(this);

		setControl(comp);

		setErrorMessage(null);// don't want red X appearing on wizard startup

		setPageComplete(false);
	}

	public String getConversionSessionName() {
		return conversionSessionName;
	}

	public IFile getConversionSessionFile() {
		IFile file = null;

		IProject prj = getProject();

		return prj.getFile(conversionSessionName + WESBConversionConstants.CONVERSION_SESSION_EXT);
	}

	/**
	 * Performs validation on all text fields on the wizard page. If errors are
	 * found, the page is disabled and any other actions (ie. disabling buttons)
	 * that are necessary are taken. Also toggles schema "(default)" label.
	 * 
	 */
	public void modifyText(ModifyEvent e) {

		// set the modified flags to be true for whatever text widgit is
		// the source of the modify event
		if (e.getSource().equals(fProjCombo)) {
		} else if (e.getSource() == fNameText) {
			conversionSessionName = fNameText.getText();
		}

		setErrorMessage(null);
		setPageComplete(isInputValid());
	}

	private boolean isInputValid() {
		String prj = fProjCombo.getText().trim();

		if (prj.length() == 0) {
			setErrorMessage(WESBConversionMessages.NewConversionSessionFileWizardPage_errorEmptyProjectName);
			return false;
		}

		if (!getProject().isAccessible()) {
			setErrorMessage(WESBConversionMessages.NewConversionSessionFileWizardPage_errorProjectNoAccessible);
			return false;
		}

		conversionSessionName = fNameText.getText().trim();
		if (!validateName(conversionSessionName))
			return false;

		return true;
	}

	protected IProject getProject() {
		String projString = fProjCombo.getText();
		if (!validateProject(projString))
			return null;

		try {
			IPath containerPath = new Path(projString);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.getProject(containerPath.segments()[0]);

		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * Validates the String entered in the page's "project" field.
	 * 
	 * @param prj
	 *            the project name to be validated.
	 */
	protected boolean validateProject(String prj) {

		// remove leading and trailing "/" chars
		if (prj.startsWith("/")) { //$NON-NLS-1$
			prj = prj.substring(1, prj.length());
		}

		if (prj.endsWith("/")) { //$NON-NLS-1$
			prj = prj.substring(0, prj.length() - 1);
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus nameStatus = workspace.validateName(prj, IResource.PROJECT);

		if (!nameStatus.isOK()) {
			String msg = nameStatus.getMessage();
			if (msg != null && !msg.isEmpty()) {
				setErrorMessage(msg);
				return false;
			}
		}

		return true;

	}

	/**
	 * Validates the String entered in the page's "name" field. Must conform to
	 * both platform rules and the rules of Java identifiers.
	 * 
	 * @param name
	 *            the name to be validated.
	 */
	protected boolean validateName(String name) {
		if (name.isEmpty() || name.trim().equalsIgnoreCase(WMQIConstants.DECISION_SERVICE_FILE_EXTENSION)) {
			setErrorMessage(WESBConversionMessages.NewConversionSessionFileWizardPage_errorSessionNameEmpty);
			return false;
		}

		String msg = validateResourceName(name);
		if (msg != null) {
			setErrorMessage(msg);
			return false;
		}
		return true;
	}

	/**
	 * Validates 'name' conform to both platform rules and the rules of Java
	 * identifiers. Ensure that resource "name.extension" doesn't already exist
	 * within the IContainer.
	 * 
	 * @param name
	 *            resource name to be validated.
	 * @param extension
	 *            resource extention
	 * @param container
	 *            container where resource must be unique
	 * @return a String containing either an error message to be displayed or
	 *         <code>null</code> if there is no error.
	 */
	private String validateResourceName(String name) {
		// validate the name to make sure it conforms with
		// the Java identifier rules.
		for (int i = 0; i < name.length(); i++) {

			if (i == 0) {
				if (!Character.isJavaIdentifierStart(name.charAt(0))) {
					return WESBConversionMessages.NewConversionSessionFileWizardPage_errorInvalidFileName;
				}

			} else {
				if (!Character.isJavaIdentifierPart(name.charAt(i))) {
					return WESBConversionMessages.NewConversionSessionFileWizardPage_errorInvalidFileName;
				}
			}

		}

		// validate the name to make sure it conforms with
		// Eclipse workspace rules.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateName(name, IResource.FILE);
		if (!status.isOK()) {
			return status.getMessage();
		}

		return null;
	}

	/**
	 * @param supressWSFilter
	 *            true if projects should show all projects regardless of
	 *            whether user has an active working set
	 */
	protected void refreshProjects(boolean supressWSFilter) {
		if (fProjCombo.getItemCount() > 0)
			fProjCombo.removeAll();

		fill_projects_all_appropriate_projects_from_workspace(supressWSFilter);

	}

	private int getButtonWidthHint(Button button) {

		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	protected IProject getSelectedProject(IStructuredSelection selection) {

		if ((selection == null) || (selection.size() <= 0))
			return null;

		Object selectedResource = fSelection.getFirstElement();
		IProject selectedProject = null;

		if (selectedResource instanceof LibraryNamespace) {
			Object parent = ((LibraryNamespace) selectedResource).getParent();
			if (parent instanceof IProject) {
				selectedProject = (IProject) parent;
			} else if (parent instanceof AbstractVirtualFolder) {
				selectedProject = (IProject) ((AbstractVirtualFolder) parent).getParent();
			}
		} else if (selectedResource instanceof FlowNamespace) {
			Object parent = ((FlowNamespace) selectedResource).getParent();
			if (parent instanceof IProject) {
				selectedProject = (IProject) parent;
			} else if (parent instanceof AbstractVirtualFolder) {
				selectedProject = (IProject) ((AbstractVirtualFolder) parent).getParent();
			}
		} else if (selectedResource instanceof AbstractVirtualFolder) {
			selectedProject = getProjectOfAbstractVirtualFolder((AbstractVirtualFolder) selectedResource);
		} else if (selectedResource instanceof NewShortcut) {
			Object parent = ((NewShortcut) selectedResource).getParent();
			if (parent instanceof IProject) {
				selectedProject = (IProject) parent;
			}
		} else if (selectedResource instanceof IAdaptable) {
			IResource res = NavigatorUtils.getAdaptedResource(selectedResource);

			if (res != null)
				selectedProject = (res).getProject();
		}

		return selectedProject;
	}

	/**
	 * @param vf
	 *            a Virtual folder
	 * @return the IProject containing the virtual folder.
	 */
	protected IProject getProjectOfAbstractVirtualFolder(AbstractVirtualFolder vf) {
		Object parent = vf.getParent();
		if (parent instanceof AbstractVirtualFolder) {
			return getProjectOfAbstractVirtualFolder((AbstractVirtualFolder) parent);
		} else if (parent instanceof MessageSetFolder) {
			return (IProject) ((MessageSetFolder) parent).getParent();
		} else if (parent instanceof IProject) {
			return (IProject) parent;
		}
		return null;
	}

	private void fill_projects_all_appropriate_projects_from_workspace(boolean suppressWSFilter) {
		if (fProjCombo.getItemCount() > 0)
			fProjCombo.removeAll();

		// get all appropriate projects in workspace : mb, lib, app
		List<IProject> certainProjects = new ArrayList<IProject>(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()));

		if (null != certainProjects && certainProjects.size() > 0) {

			Collections.sort(certainProjects, new Comparator<IProject>() {
				public int compare(IProject p1, IProject p2) {
					return p1.getName().compareTo(p2.getName());
				}
			});

			Iterator<IProject> it = certainProjects.iterator();
			while (it.hasNext()) {
				String name = it.next().getName();
				fProjCombo.add(name);
			}// while
		}// if

	}

	public boolean canFlipToNextPage() {
		// return isPageComplete() && getNextPage() != null;
		return isPageComplete();// don't call getNextPage() here because this
								// will cause the load of the next page for
								// every character typed
	}

	protected IWorkingSet getActiveWorkingSet() {
		if (WorkingSetActionGroup.getInstance() == null)
			return null;
		return WorkingSetActionGroup.getInstance().getWorkingSet(WorkingSetActionGroup.getInstance().getActiveNavigator());
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent event) {

		Object source = event.getSource();

		if (source.equals(fProjButton)) {

			INewWizard wizard = null;

			wizard = new BasicNewProjectResourceWizard() {
				@Override
				public boolean performFinish() {
					// TODO Auto-generated method stub
					boolean result = super.performFinish();
					if (result) {
						updateProjectCombo(((WizardNewProjectCreationPage) getStartingPage()).getProjectHandle());
					}
					return result;
				}
			};

			if (null != wizard) {

				wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) null);

				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.create();
				dialog.setBlockOnOpen(true);
				dialog.open();

				if (dialog.getReturnCode() == Window.CANCEL)
					return;

			}// if

		}// if

	}

	protected void updateProjectCombo(Object newResource) {
		refreshProjects(false);
		fProjCombo.setText(((IProject) newResource).getName());
	}

}
