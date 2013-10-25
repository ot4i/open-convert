/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.Comparator;

import org.eclipse.core.resources.IProject;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBProjectComparator implements Comparator<Object> {

	/**
	 * 
	 */
	public WESBProjectComparator() {
	}

	public int compare(Object object1, Object object2) {
		IProject p1 = (IProject) object1;
		IProject p2 = (IProject) object2;
		if (ConversionUtils.isESBModule(p1) && !ConversionUtils.isESBModule(p2) || ConversionUtils.isESBLib(p1)
				&& !ConversionUtils.isESBLib(p2)) {
			return ConversionUtils.isESBLib(p1) ? -1 : 1;
		}
		return p1.getName().compareTo(p2.getName());
	}
}
