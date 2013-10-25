/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2011,2013" 
 *  crc="4253335319" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *   
 *   
 *  (C) Copyright IBM Corp. 2011, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 **********************************************************************/
package com.ibm.etools.mft.conversion.esb.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;

/**
 * @author Ming
 */

public class Browser extends Composite {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2011, 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	int style;
	private org.eclipse.swt.browser.Browser browser = null;
	private Text text;
	StackLayout layout;
	private boolean textMode = false;

	public Browser(Composite parent, int style) {
		super(parent, SWT.NONE);
		this.style = style;

		layout = new StackLayout();
		this.setLayout(layout);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		try {
			this.browser = new org.eclipse.swt.browser.Browser(this, this.style);
		} catch (SWTError e) {
			// if (e.code != SWT.ERROR_NO_HANDLES) {
			// return;
			// }
			browser = null;
		}
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		this.text = new Text(this, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		text.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.text.setLayoutData(data);

		if (browser == null) {
			this.text.setText(WESBConversionMessages.INCOMPATIBLE_BROWSER);
			layout.topControl = this.text;
		} else {
			data = new GridData(SWT.FILL, SWT.FILL, true, true);
			this.browser.setLayoutData(data);
			layout.topControl = this.browser;
		}

		this.setEnabled(true);
	}

	public void addLocationListener(LocationListener listener) {
		if (browser == null) {
			this.text.setText(WESBConversionMessages.INCOMPATIBLE_BROWSER);
			layout.topControl = this.text;
		} else {
			checkWidget();
			if (listener == null)
				SWT.error(SWT.ERROR_NULL_ARGUMENT);
			browser.addLocationListener(listener);
		}
	}

	public boolean setUrl(String url) {
		if (this.browser != null)
			return this.browser.setUrl(url);
		else {
			setTextMode(true);
			this.text.setText(WESBConversionMessages.INCOMPATIBLE_BROWSER);
		}
		return false;
	}

	public void stop() {
		if (this.browser != null)
			this.browser.stop();
	}

	public void refresh() {
		if (this.browser != null)
			browser.refresh();
	}

	/**
	 * in case you want to access the browser yourself, but please check if the
	 * return value is null
	 **/
	public org.eclipse.swt.browser.Browser getBrowser() {
		return browser;
	}

	public boolean setText(String html) {
		html = "<font face='arial, helvetica, nimbus sans l, liberation sans, freesans, sans-serif' size='2'>" + html;
		if (this.browser != null) {
			if (textMode) {
				this.text.setText(html);
				return true;
			} else
				return this.browser.setText(html);
		} else
			this.text.setText(WESBConversionMessages.INCOMPATIBLE_BROWSER);
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (this.browser != null)
			this.browser.setEnabled(enabled);
		// else
		this.text.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * text mode shows a Text widget instead of a browser widget. It is useful
	 * if the client has some plain text that it wants to show in a specific
	 * font. If the text is richly formatted, he can switch over to browser mode
	 * which uses browser fonts.
	 * 
	 * @param text
	 */

	public void setTextMode(boolean text) {
		this.textMode = text;
		if (text)
			layout.topControl = this.text;
		else
			layout.topControl = this.browser;
		this.layout();
	}

	@Override
	public void setFont(Font arg0) {
		this.text.setFont(arg0);
		super.setFont(arg0);
	}
	// TODO: add delegates for all the other browser methods
}
