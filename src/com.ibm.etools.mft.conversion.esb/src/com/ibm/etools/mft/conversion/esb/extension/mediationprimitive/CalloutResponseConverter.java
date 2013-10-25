/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.wsspi.sca.scdl.Binding;

/**
 * @author Zhongming Chen
 * 
 */
public class CalloutResponseConverter extends AbstractMediationPrimitiveConverter {

	/**
	 * 
	 */
	public CalloutResponseConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "CalloutResponse"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node n : converterContext.moduleConverter.getPrimitiveToNodes().keySet()) {
			if ("Callout".equals(n.getType())) { //$NON-NLS-1$
				if ((n.getName() + "Response").equals(converterContext.sourcePrimitive.getName())) { //$NON-NLS-1$
					Nodes nodes = converterContext.moduleConverter.getPrimitiveToNodes().get(n);
					if (nodes != null) {
						converterContext.moduleConverter.getPrimitiveToNodes().put(converterContext.sourcePrimitive, nodes);
					}
					return nodes;
				}
			}
		}
		return null;
	}

	@Override
	public Terminal getInputTerminal(String sourceName, Nodes nodes) {
		return null;
	}

	@Override
	public Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		Nodes bindingNodes = (Nodes) nodes.getProperties().get("bindingNodes"); //$NON-NLS-1$
		Binding binding = (Binding) nodes.getProperties().get("binding"); //$NON-NLS-1$
		if (bindingNodes != null) {
			IBindingConverter converter;
			try {
				converter = BindingManager.getConverter(binding.getClass().getName(), conversionContext, conversionContext.model);
				return converter.getOutputTerminal(sourceName, bindingNodes); //$NON-NLS-1$
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getConvertedTo() {
		return WESBConversionMessages.CalloutResponseConverter_convertedTo;
	}
}
