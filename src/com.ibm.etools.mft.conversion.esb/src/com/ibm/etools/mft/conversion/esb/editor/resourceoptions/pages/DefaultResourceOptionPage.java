/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.etools.mft.conversion.esb.WESBConversionHelpListener;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.Browser;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;

/**
 * @author Zhongming Chen
 * 
 */
public class DefaultResourceOptionPage extends Composite implements IResourceOptionPage {

	protected Browser conversionNotes;
	protected ResourceOptionsEditor owner;

	/**
	 * @param parent
	 * @param style
	 */
	public DefaultResourceOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(parent, style);
		this.owner = owner;
		this.setLayout(new GridLayout(getGridColumnCount(), false));
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		createOptions();

		createConversionNotes();
	}

	protected void createOptions() {
	}

	protected int getGridColumnCount() {
		return 1;
	}

	protected void createConversionNotes() {
		Label l = new Label(this, SWT.None);
		l.setText(WESBConversionMessages.conversionNotes);
		GridData data = new GridData();
		data.horizontalSpan = getGridColumnCount();
		l.setLayoutData(data);

		conversionNotes = new Browser(this, SWT.BORDER | SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = getGridColumnCount();
		conversionNotes.setLayoutData(data);
		conversionNotes.addLocationListener(new WESBConversionHelpListener());
	}

	@Override
	public void setDetail(Object model, Object detail) {
		conversionNotes.setText(detail.toString());
	}

}
