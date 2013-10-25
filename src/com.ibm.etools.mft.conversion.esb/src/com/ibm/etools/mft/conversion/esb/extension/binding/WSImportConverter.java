/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.wsdl.Service;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.SOAPExtractNode;
import com.ibm.broker.config.appdev.nodes.SOAPRequestNode;
import com.ibm.broker.config.appdev.nodes.SOAPRequestNode.ENUM_SOAPREQUEST_TRANSPORT;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WSDLUtils;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.Part;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class WSImportConverter extends AbstractBindingConverter {

	final private String SOAP_EXTRACT_ID = "Extract";
	final private String SE_NAME_POSTFIX = "_SOAPExtract";

	/**
	 * 
	 */
	public WSImportConverter() {
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		String nodeName = getProposedIIBNodeNameFromBinding(converterContext);
		Nodes nodes = createNodes(converterContext);

		SOAPRequestNode requestNode = (SOAPRequestNode) createNode(converterContext.targetFlow, nodeName, ROLE_MAIN,
				SOAPRequestNode.class, nodes);
		// Add SOAP Extract node and connect it
		SOAPExtractNode extractNode = (SOAPExtractNode) createNode(converterContext.targetFlow, nodeName + SE_NAME_POSTFIX,
				SOAP_EXTRACT_ID, SOAPExtractNode.class, nodes);
		converterContext.targetFlow.connect(requestNode.OUTPUT_TERMINAL_OUT, extractNode.INPUT_TERMINAL_IN);

		String serviceQName = getServiceQName(converterContext.sourceBinding);
		String portName = getPortName(converterContext.sourceBinding);
		IFile wsdlFile = conversionContext.indexer.wsdlServicesToFile.get(serviceQName);
		if (wsdlFile != null) {
			requestNode.setWsdlFileName(wsdlFile.getProjectRelativePath().toString());
			Service s = conversionContext.indexer.wsdlServices.get(serviceQName);
			requestNode.setSelectedPort(ConversionUtils.getLocalPart(portName));
			javax.wsdl.Port port = s.getPort(ConversionUtils.getLocalPart(portName));
			requestNode.setSelectedBinding(port.getBinding().getQName().getLocalPart());
			requestNode.setTargetNamespace(s.getQName().getNamespaceURI());
			requestNode
					.setSelectedPortType(ConversionUtils.getLocalPart(port.getBinding().getPortType().getQName().getLocalPart()));
			requestNode.setSelectedOperation(converterContext.operationName);

			boolean isJMS = WSDLUtils.isJMSBinding(port.getBinding());

			if (isJMS) {
				requestNode.setTransport(ENUM_SOAPREQUEST_TRANSPORT.jms);
				requestNode.setJmsDestination(getEndPoint(converterContext.sourceBinding));
				createToDoTask(
						converterContext.flowFile,
						NLS.bind(WESBConversionMessages.todoJMSEndpointInSOAPRequestNode, new Object[] {
								((Part) converterContext.sourceBinding.eContainer()).getDisplayName(),
								getEndPoint(converterContext.sourceBinding), nodeName }));
			} else {
				requestNode.setTransport(ENUM_SOAPREQUEST_TRANSPORT.http);
				requestNode.setWebServiceURL(getEndPoint(converterContext.sourceBinding));
				createToDoTask(
						converterContext.flowFile,
						NLS.bind(WESBConversionMessages.todoHTTPEndpointInSOAPRequestNode, new Object[] {
								((Part) converterContext.sourceBinding.eContainer()).getDisplayName(),
								getEndPoint(converterContext.sourceBinding), nodeName }));
			}

		} else if (serviceQName != null && !serviceQName.equals("")) { //$NON-NLS-1$
			createToDoTask(converterContext.flowManager.getFlowResource(ConversionUtils
					.getFullyQualifiedFlowName(converterContext.targetFlow)), NLS.bind(WESBConversionMessages.todoUnresolvedWSDL,
					serviceQName));
		}

		return nodes;
	}

	abstract protected String getEndPoint(Binding sourceBinding);

	abstract protected String getPortName(Binding sourceBinding);

	abstract protected String getServiceQName(Binding binding);

	@Override
	public Terminal getInputTerminal(String sourceTerminalName, Nodes nodes) {
		return ((SOAPRequestNode) nodes.getNode(ROLE_MAIN)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public Terminal getOutputTerminal(String sourceTerminalName, Nodes nodes) {
		if ("Out".equals(sourceTerminalName)) { //$NON-NLS-1$
			return ((SOAPExtractNode) nodes.getNode(SOAP_EXTRACT_ID)).OUTPUT_TERMINAL_OUT;
		} else if ("Failure".equals(sourceTerminalName)) { //$NON-NLS-1$
			return ((SOAPRequestNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_FAILURE;
		} else if ("Fault".equals(sourceTerminalName)) { //$NON-NLS-1$
			return ((SOAPRequestNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_FAULT;
		}
		return null;
	}

	@Override
	public String getConvertedTo() {
		return "SOAPRequest"; //$NON-NLS-1$
	}
}
