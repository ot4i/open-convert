/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.webservice.WebServiceExportBinding;
import com.ibm.wsspi.sca.scdl.webservice.impl.WebServiceExportBindingImpl;

/**
 * @author Zhongming Chen
 * 
 */
public class WebServiceExportConverter extends WSExportConverter {

	/**
	 * 
	 */
	public WebServiceExportConverter() {
	}

	@Override
	public String getType() {
		return WebServiceExportBindingImpl.class.getName();
	}

	protected String getPortName(Binding sourceBinding) {
		WebServiceExportBinding binding = (WebServiceExportBinding) sourceBinding;
		return binding.getPort().toString();
	}

	protected String getServiceQName(Binding sourceBinding) {
		WebServiceExportBinding binding = (WebServiceExportBinding) sourceBinding;
		return binding.getService().toString();
	}

	@Override
	public String getDisplayName() {
		return "WebService Export"; //$NON-NLS-1$
	}

}
