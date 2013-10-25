/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.binding;

import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.webservice.WebServiceImportBinding;
import com.ibm.wsspi.sca.scdl.webservice.impl.WebServiceImportBindingImpl;

/**
 * @author Zhongming Chen
 * 
 */
public class WebServiceImportConverter extends WSImportConverter {

	/**
	 * 
	 */
	public WebServiceImportConverter() {
	}

	@Override
	public String getType() {
		return WebServiceImportBindingImpl.class.getName();
	}

	@Override
	protected String getEndPoint(Binding sourceBinding) {
		return ((WebServiceImportBinding) sourceBinding).getEndpoint();
	}

	@Override
	protected String getPortName(Binding sourceBinding) {
		String result = ""; //$NON-NLS-1$
		if (((WebServiceImportBinding) sourceBinding).getPort() != null) {
			result = ((WebServiceImportBinding) sourceBinding).getPort().toString();
		}
		return result;
	}

	@Override
	protected String getServiceQName(Binding binding) {
		String result = ""; //$NON-NLS-1$
		if (((WebServiceImportBinding) binding).getService() != null) {
			result = ((WebServiceImportBinding) binding).getService().toString();
		}
		return result;
	}

	@Override
	public String getDisplayName() {
		return "WebService Import"; //$NON-NLS-1$
	}
}
