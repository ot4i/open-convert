/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.DebugEntry;
import com.ibm.etools.mft.conversion.esb.userlog.ErrorEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionLogEntriesLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * 
	 */
	public ConversionLogEntriesLabelProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	@Override
	public Image getColumnImage(Object e, int columnIndex) {
		switch (columnIndex) {
		case 0:
			if (e == TodoEntry.class) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_OUTSTANDING_TODO);
			} else if (e == ConversionLogEntry.class) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_USER_LOG_ENTRY);
			} else if (e instanceof TodoEntry) {
				if (((TodoEntry) e).isCompleted()) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_COMPLETE_TODO);
				} else {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_OUTSTANDING_TODO);
				}
			} else if (e instanceof ErrorEntry) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_ERROR);
			} else if (e instanceof ConversionLogEntry) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_USER_LOG_ENTRY);
			} else if (e instanceof IMarker) {
				IMarker m = (IMarker) e;
				if (m.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_ERROR);
				} else if (m.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WARNING);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		switch (columnIndex) {
		case 0:
			if (element instanceof IResource) {
				return WESBConversionMessages.workspaceProblemMarkers;
			} else if (element == TodoEntry.class) {
				return WESBConversionMessages.todoEntries;
			} else if (element == ConversionLogEntry.class) {
				return WESBConversionMessages.informationEntries;
			} else if (element == DebugEntry.class) {
				return WESBConversionMessages.debugMessages;
			} else if (element == ErrorEntry.class) {
				return WESBConversionMessages.errorEntries;
			} else if (element instanceof ConversionLogEntry) {
				return ConversionUtils.removeAllHTMLMarkersAndLRCF(((ConversionLogEntry) element).getMessage());
			} else if (element instanceof IMarker) {
				return ((IMarker) element).getAttribute("message", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return ""; //$NON-NLS-1$
	}

}
