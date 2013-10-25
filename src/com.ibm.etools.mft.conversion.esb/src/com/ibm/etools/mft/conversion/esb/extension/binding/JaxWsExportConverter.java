/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import org.eclipse.osgi.util.NLS;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.jaxws.JaxWsExportBinding;
import com.ibm.wsspi.sca.scdl.jaxws.impl.JaxWsExportBindingImpl;

/**
 * @author Zhongming Chen
 * 
 */
public class JaxWsExportConverter extends WSExportConverter {

	/**
	 * 
	 */
	public JaxWsExportConverter() {
	}

	@Override
	public String getType() {
		return JaxWsExportBindingImpl.class.getName();
	}

	protected String getPortName(Binding sourceBinding) {
		JaxWsExportBinding binding = (JaxWsExportBinding) sourceBinding;
		return binding.getPort().toString();
	}

	protected String getServiceQName(Binding sourceBinding) {
		JaxWsExportBinding binding = (JaxWsExportBinding) sourceBinding;
		return binding.getService().toString();
	}

	@Override
	public String getDisplayName() {
		return "Jax/Ws Export"; //$NON-NLS-1$
	}

	@Override
	protected void createToDoTaskForAdvancedProperties(SCAModuleConverterHelper helper, Binding sourceBinding,
			FlowResourceManager flowManager) {
		JaxWsExportBinding binding = (JaxWsExportBinding) sourceBinding;
		StringBuffer sb = new StringBuffer();
		if (binding.getPolicySetRef() != null) {
			sb.append("<li>"); //$NON-NLS-1$
			sb.append(NLS.bind(WESBConversionMessages.advancedPropertyPolicySet, binding.getPolicySetRef().getPolicySetName()));
			sb.append("</li>"); //$NON-NLS-1$
		}
		if (binding.getDataHandlerType() != null) {
			sb.append("<li>"); //$NON-NLS-1$
			sb.append(NLS.bind(WESBConversionMessages.advancedPropertyDataHandler, binding.getDataHandlerType()));
			sb.append("</li>"); //$NON-NLS-1$
		}
		if (binding.getSelectorType() != null) {
			sb.append("<li>"); //$NON-NLS-1$
			sb.append(NLS.bind(WESBConversionMessages.advancedPropertyFunctionSelector, binding.getSelectorType()));
			sb.append("</li>"); //$NON-NLS-1$
		}
		if (binding.getHandlerChain() != null && binding.getHandlerChain().getHandler().size() > 0) {
			sb.append("<li>"); //$NON-NLS-1$
			sb.append(WESBConversionMessages.advancedPropertyJAXWSHandler);
			sb.append("</li>"); //$NON-NLS-1$
		}

		if (sb.length() > 0) {
			createToDoTask(flowManager.getFlowResource(ConversionUtils.getFullyQualifiedFlowName(null, helper.getTargetProject()
					.getName())), NLS.bind(WESBConversionMessages.todoAdvancedPropertiesOnSOAPBinding, sb.toString()));
		}
	}
}
