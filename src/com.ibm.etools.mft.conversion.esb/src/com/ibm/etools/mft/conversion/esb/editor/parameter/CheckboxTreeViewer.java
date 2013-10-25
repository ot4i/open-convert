package com.ibm.etools.mft.conversion.esb.editor.parameter;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Subclassed in order to avoid the handleDoubleSelect event during a specific
 * case of keyboard navigation.
 * 
 * We added the ability to move to any cell in the table. Sometimes... if we are
 * in the 2nd column cell in the table and hit ENTER to activate the text editor
 * for project rename, an event would be sent to click the checkbox in the 1st
 * column. This is not desired as the user is not currently in the cell of the
 * first column.
 * 
 * The event that was unnecessarily being called was handleDoubleSelect().
 * 
 * I subclassed the checkbox tree viewer to override this method... in order to
 * avoid this issue.
 * 
 * 
 * 
 * @author demond
 * 
 */
public class CheckboxTreeViewer extends org.eclipse.jface.viewers.CheckboxTreeViewer {

	public CheckboxTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleDoubleSelect(SelectionEvent event) {
		// super.handleDoubleSelect(event);

		//
		// causes problems when keyboard navigating and hitting ENTER on 2nd
		// column... it calls double-click on first column.
		//

	}

}
