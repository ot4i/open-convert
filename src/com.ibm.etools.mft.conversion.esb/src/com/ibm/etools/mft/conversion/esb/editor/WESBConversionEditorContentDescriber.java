package com.ibm.etools.mft.conversion.esb.editor;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBConversionEditorContentDescriber implements IContentDescriber {

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		// TODO Auto-generated method stub
		return VALID;
	}

	@Override
	public QualifiedName[] getSupportedOptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
