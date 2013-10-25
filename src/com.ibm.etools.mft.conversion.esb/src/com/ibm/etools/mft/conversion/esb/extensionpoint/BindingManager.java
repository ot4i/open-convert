/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="2745111890" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2010, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.binding.WSExportConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter.ConverterContext;
import com.ibm.etools.mft.conversion.esb.model.BindingConverter;
import com.ibm.etools.mft.conversion.esb.model.ClassDefinition;
import com.ibm.etools.mft.conversion.esb.model.GlobalConfigurationType;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.Part;
import com.ibm.wsspi.sca.scdl.impl.NativeExportBindingImpl;

/**
 * @author Zhongming Chen
 *
 */
public class BindingManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static HashMap<String, IBindingConverter> bindingConverters = new HashMap<String, IBindingConverter>();

	private static HashMap<String, String> displayNames = new HashMap<String, String>();

	static {
		loadBindingConverters();

		displayNames.put(NativeExportBindingImpl.class.getName(), "SCA Export Binding"); //$NON-NLS-1$
	}

	private static void loadBindingConverters() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		try {
			IConfigurationElement[] handlerConfigurations = extensionRegistry
					.getConfigurationElementsFor(WESBConversionConstants.BINDING_CONVERTER_ID);
			for (IConfigurationElement current : handlerConfigurations) {
				IBindingConverter handler = (IBindingConverter) current
						.createExecutableExtension(WESBConversionConstants.CLASS_ATTRIBUTE_NAME);

				bindingConverters.put(handler.getType(), handler);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static Object[] getBindingMappings(GlobalConfigurationType model) {
		HashMap<String, BindingConverter> result = new HashMap<String, BindingConverter>();
		for (String k : bindingConverters.keySet()) {
			BindingConverter v = new BindingConverter();
			v.setType(k);
			ClassDefinition clazz = new ClassDefinition();
			clazz.setClazz(bindingConverters.get(k).getClass().getName());
			v.setClazz(clazz);
			result.put(k, v);
		}
		for (BindingConverter m : model.getBindingConverters()) {
			result.put(m.getType(), m);
		}
		return result.values().toArray();
	}

	public static boolean isUserDefined(String type) {
		return !bindingConverters.containsKey(type);
	}

	public static IBindingConverter getBindingConverter(String type) {
		return bindingConverters.get(type);
	}

	public static void setContext(ConversionContext context) {
		DefaultBindingConverter.instance.setConversionContext(context);
		for (IBindingConverter converter : bindingConverters.values()) {
			converter.setConversionContext(context);
		}
	}

	public static IBindingConverter getConverter(String type, ConversionContext context, WESBConversionDataType model)
			throws Exception {
		BindingConverter bc = getBindingConverter(type, model.getGlobalConfiguration());
		if (bc == null || bc.getClazz() == null) {
			IBindingConverter result = getBindingConverter(type);
			if (result != null) {
				return result;
			}
		} else {
			ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
			ConversionClassLoader classLoader = new ConversionClassLoader(new URL[0]);
			classLoader.setClassDefinition(bc.getClazz(), oldLoader);
			try {
				Thread.currentThread().setContextClassLoader(classLoader);
				Class clazz = classLoader.loadClass(bc.getClazz().getClazz());
				if (clazz != null) {
					IBindingConverter result = (IBindingConverter) clazz.newInstance();
					result.setConversionContext(context);
					return result;
				}
			} finally {
				Thread.currentThread().setContextClassLoader(oldLoader);
			}
		}
		return DefaultBindingConverter.instance;
	}

	public static BindingConverter getBindingConverter(String type, GlobalConfigurationType model) {
		for (BindingConverter pm : model.getBindingConverters()) {
			if (pm.getType().equals(type)) {
				return pm;
			}
		}
		return null;
	}

	public static String getNodeName(Binding binding, HashMap<Binding, Nodes> bindingToNodes) {
		String proposedName = "SCABinding_";
		if (binding != null) {
			proposedName = ((Part) binding.eContainer()).getName();
		}
		proposedName = ConversionUtils.encodeName(proposedName);
		String baseName = proposedName;
		int i = 1;
		while (isNameInUse(proposedName, bindingToNodes.values())) {
			proposedName = baseName + (i++);
			proposedName = ConversionUtils.encodeName(proposedName);
		}
		return proposedName;
	}

	protected static boolean isNameInUse(String proposedName, Collection<Nodes> nodes) {
		for (Nodes nc : nodes) {
			for (com.ibm.broker.config.appdev.Node n : nc.getAllNodes()) {
				if (n.getNodeName().equals(proposedName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Nodes convertBinding(ConverterContext converterContext) throws Exception {

		IBindingConverter bindingHandler = null;
		if (converterContext.sourceBinding != null) {
			bindingHandler = BindingManager.getConverter(converterContext.sourceBinding.getClass().getName(),
					converterContext.moduleConverter.getContext(), converterContext.moduleConverter.getContext().model);
		}
		if (bindingHandler == null) {
			bindingHandler = DefaultBindingConverter.instance;
		}

		return bindingHandler.convert(converterContext);
	}

	public static String getConverterDisplayName(String s) {
		if (DefaultBindingConverter.class.getName().equals(s)) {
			s = WESBConversionMessages.defaultConverter;
		} else if (s.startsWith(WSExportConverter.class.getPackage().getName())) {
			s = WESBConversionMessages.builtInConverter;
		}
		return s;
	}

	public static String getDisplayName(BindingConverter converter, IBindingConverter converterInstance) {
		if (converterInstance != null && converterInstance != DefaultBindingConverter.instance) {
			return converterInstance.getDisplayName();
		} else {
			if (displayNames.containsKey(converter.getType())) {
				return displayNames.get(converter.getType());
			}
			return converter.getType();
		}
	}

}
