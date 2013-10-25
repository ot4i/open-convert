/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;

/**
 * @author Zhongming Chen
 * 
 */
public class MarkerPage extends Composite {

	private Text messageText;
	private Text resourceText;
	private Text pathText;
	private Text typeText;

	public MarkerPage(PageBook owner) {
		super(owner, SWT.None);

		setLayout(new GridLayout(2, false));

		new Label(this, SWT.None).setText(WESBConversionMessages.message);
		messageText = createText();

		new Label(this, SWT.None).setText(WESBConversionMessages.resource);
		resourceText = createText();

		new Label(this, SWT.None).setText(WESBConversionMessages.path);
		pathText = createText();

		new Label(this, SWT.None).setText(WESBConversionMessages.type);
		typeText = createText();
	}

	private Text createText() {
		Text t = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return t;
	}

	public void setMarker(IMarker m) {
		messageText.setText(m.getAttribute(IMarker.MESSAGE, "")); //$NON-NLS-1$
		resourceText.setText(m.getResource().getName());
		pathText.setText(m.getResource().getFullPath().toString());
		try {
			typeText.setText(MarkerTypesModel.getInstance().getType(m.getType()).getLabel());
		} catch (CoreException e) {
			typeText.setText(""); //$NON-NLS-1$
		}
	}

}
