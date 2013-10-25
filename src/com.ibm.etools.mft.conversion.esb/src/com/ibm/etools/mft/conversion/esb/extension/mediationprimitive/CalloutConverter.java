/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper.NodeObject;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.Import;
import com.ibm.wsspi.sca.scdl.Reference;
import com.ibm.wsspi.sca.scdl.Wire;

/**
 * @author Zhongming Chen
 * 
 */
public class CalloutConverter extends AbstractMediationPrimitiveConverter {

	/**
	 * 
	 */
	public CalloutConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "Callout"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Property referenceName = (Property) getPropertyOfSourcePrimitive(converterContext, "referenceName"); //$NON-NLS-1$
		Property opName = (Property) getPropertyOfSourcePrimitive(converterContext, "operationName"); //$NON-NLS-1$
		Reference referenceFromComponent = null;
		for (Object o : converterContext.component.getReferences()) {
			if (o instanceof Reference) {
				if (((Reference) o).getName().equals(referenceName.getValue())) {
					referenceFromComponent = (Reference) o;
					break;
				}
			}
		}
		if (referenceFromComponent == null || referenceFromComponent.getWires().size() <= 0) {
			return null;
		}

		if (referenceFromComponent.getWires().size() <= 0) {
			// no external reference. ignore
			return null;
		}
		Wire w = (Wire) referenceFromComponent.getWires().get(0);
		String target = w.getTargetName();
		NodeObject nodeObject = getPartInAssemblyDiagram(converterContext, target);
		if (nodeObject == null) {
			return null;
		}
		if (nodeObject.isImport()) {
			Import theImport = (Import) nodeObject.root;
			if (theImport.getBinding() != null) {
				com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter.ConverterContext bindingConverterContext = new com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter.ConverterContext();
				bindingConverterContext.moduleConverter = converterContext.moduleConverter;
				bindingConverterContext.flowFile = converterContext.flowManager.getFlowResource(converterContext.targetFlow);
				bindingConverterContext.targetFlow = converterContext.targetFlow;
				bindingConverterContext.operationName = opName.getValue();
				bindingConverterContext.flowManager = converterContext.flowManager;
				bindingConverterContext.portType = null;
				bindingConverterContext.sourceBinding = theImport.getBinding();
				Nodes nc = BindingManager.convertBinding(bindingConverterContext);
				converterContext.moduleConverter.getPrimitiveToNodes().put(converterContext.sourcePrimitive, nc);
				addSourceToTargetResource(nodeObject.owningFile,
						converterContext.flowManager.getFlowResource(converterContext.targetFlow));
				nc.getProperties().put(
						"bindingNodes", converterContext.moduleConverter.getBindingToNodes().get(theImport.getBinding())); //$NON-NLS-1$
				nc.getProperties().put("binding", theImport.getBinding()); //$NON-NLS-1$
				return nc;
			}
		}

		return null;
	}

	@Override
	public Terminal getInputTerminal(String sourceName, Nodes nodes) {
		Nodes bindingNodes = (Nodes) nodes.getProperties().get("bindingNodes"); //$NON-NLS-1$
		Binding binding = (Binding) nodes.getProperties().get("binding"); //$NON-NLS-1$
		if (bindingNodes != null) {
			IBindingConverter converter;
			try {
				converter = BindingManager.getConverter(binding.getClass().getName(), conversionContext, conversionContext.model);
				return converter.getInputTerminal(sourceName, bindingNodes); //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		return null;
	}

	@Override
	public String getConvertedTo() {
		return WESBConversionMessages.CalloutConverter_convertedTo;
	}
}
