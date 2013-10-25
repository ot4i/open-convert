/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.ClassDefinition;

/**
 * @author Zhongming Chen
 * 
 */
public class ClassSelectionDialog extends Dialog implements SelectionListener {

	protected ClassDefinition model;

	protected Text typeText;
	protected Text clazzText;

	protected Text errorMessageText;

	protected String errorMessage = null;
	protected String type;
	protected String clazz;
	protected Button browseButton;
	protected Button resetButton;
	protected String resourceType;
	protected String resourcePath;

	private String label;

	private Button createButton;

	private Class baseClass;

	/**
	 * @param parentShell
	 * @param label
	 * @param artifacts
	 * @param selected
	 */
	public ClassSelectionDialog(Shell parentShell, ClassDefinition model, String type, String label, Class baseClass) {
		super(parentShell);
		this.model = model;
		this.label = label;
		this.type = type;
		this.baseClass = baseClass;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WESBConversionMessages.ClassSelectionDialog_title);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// type = typeText.getText();
			clazz = clazzText.getText();
		} else {
			// type = null;
			clazz = null;
		}
		super.buttonPressed(buttonId);
	}

	public String getType() {
		return type;
	}

	public String getClazz() {
		return clazz;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		// typeText.setFocus();
	}

	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(5, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);

		Link link = new Link(composite, SWT.WRAP);
		link.setText(label + WESBConversionMessages.ClassSelectionDialog_LoadSample);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 5;
		data.widthHint = composite.getBorderWidth();
		data.widthHint = 300;// otherwise dialog is TOO long
		link.setLayoutData(data);
		link.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem()
						.displayHelpResource("/com.ibm.etools.mft.samples.wesbcustom.doc/doc/overview.htm"); //$NON-NLS-1$
				ClassSelectionDialog.this.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		new Label(composite, SWT.WRAP).setText(WESBConversionMessages.converterClazz);

		clazzText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		clazzText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		if (model != null && model.getClazz().length() > 0) {
			clazzText.setText(model.getClazz());
		}

		browseButton = new Button(composite, SWT.None);
		browseButton.setText(WESBConversionMessages.select);
		browseButton.addSelectionListener(this);

		createButton = new Button(composite, SWT.None);
		createButton.setText(WESBConversionMessages.ClassSelectionDialog_create);
		createButton.addSelectionListener(this);

		resetButton = new Button(composite, SWT.None);
		resetButton.setText(WESBConversionMessages.resetToDefault);
		resetButton.addSelectionListener(this);

		errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 5;
		data.widthHint = 600;
		errorMessageText.setLayoutData(data);
		errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		setErrorMessage(errorMessage);

		applyDialogFont(composite);
		return composite;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		if (errorMessageText != null && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
			boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();
			Control button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(errorMessage == null);
			}
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == browseButton) {
			OpenTypeSelectionDialog d = new OpenTypeSelectionDialog(browseButton.getShell(), false, PlatformUI.getWorkbench()
					.getProgressService(), null, IJavaSearchConstants.TYPE);
			d.setTitle(WESBConversionMessages.openClassType);
			if (d.open() == OpenTypeSelectionDialog.OK) {
				IType type = (IType) d.getFirstResult();
				setType(type);
			}
		} else if (e.getSource() == resetButton) {
			clazzText.setText(""); //$NON-NLS-1$
		} else if (e.getSource() == createButton) {

			NewConverterWizard wizard = new NewConverterWizard(baseClass.getName()) {
				@Override
				public void createMethods(IType type, IProgressMonitor monitor) throws JavaModelException {
					if (baseClass == AbstractMediationPrimitiveConverter.class) {
						ConversionUtils.createMethodFromTemplate(type, "primitiveConverter.code.getConvertTo.template", monitor); //$NON-NLS-1$
						ConversionUtils.createMethodFromTemplate(type, "primitiveConverter.code.getType.template", monitor, //$NON-NLS-1$
								new Object[] { ClassSelectionDialog.this.type });
						ConversionUtils.createMethodFromTemplate(type, "primitiveConverter.code.convert.template", monitor); //$NON-NLS-1$
						ConversionUtils
								.createMethodFromTemplate(type, "primitiveConverter.code.getInputTerminal.template", monitor); //$NON-NLS-1$
						ConversionUtils.createMethodFromTemplate(type, "primitiveConverter.code.getOutputTerminal.template", //$NON-NLS-1$
								monitor);
					} else if (baseClass == AbstractBindingConverter.class) {
						ConversionUtils.createMethodFromTemplate(type, "bindingConverter.code.getConvertTo.template", monitor); //$NON-NLS-1$
						ConversionUtils.createMethodFromTemplate(type, "bindingConverter.code.getType.template", monitor, //$NON-NLS-1$
								ClassSelectionDialog.this.type);
						ConversionUtils.createMethodFromTemplate(type, "bindingConverter.code.convert.template", monitor); //$NON-NLS-1$
						ConversionUtils.createMethodFromTemplate(type, "bindingConverter.code.getInputTerminal.template", monitor); //$NON-NLS-1$
						ConversionUtils.createMethodFromTemplate(type, "bindingConverter.code.getOutputTerminal.template", monitor); //$NON-NLS-1$
					}
				}

				@Override
				public String getImports(IType type, IProgressMonitor monitor) {
					if (baseClass == AbstractMediationPrimitiveConverter.class) {
						return ConversionUtils.loadTemplate("primitiveConverter.imports.template"); //$NON-NLS-1$
					} else if (baseClass == AbstractBindingConverter.class) {
						return ConversionUtils.loadTemplate("bindingConverter.imports.template"); //$NON-NLS-1$
					} else {
						return super.getImports(type, monitor);
					}
				}

				@Override
				public String getUnusedImports(IType createdType, IProgressMonitor monitor) {
					if (baseClass == AbstractMediationPrimitiveConverter.class) {
						return ConversionUtils.loadTemplate("primitiveConverter.unusedimports.template"); //$NON-NLS-1$
					} else if (baseClass == AbstractBindingConverter.class) {
						return ConversionUtils.loadTemplate("bindingConverter.unusedimports.template"); //$NON-NLS-1$
					} else {
						return super.getUnusedImports(createdType, monitor);
					}
				}
			};
			WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			wizard.setWindowTitle(WESBConversionMessages.NewConverterProjectWizardPage_title);
			wd.setHelpAvailable(false);
			if (wd.open() == wd.OK) {
				IType typeCreated = wizard.getTypeCreated();
				setType(typeCreated);
				try {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							(IFile) typeCreated.getResource());
				} catch (PartInitException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	protected void setType(IType type) {
		if (type != null) {
			// if ( !isValidType(type) ) {
			// MessageDialog.openError(browseButton.getShell(),
			// WESBConversionMessages.errorTitle,
			// NLS.bind(WESBConversionMessages.errorWrongPrimitiveConverter,
			// type.getFullyQualifiedName(),
			// IMediationPrimitiveHandler.class.getName()) );
			// return;
			// }
			if (type.getClassFile() != null) {
				resourceType = PrimitiveManager.JAR_SPACE;
				resourcePath = type.getClassFile().getPath().toString();
			} else {
				resourceType = PrimitiveManager.WORKSPACE;
				resourcePath = type.getResource().getFullPath().toString();
			}
			clazzText.setText(type.getFullyQualifiedName());
		}
	}

	protected boolean isValidType(IType type) {
		try {
			if (!type.isClass()) {
				return false;
			} else {
				for (String s : type.getSuperInterfaceNames()) {
					if (IPrimitiveConverter.class.getName().equals(s)
							|| IPrimitiveConverter.class.getName()
									.substring(IPrimitiveConverter.class.getPackage().getName().length() + 1).equals(s)) {
						return true;
					}
				}
				String s = type.getSuperclassName();
				if (s != null) {
					IType t = (IType) type.getJavaProject().findElement(new Path(s));
					return isValidType(t);
				}
			}
		} catch (JavaModelException e) {
		}
		return true;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public String getResourceType() {
		return resourceType;
	}

	public ClassDefinition getModel() {
		return model;
	}
}
