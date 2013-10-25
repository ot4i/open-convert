/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3025551918" > 
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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Point;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.FlowResource;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.binding.WSExportConverter;
import com.ibm.etools.mft.conversion.esb.extension.mediationprimitive.CalloutConverter;
import com.ibm.etools.mft.conversion.esb.model.ClassDefinition;
import com.ibm.etools.mft.conversion.esb.model.GlobalConfigurationType;
import com.ibm.etools.mft.conversion.esb.model.PrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.Node;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;

/**
 * @author Zhongming Chen
 *
 */
public class PrimitiveManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public static final String PLUGIN_SPACE = "PLUGIN_SPACE"; //$NON-NLS-1$
	public static final String WORKSPACE = "WORKSPACE"; //$NON-NLS-1$
	public static final String JAR_SPACE = "JAR_SPACE"; //$NON-NLS-1$

	private static HashMap<String, IPrimitiveConverter> mediationPrimitiveHandlers = new HashMap<String, IPrimitiveConverter>();

	static {
		loadMediationPrimitiveHandlers();
	}

	private static void loadMediationPrimitiveHandlers() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		try {
			IConfigurationElement[] handlerConfigurations = extensionRegistry
					.getConfigurationElementsFor(WESBConversionConstants.MEDIATION_PRIMITIVE_HANDLER_ID);
			for (IConfigurationElement current : handlerConfigurations) {
				IPrimitiveConverter handler = (IPrimitiveConverter) current
						.createExecutableExtension(WESBConversionConstants.CLASS_ATTRIBUTE_NAME);

				mediationPrimitiveHandlers.put(handler.getType(), handler);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static Object[] getPrimitiveMappings(GlobalConfigurationType model) {
		HashMap<String, PrimitiveConverter> result = new HashMap<String, PrimitiveConverter>();
		for (String k : mediationPrimitiveHandlers.keySet()) {
			PrimitiveConverter v = new PrimitiveConverter();
			v.setType(k);
			ClassDefinition clazz = new ClassDefinition();
			clazz.setClazz(mediationPrimitiveHandlers.get(k).getClass().getName());
			v.setClazz(clazz);
			result.put(k, v);
		}
		for (PrimitiveConverter m : model.getPrimitiveConverters()) {
			result.put(m.getType(), m);
		}
		return result.values().toArray();
	}

	public static boolean isUserDefined(String type) {
		return !mediationPrimitiveHandlers.containsKey(type);
	}

	public static IPrimitiveConverter getMediationPrimitiveHandler(String type) {
		return mediationPrimitiveHandlers.get(type);
	}

	public static PrimitiveConverter getPrimitiveConverter(GlobalConfigurationType model, String type) {
		for (PrimitiveConverter pm : model.getPrimitiveConverters()) {
			if (pm.getType().equals(type)) {
				return pm;
			}
		}
		return null;
	}

	public static String getNodeName(com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive,
			HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> primitiveToNodes, String primitiveSuffix) {
		String proposedName = primitive.getName();
		proposedName = ConversionUtils.encodeName(proposedName);
		if (isNameInUse(proposedName, primitiveToNodes.values())) {
			if (primitiveSuffix != null) {
				proposedName = proposedName + primitiveSuffix; //$NON-NLS-1$
				proposedName = ConversionUtils.encodeName(proposedName);
			}
		}
		int i = 0;
		String baseName = proposedName;
		while (isNameInUse(proposedName, primitiveToNodes.values())) {
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

	public static com.ibm.broker.config.appdev.Node createNode(MessageFlow parent, String nodeName, String roleName,
			Class nodeClass, Nodes nodes) throws Exception {
		Point location = getLocation();
		com.ibm.broker.config.appdev.Node n = (com.ibm.broker.config.appdev.Node) nodeClass.newInstance();
		n.setNodeName(nodeName);
		parent.addNode(n);
		n.setLocation(location);
		if (nodes != null) {
			nodes.addNode(roleName, n);
		}
		return n;
	}

	public static Point getLocation() {
		return new Point(100, 100);
	}

	public static void setContext(ConversionContext context) {
		DefaultMediationPrimitiveConverter.instance.setConversionContext(context);
		for (IPrimitiveConverter handler : mediationPrimitiveHandlers.values()) {
			handler.setConversionContext(context);
		}
	}

	public static AbstractProperty getProperty(Node node, String key) {
		for (JAXBElement e : node.getAbstractProperty()) {
			AbstractProperty p = (AbstractProperty) e.getValue();
			if (p.getName().equals(key)) {
				return p;
			}
		}
		return null;
	}

	public static MessageFlow getOrCreateMessageFlow(ConversionContext context, FlowResourceManager flowManager, String folderName,
			String flowName, String flowType) throws IOException {
		IFile flowFile = context.helper.getTargetFile(context.resource.getProject().getFile(
				ConversionUtils.getFullyQualifiedFlowName(folderName, flowName) + flowType));
		MessageFlow flow = null;
		if (flowFile.exists()) {
			try {
				flowFile.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
			}
		}

		flow = new MessageFlow(ConversionUtils.getFullyQualifiedFlowName(folderName, flowName) + flowType);

		flowManager.addFlow(getFullyQualifiedFlowName(flow), new FlowResource(flow, flowFile));
		return flow;
	}

	public static String getFullyQualifiedFlowName(MessageFlow flow) {
		return ConversionUtils.getFullyQualifiedFlowName(flow.getBrokerSchema(), flow.getName());
	}

	public static Property getProperty(Row r, String key) {
		for (Property p : r.getProperty()) {
			if (p.getName().equals(key)) {
				return p;
			}
		}
		return null;
	}

	public static String resolveESQLPath(String value, boolean isInput) {
		value = value.replace('/', '.');
		if (value.startsWith("$LocalEnvironment.")) { //$NON-NLS-1$
			value = (isInput ? "Input" : "Output") + value.substring(1); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	public static boolean isEdgeNode(com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive) {
		String type = primitive.getType();
		return "Input".equals(type) || "InputResponse".equals(type) || "Callout".equals(type) || "CalloutResponse".equals(type); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public static IPrimitiveConverter getConverter(String type, ConversionContext context, WESBConversionDataType model)
			throws Exception {
		PrimitiveConverter mp = PrimitiveManager.getPrimitiveConverter(model.getGlobalConfiguration(), type);
		if (mp == null || mp.getClazz() == null) {
			IPrimitiveConverter result = PrimitiveManager.getMediationPrimitiveHandler(type);
			if (result != null) {
				return result;
			}
		} else {
			ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
			ConversionClassLoader classLoader = new ConversionClassLoader(new URL[0]);
			classLoader.setClassDefinition(mp.getClazz(), oldLoader);
			try {
				Thread.currentThread().setContextClassLoader(classLoader);
				Class clazz = classLoader.loadClass(mp.getClazz().getClazz());
				if (clazz != null) {
					IPrimitiveConverter result = (IPrimitiveConverter) clazz.newInstance();
					result.setConversionContext(context);
					return result;
				}
			} finally {
				Thread.currentThread().setContextClassLoader(oldLoader);
			}
		}
		return DefaultMediationPrimitiveConverter.instance;
	}

	public static String getConverterDisplayName(String s) {
		if (DefaultMediationPrimitiveConverter.class.getName().equals(s)) {
			s = WESBConversionMessages.defaultConverter;
		} else if (s.startsWith(CalloutConverter.class.getPackage().getName())) {
			s = WESBConversionMessages.builtInConverter;
		} else if (s.startsWith(WSExportConverter.class.getPackage().getName())) {
			s = WESBConversionMessages.builtInConverter;
		} else if (DefaultBindingConverter.class.getName().equals(s)) {
			s = WESBConversionMessages.defaultConverter;
		}
		return s;
	}

}
