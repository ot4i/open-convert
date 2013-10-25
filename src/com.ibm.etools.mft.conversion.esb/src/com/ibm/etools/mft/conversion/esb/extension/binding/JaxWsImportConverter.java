/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.jaxws.JaxWsImportBinding;
import com.ibm.wsspi.sca.scdl.jaxws.impl.JaxWsImportBindingImpl;

/**
 * @author Zhongming Chen
 * 
 */
public class JaxWsImportConverter extends WSImportConverter {

	/**
	 * 
	 */
	public JaxWsImportConverter() {
	}

	@Override
	public String getType() {
		return JaxWsImportBindingImpl.class.getName();
	}

	@Override
	protected String getEndPoint(Binding sourceBinding) {
		return ((JaxWsImportBinding) sourceBinding).getEndpoint();
	}

	@Override
	protected String getPortName(Binding sourceBinding) {
		return ((JaxWsImportBinding) sourceBinding).getPort().toString();
	}

	@Override
	protected String getServiceQName(Binding binding) {
		return ((JaxWsImportBinding) binding).getService().toString();
	}

	@Override
	public String getDisplayName() {
		return "Jax/Ws Import"; //$NON-NLS-1$
	}
}
