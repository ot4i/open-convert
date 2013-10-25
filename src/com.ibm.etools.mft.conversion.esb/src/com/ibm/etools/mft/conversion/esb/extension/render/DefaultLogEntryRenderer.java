/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionHelpListener;
import com.ibm.etools.mft.conversion.esb.editor.Browser;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ILogEntryRenderer;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class DefaultLogEntryRenderer implements ILogEntryRenderer {

	private Browser text;

	/**
	 * 
	 */
	public DefaultLogEntryRenderer() {
	}

	@Override
	public String getType() {
		return ConversionLogEntry.TYPE;
	}

	@Override
	public void createControl(Composite parent) {
		text = new Browser(parent, SWT.BORDER | SWT.READ_ONLY);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.addLocationListener(new WESBConversionHelpListener());
	}

	@Override
	public void setData(ConversionLogEntry entry) {
		text.setText(entry.getMessage());
	}

}
