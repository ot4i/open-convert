/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.Browser;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionErrorRenderer implements IConversionResultRenderer {

	public static final String ID = "ConversionErrorRenderer"; //$NON-NLS-1$

	public Browser contentViewer;

	/**
	 * 
	 */
	public ConversionErrorRenderer() {
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControl(ConversionResultViewer editor, Composite parent) {
		contentViewer = new Browser(parent, SWT.BORDER);
		contentViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentViewer.setFont(parent.getFont());
	}

	@Override
	public void setData(Object data) {
		if (data instanceof IConversionResultRenderer.ConversionErrorData) {
			ConversionErrorData d = (ConversionErrorData) data;
			String indexTemplate = ConversionUtils.loadTemplate("internal/conversionError.html"); //$NON-NLS-1$

			StringBuffer sb = new StringBuffer();
			for (ConversionLogEntry e : d.model.getErrors()) {
				sb.append(ConversionUtils.convertLRToHTML(e.getMessage()) + "\n"); //$NON-NLS-1$
			}
			indexTemplate = NLS.bind(indexTemplate, new Object[] { WESBConversionMessages.errorDuringConversion, sb.toString() });
			contentViewer.setText(indexTemplate);
		}
	}

	@Override
	public void setShowAll(boolean showAll) {
	}

	@Override
	public void refresh() {
	}

}
