/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.OutputNode;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;

/**
 * @author Zhongming Chen
 * 
 */
public class InputResponseConverter extends AbstractMediationPrimitiveConverter {

	/**
	 * 
	 */
	public InputResponseConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "InputResponse"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node n : converterContext.moduleConverter.getPrimitiveToNodes().keySet()) {
			if (n.getName().equals(converterContext.sourcePrimitive.getName())) {
				Nodes nodes = converterContext.moduleConverter.getPrimitiveToNodes().get(n);
				converterContext.moduleConverter.getPrimitiveToNodes().put(converterContext.sourcePrimitive, nodes);
				return nodes;
			}
		}
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		createNode(converterContext.targetFlow, nodeName, ROLE_MAIN, OutputNode.class, nodes); //$NON-NLS-1$
		return nodes;
	}

	@Override
	public Terminal getInputTerminal(String sourceName, Nodes nodes) {
		return ((OutputNode) nodes.getNode(ROLE_MAIN)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public String getConvertedTo() {
		return WESBConversionMessages.InputResponseConverter_convertedTo0;
	}
}
