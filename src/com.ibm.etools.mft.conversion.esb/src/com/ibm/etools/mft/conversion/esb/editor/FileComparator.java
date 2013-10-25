/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * @author Zhongming Chen
 * 
 */
public class FileComparator implements Comparator<IFile> {

	private static List<String> exts = new ArrayList<String>();

	static {
		exts.add("xsd"); //$NON-NLS-1$
		exts.add("wsdl"); //$NON-NLS-1$
		exts.add("map"); //$NON-NLS-1$
	}

	/**
		 * 
		 */
	public FileComparator() {
	}

	public int compare(IFile f1, IFile f2) {
		String ext1 = f1.getFileExtension();
		String ext2 = f2.getFileExtension();
		int i1 = exts.indexOf(ext1);
		int i2 = exts.indexOf(ext2);
		if (i1 == -1) {
			i1 = 100;
		}
		if (i2 == -1) {
			i2 = 100;
		}
		if (ext1.equals(ext2)) {
			return f1.getName().compareTo(f2.getName());
		} else if (i1 == 100 && i2 == 100) {
			return ext1.compareTo(ext2);
		} else {
			return i1 - i2;
		}
	}

}
