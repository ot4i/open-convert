/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.MappingMSLNode;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;

/**
 * @author Zhongming Chen
 * 
 */
public class XSLTransformationConverter extends AbstractMediationPrimitiveConverter {

	public static final String TYPE = "XSLTransformation"; //$NON-NLS-1$

	/**
	 * 
	 */
	public XSLTransformationConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		MappingMSLNode node = (MappingMSLNode) createNode(converterContext.targetFlow, nodeName, ROLE_MAIN, MappingMSLNode.class,
				nodes); //$NON-NLS-1$
		Property p = (Property) PrimitiveManager.getProperty(converterContext.sourcePrimitive, "XMXMap"); //$NON-NLS-1$
		if (p != null) {
			IFile file = converterContext.flowManager.getFlowResource(converterContext.targetFlow).getProject()
					.getFile(p.getValue());
			if (!file.isAccessible()) {
				createToDoTask(
						getTargetFlowFile(converterContext),
						NLS.bind(WESBConversionMessages.todoMapIsMissing, file.getProjectRelativePath().toString(),
								node.getNodeName()));
			} else {
				Document dom = ConversionUtils.loadXML(file.getContents());

				String expr = "msl://"; //$NON-NLS-1$
				NodeList mappingRoots = dom.getElementsByTagName("mappingRoot"); //$NON-NLS-1$
				if (mappingRoots.getLength() > 0) {
					org.w3c.dom.Node root = mappingRoots.item(0);
					List<Element> nl = ConversionUtils.getImmediateChild((Element) root, null, "mappingDeclaration"); //$NON-NLS-1$
					if (nl.size() > 0) {
						Element declaration = nl.get(0);
						expr += "{" + ((Element) root).getAttribute("targetNamespace") + "}#" + declaration.getAttribute("name"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
				}

				node.setProperty("mappingExpression", expr); //$NON-NLS-1$
			}
		}

		return nodes;
	}

	@Override
	public Terminal getInputTerminal(String sourceName, Nodes nodes) {
		return ((MappingMSLNode) nodes.getNode(ROLE_MAIN)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		if ("Out".equals(sourceName)) { //$NON-NLS-1$
			return ((MappingMSLNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_OUT; //$NON-NLS-1$
		} else {
			return ((MappingMSLNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_FAILURE; //$NON-NLS-1$
		}
	}

	@Override
	public String getConvertedTo() {
		return "Map"; //$NON-NLS-1$
	}
}
