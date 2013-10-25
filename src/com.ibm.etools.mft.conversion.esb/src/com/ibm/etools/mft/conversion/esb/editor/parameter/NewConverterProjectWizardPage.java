/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.NewProjectCreationOperation;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.navigator.TreeLabelProvider;
import com.ibm.etools.mft.node.resource.ManifestFileHelper;

/**
 * @author Zhongming Chen
 * 
 */
public class NewConverterProjectWizardPage extends WizardPage implements SelectionListener, ISelectionChangedListener {

	private ComboViewer viewer;
	private Button createButton;
	private static Object lastSelection = null;

	private static final String[][] REQUIRED_IMPORTS = {
			{ "com.ibm.etools.mft.conversion.esb", "9.0.0", "greaterOrEqual" }, { "com.ibm.etools.mft.config", "9.0.0", "greaterOrEqual" }, { "org.eclipse.core.resources", "3.6.0", "greaterOrEqual" }, { "com.ibm.ws.sca.scdl.wsdl", "8.5.0", "greaterOrEqual" }, { "org.eclipse.emf.ecore", "2.6.0", "greaterOrEqual" }, { "com.ibm.ws.sca.scdl", "8.5.0", "greaterOrEqual" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$

	/**
	 * 
	 */
	public NewConverterProjectWizardPage(String pageName) {
		super(pageName);
		setDescription(WESBConversionMessages.NewConverterProjectWizardPage_desc);
		setTitle(WESBConversionMessages.NewConverterProjectWizardPage_title);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(composite, SWT.None).setText(WESBConversionMessages.NewConverterProjectWizardPage_javaProject);

		viewer = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewer.addSelectionChangedListener(this);
		refreshProjects();

		createButton = new Button(composite, SWT.None);
		createButton.setText(WESBConversionMessages.NewConverterProjectWizardPage_create);
		createButton.addSelectionListener(this);

		setPageComplete(!viewer.getSelection().isEmpty());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == createButton) {
			INewWizard wizard = null;

			wizard = new BasicNewProjectResourceWizard() {
				protected IFieldData getPluginFieldData(IProject project) {
					PluginFieldData data = new PluginFieldData();
					data.setId(project.getName());
					data.setVersion("1.0.0.0"); //$NON-NLS-1$
					data.setName(project.getName());
					data.setProvider(""); //$NON-NLS-1$
					data.setEnableAPITooling(false);
					data.setDoGenerateClass(false);
					data.setHasBundleStructure(true);
					data.setLegacy(false);
					data.setRCPApplicationPlugin(false);
					data.setSimple(false);
					data.setOutputFolderName("bin"); //$NON-NLS-1$
					data.setSourceFolderName("src"); //$NON-NLS-1$

					return data;
				}

				@Override
				public boolean performFinish() {
					final IProject project = ((WizardNewProjectCreationPage) getPage("basicNewProjectPage")).getProjectHandle(); //$NON-NLS-1$
					IProjectProvider projectProvider = new IProjectProvider() {
						public IPath getLocationPath() {
							// use default location
							return Platform.getLocation();
						}

						@Override
						public IProject getProject() {
							return project;
						}

						@Override
						public String getProjectName() {
							return project.getName();
						}
					};
					NewProjectCreationOperation op = new NewProjectCreationOperation(getPluginFieldData(project), projectProvider,
							null);
					try {
						getContainer().run(false, false, op);
					} catch (Exception e) {
						e.printStackTrace();
					}

					addBundleDependency(project);

					updateProjectCombo(((WizardNewProjectCreationPage) getStartingPage()).getProjectHandle());
					return true;
				}
			};

			if (null != wizard) {

				wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection) null);

				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.setHelpAvailable(false);
				dialog.create();
				dialog.setBlockOnOpen(true);
				dialog.open();

				if (dialog.getReturnCode() == Window.CANCEL)
					return;

			}// if
		}
	}

	protected void addBundleDependency(IProject project) {
		IFile manifestFile = project.getFile("META-INF/MANIFEST.MF"); //$NON-NLS-1$
		if (manifestFile.exists()) {
			Manifest manifest = ManifestFileHelper.parseManifestFile(manifestFile);
			if (null != manifest) {
				boolean isRequiredBundleValueChanged = false;
				Attributes mainAttributes = manifest.getMainAttributes();
				String requireBundle = mainAttributes.getValue("Require-Bundle"); //$NON-NLS-1$
				Map<String, String> requiredBundlesMap = ManifestFileHelper.parseRequiredBundles(requireBundle);

				// walk through the list of REQUIRED imports, and add missing
				// once to the "Require-Bundle" attribute
				for (int k = 0; k < REQUIRED_IMPORTS.length; k++) {
					if (!requiredBundlesMap.containsKey(REQUIRED_IMPORTS[k][0])) {
						if (null == requireBundle)
							requireBundle = REQUIRED_IMPORTS[k][0]; //$NON-NLS-1$							
						else
							requireBundle = requireBundle + "," + REQUIRED_IMPORTS[k][0]; //$NON-NLS-1$

						isRequiredBundleValueChanged = true;
					}
				}
				if (isRequiredBundleValueChanged) {
					if (isRequiredBundleValueChanged)
						mainAttributes.putValue("Require-Bundle", requireBundle); //$NON-NLS-1$

					ManifestFileHelper.writeManifest(manifest, manifestFile, true, new NullProgressMonitor());
				}
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	protected void updateProjectCombo(Object newResource) {
		refreshProjects();
		viewer.setSelection(new StructuredSelection(newResource));
	}

	protected void refreshProjects() {
		List<IProject> ps = new ArrayList<IProject>();

		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				if (!p.isAccessible()) {
					continue;
				}
				if (!p.hasNature("org.eclipse.pde.PluginNature")) { //$NON-NLS-1$
					continue;
				}
				ps.add(p);
			} catch (CoreException e) {
			}
		}

		Collections.sort(ps, new Comparator<IProject>() {
			@Override
			public int compare(IProject p1, IProject p2) {
				return p1.getName().compareTo(p2.getName());
			}
		});

		viewer.setInput(ps);
		if (ps.size() > 0) {
			if (lastSelection != null && ps.contains(lastSelection)) {
				viewer.setSelection(new StructuredSelection(lastSelection));
			} else {
				viewer.setSelection(new StructuredSelection(ps.get(0)));
			}
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setPageComplete(!viewer.getSelection().isEmpty());
		if (!viewer.getSelection().isEmpty()) {
			lastSelection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			((NewConverterWizard) getWizard()).getClassPage().init(new StructuredSelection(lastSelection));
		}
	}

}
