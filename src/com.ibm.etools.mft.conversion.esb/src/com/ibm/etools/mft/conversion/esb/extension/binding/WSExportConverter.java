/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.wsdl.Service;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.RouteToLabelNode;
import com.ibm.broker.config.appdev.nodes.SOAPExtractNode;
import com.ibm.broker.config.appdev.nodes.SOAPExtractNode.ENUM_SOAPEXTRACT_PATHMODE;
import com.ibm.broker.config.appdev.nodes.SOAPInputNode;
import com.ibm.broker.config.appdev.nodes.SOAPInputNode.ENUM_SOAPINPUT_TRANSPORT;
import com.ibm.broker.config.appdev.nodes.SOAPReplyNode;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WSDLUtils;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.wsspi.sca.scdl.Binding;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class WSExportConverter extends AbstractBindingConverter {

	/**
	 * 
	 */
	public WSExportConverter() {
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromBinding(converterContext);

		SOAPInputNode soapInputNode = (SOAPInputNode) createNode(converterContext.targetFlow, nodeName, ROLE_ENTRY, //$NON-NLS-1$
				SOAPInputNode.class, nodes);
		SOAPReplyNode soapReplyNode = (SOAPReplyNode) createNode(converterContext.targetFlow, nodeName + REPLY_NODE_SUFFIX,
				ROLE_EXIT, SOAPReplyNode.class, nodes); //$NON-NLS-1$

		String qName = getServiceQName(converterContext.sourceBinding);
		String portName = getPortName(converterContext.sourceBinding);
		IFile wsdlFile = conversionContext.indexer.wsdlServicesToFile.get(qName);
		if (wsdlFile != null) {
			soapInputNode.setWsdlFileName(wsdlFile.getProjectRelativePath().toString());
			Service s = conversionContext.indexer.wsdlServices.get(qName);
			soapInputNode.setSelectedPort(ConversionUtils.getLocalPart(portName));
			javax.wsdl.Port port = s.getPort(ConversionUtils.getLocalPart(portName));
			soapInputNode.setSelectedBinding(port.getBinding().getQName().getLocalPart());
			soapInputNode.setTargetNamespace(s.getQName().getNamespaceURI());
			soapInputNode.setSelectedPortType(ConversionUtils.getLocalPart(converterContext.portType.getPortType().toString()));
			boolean isJMS = WSDLUtils.isJMSBinding(port.getBinding());

			if (isJMS) {
				SOAPExtractNode soapExtractNode = (SOAPExtractNode) createNode(converterContext.targetFlow, nodeName
						+ ROUTE_TO_LABEL_SUFFIX, "extract", SOAPExtractNode.class, nodes);
				soapExtractNode.setRemoveEnvelope(true);
				soapExtractNode.setRouteToOperation(true);
				soapExtractNode.setPathMode(ENUM_SOAPEXTRACT_PATHMODE.Create);

				soapInputNode.setTransport(ENUM_SOAPINPUT_TRANSPORT.jms);
				createToDoTask(getMainFlowFile(converterContext),
						NLS.bind(WESBConversionMessages.todoConfigureJMSTransport, nodeName));

				converterContext.targetFlow.connect(soapInputNode.OUTPUT_TERMINAL_OUT, soapExtractNode.INPUT_TERMINAL_IN);
			} else {
				RouteToLabelNode routeToLabelNode = (RouteToLabelNode) createNode(converterContext.targetFlow, nodeName
						+ ROUTE_TO_LABEL_SUFFIX, "route", RouteToLabelNode.class, nodes); //$NON-NLS-1$

				soapInputNode.setExtractSOAPBody(true);
				soapInputNode.setTransport(ENUM_SOAPINPUT_TRANSPORT.http);
				String location = WSDLUtils.getSOAPAddressLocation(port);
				if (location != null) {
					URL url = new URL(location);
					soapInputNode.setUrlSelector(url.getPath());
				} else {
					createToDoTask(converterContext.flowManager.getFlowResource(ConversionUtils.getFullyQualifiedFlowName(null,
							converterContext.moduleConverter.getTargetProject().getName())), NLS.bind(
							WESBConversionMessages.todoConfigureHTTPTransport, nodeName));
				}

				converterContext.targetFlow.connect(soapInputNode.OUTPUT_TERMINAL_OUT, routeToLabelNode.INPUT_TERMINAL_IN);
			}

			if (converterContext.moduleConverter.getBrokerService() != null) {
				converterContext.moduleConverter.createService(soapInputNode.getSelectedPortType(), wsdlFile, port);
			}

			createToDoTaskForAdvancedProperties(converterContext.moduleConverter, converterContext.sourceBinding,
					converterContext.flowManager);
		} else {
			createToDoTask(converterContext.flowManager.getFlowResource(ConversionUtils.getFullyQualifiedFlowName(null,
					converterContext.moduleConverter.getTargetProject().getName())), NLS.bind(
					WESBConversionMessages.todoUnresolvedWSDL, qName));
		}

		return nodes;
	}

	protected void createToDoTaskForAdvancedProperties(SCAModuleConverterHelper helper, Binding sourceBinding,
			FlowResourceManager flowManager) {
	}

	abstract protected String getPortName(Binding sourceBinding);

	abstract protected String getServiceQName(Binding sourceBinding);

	@Override
	public Terminal getInputTerminal(String sourceTerminalName, Nodes nodes) {
		return ((SOAPReplyNode) nodes.getNode(ROLE_EXIT)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public Terminal getOutputTerminal(String sourceTerminalName, Nodes nodes) {
		return null;
	}

	@Override
	public String getConvertedTo() {
		return "SOAPInput"; //$NON-NLS-1$
	}
}
