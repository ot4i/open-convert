/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.HashMap;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.resources.IFile;

import com.ibm.broker.config.appdev.InputTerminal;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.SubFlowNode;
import com.ibm.broker.config.appdev.nodes.InputNode;
import com.ibm.broker.config.appdev.nodes.OutputNode;
import com.ibm.broker.config.appdev.nodes.PassthroughNode;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper.NodeObject;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext.MappedPath;
import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.FailTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Flow;
import com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.ResponseFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;
import com.ibm.etools.mft.conversion.esb.model.mfc.Terminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Wire;
import com.ibm.etools.mft.util.WMQIConstants;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class AbstractMediationPrimitiveConverter extends AbstractWESBArtifactConverter implements IPrimitiveConverter {

	/**
	 * 
	 */
	public AbstractMediationPrimitiveConverter() {
	}

	protected Nodes getSourceNodes(com.ibm.etools.mft.conversion.esb.model.mfc.Node sourcePrimitive,
			HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> primitiveToNodes) {
		return primitiveToNodes.get(sourcePrimitive);
	}

	protected Nodes createNodes(ConverterContext converterContext) {
		Nodes nc = new Nodes();
		converterContext.moduleConverter.getPrimitiveToNodes().put(converterContext.sourcePrimitive, nc);
		return nc;
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		Nodes ns = createNodes(converterContext);
		SubFlowNode n = (SubFlowNode) createNode(converterContext.targetFlow, nodeName, "main", SubFlowNode.class, ns); //$NON-NLS-1$
		String subflowName = converterContext.targetFlow.getName() + "_" + nodeName; //$NON-NLS-1$
		MessageFlow subflow = PrimitiveManager.getOrCreateMessageFlow(conversionContext, converterContext.flowManager,
				converterContext.targetFlow.getName(), subflowName, WMQIConstants.MESSAGE_SUBFLOW_EXTENSION);
		InputNode inTerminal = null;
		OutputNode outTerminal = null;
		for (Terminal t : converterContext.sourcePrimitive.getInputTerminal()) {
			String terminalName = getTerminalName(t);
			InputNode in = (InputNode) createNode(subflow, terminalName, null, InputNode.class, null);
			subflow.addNode(in);
			// set inTerminal to the "In" terminal if there is one, else the
			// first one we find
			if (IN_TERMINAL.equals(terminalName) || inTerminal == null) {
				inTerminal = in;
			}
		}
		for (Terminal t : converterContext.sourcePrimitive.getOutputTerminal()) {
			String terminalName = getTerminalName(t);
			OutputNode out = (OutputNode) createNode(subflow, terminalName, null, OutputNode.class, null);
			subflow.addNode(out);
			// set outTerminal to the "Out" terminal if there is one, else the
			// first one we find
			if (OUT_TERMINAL.equals(terminalName) || outTerminal == null) {
				outTerminal = out;
			}
		}
		for (Terminal t : converterContext.sourcePrimitive.getFailTerminal()) {
			OutputNode fail = (OutputNode) createNode(subflow, getTerminalName(t), null, OutputNode.class, null);
			subflow.addNode(fail);
		}

		if (inTerminal != null) {
			PassthroughNode passThrough = (PassthroughNode) createNode(subflow,
					WESBConversionMessages.AbstractMediationPrimitiveConverter_PassThroughName, "passthrough", //$NON-NLS-2$ //$NON-NLS-1$
					PassthroughNode.class, null);
			createStickyNote(subflow, new Node[] { passThrough },
					WESBConversionMessages.AbstractMediationPrimitiveConverter_ToDoTask);
			subflow.connect(inTerminal.OUTPUT_TERMINAL_OUT, passThrough.INPUT_TERMINAL_IN);
			if (outTerminal != null) {
				subflow.connect(passThrough.OUTPUT_TERMINAL_OUT, outTerminal.INPUT_TERMINAL_IN);
			}
		}

		n.setSubFlow(subflow);
		return ns;
	}

	protected String getProposedIIBNodeNameFromSourcePrimitive(ConverterContext converterContext) {
		return PrimitiveManager
				.getNodeName(
						converterContext.sourcePrimitive,
						converterContext.moduleConverter.getPrimitiveToNodes(),
						(converterContext.sourceFlow instanceof ResponseFlow) ? WESBConversionConstants.SUFFIX_OF_MEDIATION_PRIMITIVE_IN_RESPONSE_FLOW
								: null);
	}

	@Override
	public void convertWire(MessageFlow targetFlow, Flow sourceFlow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNodeInSourceFlow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node sourcePrimitive,
			HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> primitiveToNodes) throws Exception {
		for (OutputTerminal t : sourcePrimitive.getOutputTerminal()) {
			if (t == null || t.getWire() == null) {
				continue;
			}
			for (Wire w : t.getWire()) {
				processWire(targetFlow, primitiveToNodes, sourcePrimitive, sourceFlow, t, w);
			}
		}

		for (FailTerminal t : sourcePrimitive.getFailTerminal()) {
			if (t == null || t.getWire() == null) {
				continue;
			}
			for (Wire w : t.getWire()) {
				processWire(targetFlow, primitiveToNodes, sourcePrimitive, sourceFlow, t, w);
			}
		}
	}

	protected void processWire(MessageFlow targetFlow,
			HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> primitiveToNodes,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node sourcePrimitive, Flow sourceFlow, Terminal t, Wire w) throws Exception {
		IPrimitiveConverter targetConverter = null;
		Nodes targetNodes = null;
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node p : sourceFlow.getNode()) {
			if (p.getName().equals(w.getTargetNode())) {
				targetConverter = conversionContext.getPrimitiveConverter(p.getType());
				targetNodes = primitiveToNodes.get(p);
				break;
			}
		}
		InputTerminal in = null;
		if (targetNodes != null) {
			in = (InputTerminal) targetConverter.getInputTerminal(w.getTargetTerminal(), targetNodes);
		} else {
			return;
		}

		Nodes sourceNodes = getSourceNodes(sourcePrimitive, primitiveToNodes);
		// FIXME: Put following logic into calloutresponse
		// if (primitiveName.endsWith("_CalloutResponse")) {
		// primitiveName = primitiveName.substring(0, primitiveName.length() -
		// "Response".length());
		// }
		com.ibm.broker.config.appdev.OutputTerminal out = null;
		if (sourceNodes != null) {
			out = (com.ibm.broker.config.appdev.OutputTerminal) getOutputTerminal(getTerminalName(t), sourceNodes);
		} else {
			return;
		}
		if (out != null && in != null) {
			targetFlow.connect(out, in);
		} else {
			int i = 0;
		}
	}

	@Override
	public com.ibm.broker.config.appdev.Terminal getInputTerminal(String sourceTerminalName, Nodes nodes) {
		Node node = nodes.getNode("main"); //$NON-NLS-1$
		if (sourceTerminalName != null) {
			com.ibm.broker.config.appdev.Terminal terminal = node.getInputTerminal(sourceTerminalName);
			if (terminal != null) {
				return terminal;
			}
		}
		if (node.getInputTerminals().length > 0) {
			return node.getInputTerminals()[0];
		}
		return null;
	}

	@Override
	public com.ibm.broker.config.appdev.Terminal getOutputTerminal(String sourceTerminalName, Nodes nodes) {
		Node node = nodes.getNode("main"); //$NON-NLS-1$
		if (sourceTerminalName != null) {
			com.ibm.broker.config.appdev.Terminal terminal = node.getOutputTerminal(sourceTerminalName);
			if (terminal != null) {
				return terminal;
			}
		}
		if (node.getOutputTerminals().length > 0) {
			return node.getOutputTerminals()[0];
		}
		return null;
	}

	protected AbstractProperty getPropertyOfSourcePrimitive(ConverterContext converterContext, String name) {
		for (JAXBElement o : converterContext.sourcePrimitive.getAbstractProperty()) {
			AbstractProperty p = (AbstractProperty) o.getValue();
			if (name.equals(p.getName())) {
				return p;
			}
		}
		return null;
	}

	protected Property getColumnValueInARow(Row r, String name) {
		return PrimitiveManager.getProperty(r, name);
	}

	protected MappedPath mapXPath(IFile targetFile, com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, String value) {
		return conversionContext.mapXPath(targetFile, inputNode, value);
	}

	protected IFile getTargetFlowFile(ConverterContext converterContext) {
		return converterContext.flowManager.getFlowResource(converterContext.targetFlow);
	}

	protected NodeObject getPartInAssemblyDiagram(ConverterContext converterContext, String name) {
		return converterContext.moduleConverter.getPart(name);
	}

}
