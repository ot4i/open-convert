package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBProjectContentProvider extends WorkbenchContentProvider implements IStructuredContentProvider {

	public WESBProjectContentProvider() {
	}

	public Object[] getChildren(Object o) {
		if (!(o instanceof IWorkspace)) {
			return new Object[0];
		}

		List<IProject> allProjects = new ArrayList<IProject>();

		for (IProject p : ((IWorkspace) o).getRoot().getProjects()) {
			if (ConversionUtils.isESBProject(p)) {
				allProjects.add(p);
			}
		}

		Collections.sort(allProjects, new WESBProjectComparator());

		return allProjects.toArray();
	}
}
