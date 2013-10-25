/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1732551029" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2010, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb;

import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.PlatformUI;

/**
 * The class will listen to the HTML link click event in the conversion session
 * editor and launch Eclipse help if applicable.
 * 
 * @author Zhongming Chen
 * 
 */
public class WESBConversionHelpListener implements LocationListener {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static final String ABOUT = "about:"; //$NON-NLS-1$
	private static final String BLANK = "blank"; //$NON-NLS-1$

	@Override
	public void changing(LocationEvent event) {
		String url = getUrl(event);
		if (!url.equals(BLANK)) {
			event.doit = false;
			try {
				if (isExternalLink(url)) {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} else {
					if (url.startsWith("file:")) //$NON-NLS-1$
					{
						url = url.substring(7);
					}
					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
				}
			} catch (Exception e) {
			}
		}
	}

	private boolean isExternalLink(String url) throws URISyntaxException {
		return new java.net.URI(url).getHost() != null;
	}

	private String getUrl(LocationEvent event) {
		if (event.location.startsWith(ABOUT)) {
			return event.location.substring(ABOUT.length());
		}
		return event.location;
	}

	@Override
	public void changed(LocationEvent event) {
		// Do nothing
	}
}
