/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1859886489" > 
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
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;

/**
 * @author Zhongming Chen
 *
 */
public interface IConversionResultRenderer {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public static class ConversionLogEntryData {
		public List<ConversionLogEntry> entries;
		public IResource resource;
		public boolean showAll;

		public ConversionLogEntryData(List<ConversionLogEntry> entries, IResource resource, boolean showAll) {
			this.entries = entries;
			this.resource = resource;
			this.showAll = showAll;
		}
	}

	public static class MarkersData {
		public List<IMarker> markers;

		public MarkersData(List<IMarker> entries) {
			this.markers = entries;
		}
	}

	public static class ConversionSummaryData {
		public HashMap<String, HashSet<String>> sourceToTargets;

		public ConversionSummaryData(HashMap<String, HashSet<String>> sourceToTargets) {
			this.sourceToTargets = sourceToTargets;
		}
	}

	public static class ConversionErrorData {
		public ConversionLog model;

		public ConversionErrorData(ConversionLog model) {
			this.model = model;
		}
	}

	/**
	 * Get the file extension supported by this transform.
	 * 
	 * @return file extension support
	 */
	public String getId();

	public void createControl(ConversionResultViewer generationResultEditor, Composite parent);

	public void setData(Object data);

	public void setShowAll(boolean showAll);

	public void refresh();
}
