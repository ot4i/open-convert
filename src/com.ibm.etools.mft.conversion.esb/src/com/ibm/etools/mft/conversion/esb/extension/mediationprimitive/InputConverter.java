/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.InputNode;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;

/**
 * @author Zhongming Chen
 * 
 */
public class InputConverter extends AbstractMediationPrimitiveConverter {

	/**
	 * 
	 */
	public InputConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "Input"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		createNode(converterContext.targetFlow, nodeName, ROLE_MAIN, InputNode.class, nodes); //$NON-NLS-1$
		return nodes;
	}

	@Override
	public Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		return ((InputNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_OUT; //$NON-NLS-1$
	}

	@Override
	public String getConvertedTo() {
		return "Input"; //$NON-NLS-1$
	}
}
