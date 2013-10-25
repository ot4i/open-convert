package com.ibm.etools.mft.conversion.esb;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;

/**
 * @author Zhongming Chen
 */
public class ConversionHelper {

	protected HashMap<String, String> projectNameMapping;

	public ConversionHelper(WESBConversionDataType model) {
		projectNameMapping = new HashMap<String, String>();
		for (WESBProject p : model.getSourceProjects()) {
			if (p.isToConvert()) {
				projectNameMapping.put(p.getName(), getConvertedProjectName(p));
			}
		}
	}

	public String getConvertedProjectName(IProject p) {
		if (ConversionUtils.isESBProject(p)) {
			String name = projectNameMapping.get(p.getName());
			if (name == null || name.length() == 0) {
				name = ConversionUtils.getDefaultTargetProjectName(p.getName());
			}
			return name;
		}
		return p.getName();
	}

	public IFile getTargetFile(IFile file) {
		return ConversionUtils.getProject(getConvertedProjectName(file.getProject())).getFile(file.getProjectRelativePath());
	}

	public String getConvertedProjectName(WESBProject p) {
		String convertedName = p.getTargetName();
		if (convertedName == null || convertedName.equals("")) { //$NON-NLS-1$
			convertedName = ConversionUtils.getDefaultTargetProjectName(p.getName());
		}
		return convertedName;
	}

}
