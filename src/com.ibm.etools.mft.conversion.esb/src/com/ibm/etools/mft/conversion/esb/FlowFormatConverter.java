package com.ibm.etools.mft.conversion.esb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.FailTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.InputTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Interface;
import com.ibm.etools.mft.conversion.esb.model.mfc.MediationFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Node;
import com.ibm.etools.mft.conversion.esb.model.mfc.ObjectFactory;
import com.ibm.etools.mft.conversion.esb.model.mfc.Operation;
import com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.RequestFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.ResponseFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;
import com.ibm.etools.mft.conversion.esb.model.mfc.Table;
import com.ibm.etools.mft.conversion.esb.model.mfc.Terminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Wire;
import com.ibm.ws.sibx.scax.mediation.model.CalloutNode;
import com.ibm.ws.sibx.scax.mediation.model.ComponentFlows;
import com.ibm.ws.sibx.scax.mediation.model.Connection;
import com.ibm.ws.sibx.scax.mediation.model.CustomMediationNode;
import com.ibm.ws.sibx.scax.mediation.model.ErrorInputNode;
import com.ibm.ws.sibx.scax.mediation.model.FaultFlowModel;
import com.ibm.ws.sibx.scax.mediation.model.InNode;
import com.ibm.ws.sibx.scax.mediation.model.InputFaultNode;
import com.ibm.ws.sibx.scax.mediation.model.InputNode;
import com.ibm.ws.sibx.scax.mediation.model.InputResponseNode;
import com.ibm.ws.sibx.scax.mediation.model.MediationPrimitiveNode;
import com.ibm.ws.sibx.scax.mediation.model.NodeProperty;
import com.ibm.ws.sibx.scax.mediation.model.RequestFlowModel;
import com.ibm.ws.sibx.scax.mediation.model.ResponseFaultNode;
import com.ibm.ws.sibx.scax.mediation.model.ResponseFlowModel;
import com.ibm.ws.sibx.scax.mediation.model.ResponseNode;

/**
 * This class convert mediation flow format prior to WESB 7.5 to new format --
 * readable mediation flow.
 * 
 * @author Zhongming Chen, Nikita Hickson
 * 
 */
public class FlowFormatConverter {

	private MediationFlow newModel;
	private ComponentFlows oldModel;
	private HashMap<String, Interface> interfaces = new HashMap<String, Interface>();
	private ObjectFactory factory = new ObjectFactory();
	private HashMap<String, Node> nodes = new HashMap<String, Node>();

	public FlowFormatConverter(ComponentFlows compFlows, MediationFlow mfc) {
		this.oldModel = compFlows;
		this.newModel = mfc;
	}

	public void convert() throws Exception {
		for (Object o : oldModel.getRequestFlows()) {
			if (o instanceof RequestFlowModel) {
				RequestFlowModel oldFlow = (RequestFlowModel) o;
				// create interface and operation
				String interfaceQName = ConversionUtils.getQName(oldFlow.getInterfaceNamespace(), oldFlow.getInterfaceLocalName());
				Interface theInterface = interfaces.get(interfaceQName);
				if (theInterface == null) {
					interfaces.put(interfaceQName, theInterface = new Interface());
					newModel.getInterface().add(theInterface);
				}
				String opName = oldFlow.getOperationName();
				Operation operation = new Operation();
				operation.setName(opName);
				theInterface.getOperation().add(operation);

				newModel.setName(oldModel.getComponentName());

				nodes.clear();

				// visit request flow
				RequestFlow newFlow = new RequestFlow();
				operation.setRequestFlow(newFlow);
				createNode(oldFlow.getInitialNode());
				newFlow.getNode().addAll(nodes.values());

				// visit all response flow
				if (oldFlow.getResponseFlows().size() > 0) {
					ResponseFlow newResponseFlow = new ResponseFlow();
					nodes.clear();
					for (Object o1 : oldFlow.getResponseFlows()) {
						if (o1 instanceof ResponseFlowModel) {
							createNode(((ResponseFlowModel) o1).getInitialNode());
						}
					}
					for (Object o2 : oldFlow.getFaultFlows()) {
						if (o2 instanceof FaultFlowModel) {
							createNode(((FaultFlowModel) o2).getInitialNode());
						}
					}
					newResponseFlow.getNode().addAll(nodes.values());
					operation.setResponseFlow(newResponseFlow);
				}

			}
		}
		// TODO: Promoted properties. This should be the lowest priority because
		// current WESB conversion doesn't support property promotion.
	}

	protected void createNode(com.ibm.ws.sibx.scax.mediation.model.Node oldNode) throws Exception {
		if (nodes.containsKey(oldNode.toString())) {
			return;
		}
		Node newNode = new Node();
		newNode.setDisplayName(oldNode.getDisplayName());
		newNode.setName(convertNodeName(oldNode));
		newNode.setType(convertNodeType(oldNode));

		populateProperties(newNode, oldNode);

		populateTerminals(newNode, oldNode);

		nodes.put(oldNode.toString(), newNode);
	}

	protected void populateTerminals(Node newNode, com.ibm.ws.sibx.scax.mediation.model.Node oldNode) throws Exception {
		if (oldNode instanceof CalloutNode) {
			Terminal newTerminal = createTerminal(((CalloutNode) oldNode).getInputTerminal(), InputTerminal.class);
			newNode.getInputTerminal().add((InputTerminal) newTerminal);
		} else if (oldNode instanceof CustomMediationNode) {
			Terminal newTerminal = createTerminal(((CustomMediationNode) oldNode).getInputTerminal(), InputTerminal.class);
			newNode.getInputTerminal().add((InputTerminal) newTerminal);
			newTerminal = createTerminal(((CustomMediationNode) oldNode).getOutputTerminal(), OutputTerminal.class);
			newNode.getOutputTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) newTerminal);
			newTerminal = createTerminal(((CustomMediationNode) oldNode).getFailureTerminal(), FailTerminal.class);
			newNode.getFailTerminal().add((FailTerminal) newTerminal);
		} else if (oldNode instanceof ErrorInputNode) {
			Terminal newTerminal = createTerminal(((ErrorInputNode) oldNode).getOutputTerminal(), OutputTerminal.class);
			newNode.getOutputTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) newTerminal);
		} else if (oldNode instanceof InNode) {
			Terminal newTerminal = createTerminal(((InNode) oldNode).getOutputTerminal(), OutputTerminal.class);
			newNode.getOutputTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) newTerminal);
		} else if (oldNode instanceof InputFaultNode) {
			for (Object o : ((InputFaultNode) oldNode).getInputTerminals().values()) {
				if (o instanceof com.ibm.ws.sibx.scax.mediation.model.Terminal) {
					Terminal newTerminal = createTerminal((com.ibm.ws.sibx.scax.mediation.model.Terminal) o, InputTerminal.class);
					newNode.getInputTerminal().add((InputTerminal) newTerminal);
				}
			}
		} else if (oldNode instanceof ResponseFaultNode) {
			if (((ResponseFaultNode) oldNode).getOutputTerminal() != null) {
				Terminal newTerminal = createTerminal(((ResponseFaultNode) oldNode).getOutputTerminal(), FailTerminal.class);
				newNode.getFailTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.FailTerminal) newTerminal);
			}
		} else if (oldNode instanceof InputNode) {
			Terminal newTerminal = createTerminal(((InputNode) oldNode).getOutputTerminal(), OutputTerminal.class);
			if (newTerminal != null) {
				newNode.getOutputTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) newTerminal);
			}
		} else if (oldNode instanceof InputResponseNode) {
			Terminal newTerminal = createTerminal(((InputResponseNode) oldNode).getInputTerminal(), InputTerminal.class);
			newNode.getInputTerminal().add((InputTerminal) newTerminal);
		} else if (oldNode instanceof ResponseNode) {
			Terminal newTerminal = createTerminal(((ResponseNode) oldNode).getOutputTerminal(), OutputTerminal.class);
			newNode.getOutputTerminal().add((com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) newTerminal);
		} else if (oldNode instanceof MediationPrimitiveNode) {
			for (Object o : ((MediationPrimitiveNode) oldNode).getInputTerminals().values()) {
				if (o instanceof com.ibm.ws.sibx.scax.mediation.model.Terminal) {
					Terminal newTerminal = createTerminal((com.ibm.ws.sibx.scax.mediation.model.Terminal) o, InputTerminal.class);
					newNode.getInputTerminal().add((InputTerminal) newTerminal);
				}
			}
			for (Object o : ((MediationPrimitiveNode) oldNode).getOuputTerminals().values()) {
				if (o instanceof com.ibm.ws.sibx.scax.mediation.model.Terminal) {
					Terminal newTerminal = createTerminal((com.ibm.ws.sibx.scax.mediation.model.Terminal) o, OutputTerminal.class);
					newNode.getOutputTerminal().add((OutputTerminal) newTerminal);
				}
			}
			Object o = ((MediationPrimitiveNode) oldNode).getFailureTerminal();
			if (o instanceof com.ibm.ws.sibx.scax.mediation.model.Terminal) {
				Terminal newTerminal = createTerminal((com.ibm.ws.sibx.scax.mediation.model.Terminal) o, FailTerminal.class);
				newNode.getFailTerminal().add((FailTerminal) newTerminal);
			}
		}
	}

	protected Terminal createTerminal(com.ibm.ws.sibx.scax.mediation.model.Terminal oldTerminal, Class terminalClass)
			throws Exception {
		if (oldTerminal == null) {
			return null;
		}
		Terminal newTerminal = (Terminal) terminalClass.newInstance();
		newTerminal.setDisplayName(oldTerminal.getDisplayName());
		newTerminal.setName(convertTerminalName(oldTerminal));
		newTerminal.setType(convertTerminalType(newTerminal, oldTerminal));

		for (Object o : oldTerminal.getConnections()) {
			if (o instanceof Connection) {
				Wire wire = createWire(((Connection) o));
				if (newTerminal instanceof FailTerminal) {
					newTerminal.setName(null);
					((FailTerminal) newTerminal).getWire().add(wire);
				} else if (newTerminal instanceof OutputTerminal) {
					((OutputTerminal) newTerminal).getWire().add(wire);
				}
			}
		}

		return newTerminal;
	}

	protected Wire createWire(Connection connection) throws Exception {
		Wire wire = new Wire();
		if (connection.getTargetNode().getNodeType().startsWith("MediationPrimitiveNode")) //$NON-NLS-1$
		{
			wire.setTargetNode(connection.getTargetNode().getDisplayName());
		} else {
			wire.setTargetNode(connection.getTargetNode().getName());
		}
		wire.setTargetTerminal(connection.getTargetInputTerminal());
		createNode(connection.getTargetNode());
		return wire;
	}

	protected void populateProperties(Node newNode, com.ibm.ws.sibx.scax.mediation.model.Node oldNode) {
		HashMap<String, JAXBElement<? extends AbstractProperty>> ps = new HashMap<String, JAXBElement<? extends AbstractProperty>>();
		for (Object key : oldNode.getNodeProperties().keySet()) {
			Object value = oldNode.getNodeProperties().get(key);
			createProperty(key, value, ps);
		}
		newNode.getAbstractProperty().addAll(ps.values());
	}

	protected void createProperty(Object key, Object value, HashMap<String, JAXBElement<? extends AbstractProperty>> newProperties) {
		if (value instanceof NodeProperty) {
			String name = ((NodeProperty) value).getName();
			if (newProperties.containsKey(name)) {
				return;
			}
			int index = name.indexOf("."); //$NON-NLS-1$
			if (index > 0) {
				// table property
				String tableName = name.substring(0, index);
				String columnName = name.substring(index + 1);
				Table tp = null;
				if (newProperties.containsKey(tableName)) {
					AbstractProperty p = newProperties.get(tableName).getValue();
					if (!(p instanceof Table)) {
						newProperties.put(tableName, factory.createAbstractProperty(p = new Table()));
					}
					tp = (Table) p;
				} else {
					tp = new Table();
					tp.setName(tableName);
					newProperties.put(tableName, factory.createAbstractProperty(tp));
				}

				String rowsValue = ((NodeProperty) value).getValue();
				StringTokenizer st = new StringTokenizer(rowsValue, "^"); //$NON-NLS-1$
				int rowIndex = 0;
				while (st.hasMoreTokens()) {
					String rowValue = st.nextToken();
					try {
						rowValue = URLDecoder.decode(rowValue, "UTF-8"); //$NON-NLS-1$
					} catch (UnsupportedEncodingException e) {
					}
					Row row = null;
					if (rowIndex < tp.getRow().size()) {
						row = tp.getRow().get(rowIndex);
					} else {
						row = new Row();
						tp.getRow().add(row);
					}
					Property p = new Property();
					p.setName(columnName);
					p.setValue(rowValue);
					row.getProperty().add(p);
					rowIndex++;
				}
			} else {
				Property p = new Property();
				p.setName(((NodeProperty) value).getName());
				p.setValue(((NodeProperty) value).getValue());
				newProperties.put(name, factory.createAbstractProperty(p));
			}
		} else {
		}
	}

	protected String convertNodeType(com.ibm.ws.sibx.scax.mediation.model.Node oldNode) {
		String nodeType = oldNode.getNodeType();
		if ("ResponseNode".equals(nodeType) || nodeType.startsWith("ResponseFaultNode")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return "CalloutResponse"; //$NON-NLS-1$
		} else if (nodeType.startsWith("MediationPrimitiveNode-")) //$NON-NLS-1$
		{
			nodeType = nodeType.substring("MediationPrimitiveNode-".length()); //$NON-NLS-1$
		} else if ("CalloutNode".equals(nodeType)) //$NON-NLS-1$
		{
			nodeType = "Callout"; //$NON-NLS-1$
		} else if ("InputNode".equals(nodeType)) //$NON-NLS-1$
		{
			nodeType = "Input"; //$NON-NLS-1$
		} else if ("InputResponseNode".equals(nodeType)) //$NON-NLS-1$
		{
			nodeType = "InputResponse"; //$NON-NLS-1$
		}

		return nodeType;
	}

	protected String convertNodeName(com.ibm.ws.sibx.scax.mediation.model.Node oldNode) {
		String nodeName = oldNode.getName();
		if (oldNode.getNodeType().startsWith("MediationPrimitiveNode")) //$NON-NLS-1$
		{
			nodeName = oldNode.getDisplayName();
		}
		return nodeName;
	}

	protected QName convertTerminalType(Terminal newTerminal, com.ibm.ws.sibx.scax.mediation.model.Terminal oldTerminal) {
		QName newType = new QName(oldTerminal.getBodyTypeNamespace(), oldTerminal.getBodyTypeLocalName());
		return newType;
	}

	protected String convertTerminalName(com.ibm.ws.sibx.scax.mediation.model.Terminal oldTerminal) {
		String name = oldTerminal.getName().toUpperCase();
		if ("OUT".equals(name)) //$NON-NLS-1$
		{
			return "Out"; //$NON-NLS-1$
		} else if ("FAIL".equals(name)) //$NON-NLS-1$
		{
			return "Fail"; //$NON-NLS-1$
		}
		return oldTerminal.getName();
	}

}
