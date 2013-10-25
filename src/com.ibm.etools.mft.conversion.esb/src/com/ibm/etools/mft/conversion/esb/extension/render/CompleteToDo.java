/* 
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2002,2012" 
 *  crc="2155812925" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2002, 2012 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 */

package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * 
 * @author Zhongming Chen
 */
public class CompleteToDo extends Action implements IActionDelegate {

	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2002, 2012 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	private TodoEntry selected;
	private DefaultLogEntriesRenderer renderer;

	public CompleteToDo(DefaultLogEntriesRenderer renderer) {
		super();
		this.renderer = renderer;
		setText(WESBConversionMessages.defaultConversionResultRenderer_CompleteToDo);
		setImageDescriptor(WESBConversionImages.getImageDescriptor(WESBConversionImages.IMAGE_COMPLETE_TODO));
	}

	@Override
	public void run(IAction action) {
		action.run();
	}

	@Override
	public void run() {
		selected.setCompleted(true);
		renderer.refreshViewer(selected);
		renderer.markDirty();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection s = (IStructuredSelection) selection;
		if (!s.isEmpty() && s.size() == 1 && (s.getFirstElement() instanceof TodoEntry)) {
			selected = (TodoEntry) s.getFirstElement();
			action.setEnabled(!selected.isCompleted());
		} else {
			action.setEnabled(false);
		}
	}
}
