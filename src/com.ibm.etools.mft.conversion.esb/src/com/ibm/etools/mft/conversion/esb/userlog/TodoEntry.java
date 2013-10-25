/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.userlog;

/**
 * @author Zhongming Chen
 * 
 */
public class TodoEntry extends ConversionLogEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5499639865608934459L;
	/**
	 * 
	 */
	private boolean isCompleted = false;

	/**
	 * 
	 */
	public TodoEntry(String message, Object data) {
		super(message, data);
	}

	public TodoEntry(String message) {
		super(message);
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

}
