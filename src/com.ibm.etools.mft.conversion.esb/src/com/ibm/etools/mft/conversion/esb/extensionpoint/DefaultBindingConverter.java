/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import javax.wsdl.OperationType;

import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.PortType;

import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.PassthroughNode;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.wsspi.sca.scdl.ExportBinding;
import com.ibm.wsspi.sca.scdl.ImportBinding;
import com.ibm.wsspi.sca.scdl.wsdl.WSDLPortType;

/**
 * @author Zhongming Chen
 * 
 */
public class DefaultBindingConverter extends AbstractBindingConverter {

	public static DefaultBindingConverter instance = new DefaultBindingConverter();

	/**
	 * 
	 */
	public DefaultBindingConverter() {
	}

	@Override
	public String getType() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromBinding(converterContext);

		boolean isOneWay = isOperationsInTheInterfaceAllOneway(converterContext.portType);
		if (converterContext.sourceBinding instanceof ExportBinding) {
			// create entry java compute node
			PassthroughNode entryNode = (PassthroughNode) createNode(converterContext.targetFlow, nodeName, ROLE_ENTRY,
					PassthroughNode.class, //$NON-NLS-1$
					nodes);
			createStickyNote(
					converterContext.targetFlow,
					new Node[] { entryNode },
					"Implement the Export binding (The request endpoint) manually \nby replacing the Passthrough node with your implementation node \n(E.g. MQInput + JavaCompute). "); //$NON-NLS-1$

			if (!isOneWay) {
				// create exit java compute node
				PassthroughNode replyNode = (PassthroughNode) createNode(converterContext.targetFlow, nodeName + REPLY_NODE_SUFFIX,
						ROLE_EXIT, PassthroughNode.class, //$NON-NLS-1$
						nodes);
				createStickyNote(
						converterContext.targetFlow,
						new Node[] { replyNode },
						"Implement the Export binding (The response endpoint) manually \nby replacing the Passthrough node with your implementation node \n(E.g. MQOutput)."); //$NON-NLS-1$

				createToDoTask(getMainFlowFile(converterContext), NLS.bind(WESBConversionMessages.todoConfigureUnsupportedBinding,
						converterContext.sourceBinding.getClass().getName(), nodeName), converterContext.flowManager
						.getFlowResource(converterContext.targetFlow).getFullPath().toString());
			}
		} else if (converterContext.sourceBinding instanceof ImportBinding) {

			PassthroughNode passthrough = (PassthroughNode) createNode(converterContext.targetFlow, nodeName, ROLE_MAIN,
					PassthroughNode.class, //$NON-NLS-1$
					nodes);
			createStickyNote(
					converterContext.targetFlow,
					new Node[] { passthrough },
					"Replace the passthrough node with the implmenetation to the Import binding manually.\nMore information can be found on http://somewhere/todo."); //$NON-NLS-1$

			createToDoTask(getMainFlowFile(converterContext), NLS.bind(WESBConversionMessages.todoConfigureUnsupportedBinding,
					converterContext.sourceBinding.getClass().getName(), nodeName),
					converterContext.flowManager.getFlowResource(converterContext.targetFlow).getFullPath().toString());

		}

		return nodes;
	}

	private boolean isOperationsInTheInterfaceAllOneway(WSDLPortType portType) {
		if (portType == null) {
			return false;
		}
		PortType wsdlPortType = conversionContext.indexer.wsdlPortTypes.get(portType.getPortType().toString());
		if (wsdlPortType == null) {
			return false;
		}
		for (Object o : wsdlPortType.getOperations()) {
			if (o instanceof Operation) {
				if (!OperationType.ONE_WAY.equals(((Operation) o).getStyle())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Terminal getInputTerminal(String sourceTerminalName, Nodes nodes) {
		Node n = nodes.getNode(ROLE_EXIT); //$NON-NLS-1$
		if (n != null) {
			return n.getInputTerminals()[0];
		}
		n = nodes.getNode(ROLE_MAIN); //$NON-NLS-1$
		if (n != null) {
			return n.getInputTerminals()[0];
		}
		return null;
	}

	@Override
	public Terminal getOutputTerminal(String sourceTerminalName, Nodes nodes) {
		Node n = nodes.getNode(ROLE_ENTRY); //$NON-NLS-1$
		if (n != null) {
			return n.getOutputTerminals()[0];
		}
		n = nodes.getNode(ROLE_MAIN); //$NON-NLS-1$
		if (n != null) {
			return n.getOutputTerminals()[0];
		}
		return null;
	}

	@Override
	public String getConvertedTo() {
		return "Placeholder"; //$NON-NLS-1$
	}

	@Override
	public String getDisplayName() {
		return getType();
	}
}
