/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Zhongming Chen
 * 
 */
public class UsageDialog extends TitleAreaDialog {

	private String message;
	private List<String> usages;
	private String title;
	private String heading;
	private String description;

	public UsageDialog(String heading, String title, String desc, String message, List<String> usages) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		setHelpAvailable(false);
		this.message = message;
		this.heading = heading;
		this.title = title;
		this.usages = usages;
		this.description = desc;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(heading);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(title);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setFont(parent.getFont());
		// Build the separator line
		Label titleBarSeparator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.setMessage(description, IMessageProvider.NONE);
		Label l = new Label(container, SWT.WRAP);
		l.setText(message);
		GridData d = new GridData(GridData.FILL_HORIZONTAL);
		d.widthHint = 600;
		l.setLayoutData(d);

		l = new Label(container, SWT.WRAP);

		if (usages.size() > 0) {
			ListViewer viewer = new ListViewer(container, SWT.BORDER);
			d = new GridData(GridData.FILL_BOTH);
			d.heightHint = 200;
			viewer.getControl().setLayoutData(d);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new LabelProvider());
			viewer.setInput(usages);
		}

		return container;
	}

}
