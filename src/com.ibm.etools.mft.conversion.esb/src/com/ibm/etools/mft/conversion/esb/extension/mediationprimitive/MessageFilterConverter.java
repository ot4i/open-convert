/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.NLS;

import com.ibm.broker.config.appdev.NamespacePrefixMap;
import com.ibm.broker.config.appdev.nodes.RouteNode;
import com.ibm.broker.config.appdev.nodes.RouteNode.ENUM_ROUTE_DISTRIBUTIONMODE;
import com.ibm.broker.config.appdev.nodes.RouteNode.FilterTableRow;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext.MappedPath;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;
import com.ibm.etools.mft.conversion.esb.model.mfc.Table;
import com.ibm.etools.mft.conversion.esb.model.mfc.Terminal;

/**
 * @author Zhongming Chen
 * 
 */
public class MessageFilterConverter extends AbstractMediationPrimitiveConverter {

	// The name of the WESB default terminal
	final String WESB_DEFAULT = "default"; //$NON-NLS-1$

	// WESB uses a 'displayName' (if the user has defined one) instead of the
	// 'name'
	// for an output terminal, so we should use the displayName in Broker.
	// Map terminal names to displayNames for each Message Filter primitive
	Map<String, Map<String, String>> filterMaps = new Hashtable<String, Map<String, String>>();

	/**
	 * 
	 */
	public MessageFilterConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "MessageFilter"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		RouteNode node = (RouteNode) createNode(converterContext.targetFlow, nodeName, ROLE_MAIN, RouteNode.class, nodes); //$NON-NLS-1$
		// create and save a new 'displayName' Map for this Message Filter
		// primitive
		Map<String, String> displayNames = new Hashtable<String, String>();
		filterMaps.put(nodeName, displayNames);
		for (Terminal t : converterContext.sourcePrimitive.getOutputTerminal()) {
			String name = getTerminalName(t);
			// get the displayName for a terminal
			String displayName = null;
			if (t instanceof com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) {
				displayName = t.getDisplayName(); // could return null
			}
			if (displayName == null) {
				displayName = name;
			}
			displayNames.put(name, displayName);
			// Don't create the WESB "default" terminal, it's replaced by the
			// Broker "Default" terminal, which is always created with the node
			if (!WESB_DEFAULT.equals(name)) {
				node.getOutputTerminal(displayName);
			}
		}

		// Set distribution mode to 'first', which is the default in WESB
		node.setDistributionMode(ENUM_ROUTE_DISTRIBUTIONMODE.first);

		AbstractProperty p = getPropertyOfSourcePrimitive(converterContext, "filters");
		if (p != null) {
			updateFilters(getTargetFlowFile(converterContext), converterContext.inputNodeInSourceFlow, node, p, displayNames);
		}
		p = getPropertyOfSourcePrimitive(converterContext, "distributionMode");
		if (p != null) {
			updateDistributionMode(node, p);
		}
		p = getPropertyOfSourcePrimitive(converterContext, "enabled");
		if (p != null) {
			updateEnabled(converterContext.flowManager.getFlowResource(converterContext.targetFlow),
					converterContext.sourcePrimitive, node, p);
		}

		return nodes;
	}

	protected void updateFilters(IFile targetFile, com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, RouteNode node,
			AbstractProperty p, Map<String, String> displayNames) {
		if (!(p instanceof Table)) {
			return;
		}
		Table table = (Table) p;

		node.getFilterTable().getRows().clear();
		node.getNsmappingtables().clear();

		for (Row r : table.getRow()) {
			Property pattern = getColumnValueInARow(r, "pattern"); //$NON-NLS-1$
			Property terminalName = getColumnValueInARow(r, "terminalName"); //$NON-NLS-1$
			FilterTableRow targetRow = node.getFilterTable().createRow();
			MappedPath mp = mapXPath(targetFile, inputNode, pattern.getValue());
			targetRow.setFilterPattern(mp.mappedPath);
			if (mp.mappedPath == null) {
				// can't find a mapping.
				targetRow.setFilterPattern(pattern.getValue());
				createToDoTask(targetFile,
						NLS.bind(WESBConversionMessages.todoUnsupportedPathInFilterPattern, pattern.getValue(), node.getNodeName()));
			}
			for (String namespace : mp.namespaceToPrefix.keySet()) {
				String prefix = mp.namespaceToPrefix.get(namespace);
				NamespacePrefixMap nspm = new NamespacePrefixMap();
				nspm.setNamespace(namespace);
				nspm.setNsPrefix(prefix);
				node.addNsmapping(nspm);
			}
			String displayName = displayNames.get(terminalName.getValue());
			targetRow.setRoutingOutputTerminal(displayName);
			node.getFilterTable().addRow(targetRow);
		}
	}

	// "distributionMode" property has been found on the WESB filter primitive
	protected void updateDistributionMode(RouteNode node, AbstractProperty p) {
		if (p instanceof Property) {
			String mode = ((Property) p).getValue();
			if ("1".equals(mode)) { //$NON-NLS-1$
				// WESB distribution mode is 'All'
				node.setDistributionMode(ENUM_ROUTE_DISTRIBUTIONMODE.all);
			}
		}
	}

	// "enabled" property has been found on the WESB filter primitive
	protected void updateEnabled(IFile targetFile, com.ibm.etools.mft.conversion.esb.model.mfc.Node sourcePrimitive,
			RouteNode node, AbstractProperty p) {
		if (p instanceof Property) {
			String enabled = ((Property) p).getValue();
			if ("false".equals(enabled)) { //$NON-NLS-1$
				// The WESB filter has been disabled. There is no corresponding
				// property in Broker so issue a ToDo
				createToDoTask(targetFile,
						NLS.bind(WESBConversionMessages.todoDisableMessageFilter, node.getNodeName(), sourcePrimitive.getName()));
			}
		}
	}

	@Override
	public com.ibm.broker.config.appdev.Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		RouteNode n = (RouteNode) nodes.getNode(ROLE_MAIN); //$NON-NLS-1$
		if ("Fail".equals(sourceName)) { //$NON-NLS-1$
			return n.OUTPUT_TERMINAL_FAILURE;
		} else if ("Failure".equals(sourceName)) { //$NON-NLS-1$
			return n.OUTPUT_TERMINAL_FAILURE;
		} else if (WESB_DEFAULT.equals(sourceName)) {
			return n.OUTPUT_TERMINAL_DEFAULT;
		}

		// Use the displayName, if there is one
		Map<String, String> displayNames = filterMaps.get(n.getNodeName());
		if (displayNames != null) {
			sourceName = displayNames.get(sourceName);
		}
		return findTerminal(n.getOutputTerminals(), sourceName); //$NON-NLS-1$
	}

	@Override
	public com.ibm.broker.config.appdev.Terminal getInputTerminal(String sourceName, Nodes nodes) {
		return ((RouteNode) nodes.getNode(ROLE_MAIN)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public String getConvertedTo() {
		return "Route"; //$NON-NLS-1$
	}
}
