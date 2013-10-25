/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3054407820" > 
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
package com.ibm.etools.mft.conversion.esb.wizard;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import com.ibm.etools.patterns.PatternProductDelegateManager;
import com.ibm.etools.patterns.PatternsPlugin;
import com.ibm.etools.patterns.PatternsUIPlugin;
import com.ibm.etools.patterns.dialog.PatternInstanceNameDialog;
import com.ibm.etools.patterns.editor.PatternGenerateEditorInput;
import com.ibm.etools.patterns.model.base.Pattern;
import com.ibm.etools.patterns.model.base.PatternUIInstance;

/**
 * @author Zhongming Chen
 *
 */
public class WESBOpenPatternInstanceEditorAction extends Action implements IWorkbenchWindowActionDelegate {
	private IWorkbenchPage page;
	private Pattern pattern;
	private DropTargetEvent dropTargetEvent;
	private ISelection selectedResource = null;
	private boolean actionCanceled = false;

	@Override
	public void run() {

		String patternInstanceName = null;

		try {
			Class dialogClass;
			if (pattern != null) {
				String productId = pattern.getProductId();
				if (productId != null && productId.trim().length() > 0) {
					dialogClass = PatternProductDelegateManager.getInstance().getPatternInstanceNameDialogClass(productId);
					Class[] types = new Class[] { Shell.class };
					Constructor cons = dialogClass.getConstructor(types);
					Object[] args = new Object[] { Display.getCurrent().getActiveShell() };
					PatternInstanceNameDialog dialog = (PatternInstanceNameDialog) cons.newInstance(args);
					dialog.open();
					if (Dialog.OK == dialog.getReturnCode()) {

						patternInstanceName = dialog.getPatternInstanceName();
						PatternUIInstance patternInstance = new PatternUIInstance();
						patternInstance.setPattern(pattern);
						patternInstance.setPatternInstanceName(patternInstanceName);
						patternInstance.setDropTargetEvent(dropTargetEvent);
						patternInstance.setSelectedResource(selectedResource);

						// Open the pattern instance editor..!
						page.openEditor(new PatternGenerateEditorInput(patternInstance), PatternsPlugin.CONFIG_EDITOR_ID);
					} else {
						actionCanceled = true;
					}
				}
			}

		} catch (PartInitException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (IllegalAccessException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (InstantiationException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (SecurityException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (NoSuchMethodException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (IllegalArgumentException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		} catch (InvocationTargetException e) {
			PatternsUIPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
		}
	}

	public boolean isActionCanceled() {
		return actionCanceled;
	}

	public void setDropTargetEvent(DropTargetEvent dropTargetEvent) {
		this.dropTargetEvent = dropTargetEvent;
	}

	public DropTargetEvent getDropTargetEvent() {
		return this.dropTargetEvent;
	}

	public ISelection getSelectedResource() {
		return selectedResource;
	}

	public void setSelectedResource(ISelection selectedResource) {
		this.selectedResource = selectedResource;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public void setPage(IWorkbenchPage page) {
		this.page = page;
	}

	public void init(IWorkbenchWindow window) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void run(IAction action) {
	}
}
