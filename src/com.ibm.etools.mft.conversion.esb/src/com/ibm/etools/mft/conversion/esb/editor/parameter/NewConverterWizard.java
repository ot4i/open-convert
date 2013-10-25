/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.jcn.wizards.NewJCNElementWizard;

/**
 * @author Zhongming Chen
 * 
 */
public class NewConverterWizard extends NewJCNElementWizard {

	private String baseClass;
	private NewConverterClassWizardPage classPage;
	private IType typeCreated;
	private NewConverterProjectWizardPage projectPage;

	/**
	 * @param baseClass
	 * 
	 */
	public NewConverterWizard(String baseClass) {
		this.baseClass = baseClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.jcn.wizards.NewJCNElementWizard#hasTargetJAXBPackage()
	 */
	@Override
	public boolean hasTargetJAXBPackage() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.jcn.wizards.NewJCNElementWizard#getTargetJAXBPackage()
	 */
	@Override
	public String getTargetJAXBPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.etools.mft.jcn.wizards.NewJCNElementWizard#finishOnTemplate()
	 */
	@Override
	public boolean finishOnTemplate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		projectPage = new NewConverterProjectWizardPage(WESBConversionMessages.NewConverterProjectWizardPage_title); //$NON-NLS-1$
		addPage(projectPage);

		classPage = new NewConverterClassWizardPage(baseClass);
		classPage.setTitle(WESBConversionMessages.ClassPageDialog_title);
		classPage.setDescription(WESBConversionMessages.ClassPageDialog_desc);
		addPage(classPage);
		classPage.init(getSelection());
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		classPage.createType(monitor);
		typeCreated = classPage.getCreatedType();
	}

	public IType getTypeCreated() {
		return typeCreated;
	}

	public void createMethods(IType type, IProgressMonitor monitor) throws JavaModelException {
	}

	public String getImports(IType type, IProgressMonitor monitor) {
		return ""; //$NON-NLS-1$
	}

	public NewConverterClassWizardPage getClassPage() {
		return classPage;
	}

	public String getUnusedImports(IType createdType, IProgressMonitor monitor) {
		return "";
	}
}
