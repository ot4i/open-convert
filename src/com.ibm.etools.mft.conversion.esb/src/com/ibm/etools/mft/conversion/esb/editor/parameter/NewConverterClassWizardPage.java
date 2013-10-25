/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.ibm.etools.mft.jcn.wizards.NewJCNClassWizardPage;

/**
 * @author Zhongming Chen
 * 
 */
public class NewConverterClassWizardPage extends NewJCNClassWizardPage {

	private String baseClass;

	/**
	 * @param baseClass
	 * 
	 */
	public NewConverterClassWizardPage(String baseClass) {
		this.baseClass = baseClass;
	}

	@Override
	public void init(IStructuredSelection selection) {
		super.init(selection);
		setSuperClass(baseClass, false);
	}

	protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {

		createInheritedMethods(type, false, false, imports, new SubProgressMonitor(monitor, 1));

		String importList = ((NewConverterWizard) getWizard()).getImports(type, monitor);
		StringTokenizer st = new StringTokenizer(importList);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			imports.addImport(s);
		}

		((NewConverterWizard) getWizard()).createMethods(type, monitor);

		if (monitor != null) {
			monitor.done();
		}
	}

	@Override
	public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
		super.createType(monitor);
		ICompilationUnit cu = getCreatedType().getCompilationUnit();
		cu.becomeWorkingCopy(new NullProgressMonitor());
		cu.save(new NullProgressMonitor(), true);

		String importList = ((NewConverterWizard) getWizard()).getUnusedImports(getCreatedType(), monitor);
		StringTokenizer st = new StringTokenizer(importList);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			cu.createImport(s, null, new NullProgressMonitor());
		}
	}
}
