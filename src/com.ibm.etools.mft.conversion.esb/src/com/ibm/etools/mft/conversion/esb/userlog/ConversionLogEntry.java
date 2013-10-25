/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.userlog;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionLogEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5583803269525162694L;

	public static final String TYPE = "DEFAULT"; //$NON-NLS-1$

	protected String message;
	protected String type = TYPE;

	/**
	 * @
	 */
	protected Object data;

	protected transient IResource resource;

	/**
	 * @param message
	 * 
	 */
	public ConversionLogEntry(String message, Object data) {
		this.message = message;
		this.data = data;
	}

	public ConversionLogEntry(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public IResource getResource() {
		return resource;
	}

}
