package com.ibm.etools.mft.conversion.esb.editor.controller;

import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionPlugin;
import com.ibm.etools.mft.conversion.esb.model.definition.ConversionType;

/**
 * @author Zhongming Chen
 * 
 */
public class DefinitionModelManager {

	private static HashMap<String, ConversionType> definitions = new HashMap<String, ConversionType>();

	static {
		// TODO: Add extension support

		try {
			definitions.put(
					Controller.WESBTYPE,
					loadDefinition(WESBConversionPlugin.getDefault().getBundle(),
							"/model/WESBConversionDefinition.xml", "com.ibm.etools.mft.conversion.esb.model.definition")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ConversionType loadDefinition(Bundle bundle, String definitionFile, String packageName) throws Exception {
		InputStream in = FileLocator.openStream(bundle, new Path(definitionFile), false);
		String content = ConversionUtils.readStream(in, "UTF-8"); //$NON-NLS-1$
		return (ConversionType) ConversionUtils.loadModel(content, packageName);
	}

	public static ConversionType getConversionType(String type) {
		return definitions.get(type);
	}
}
