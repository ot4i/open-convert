/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2013" 
 *  crc="3734933558" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *   
 *   
 *  (C) Copyright IBM Corp. 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 **********************************************************************/
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WESBConversionPlugin;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.msl.mapping.util.Status;

/**
 * @author Zhongming Chen
 * 
 */
public class SummaryEditor extends WESBConversionParameterEditor implements SelectionListener, ISelectionChangedListener, Observer {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private Composite rootComposite;

	private Link statusLink;

	private Link gotoSourceProjectLink;

	private Link gotoConversionResult;

	private Link gotoResourceConfigurationLink;

	private Link gotoGlobalOptionLink;

	private ConversionResultViewer viewer;

	private IStatus status;

	@Override
	public void createControls(Composite parent) {
		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComposite = container;

		viewer = new ConversionResultViewer(this);
		viewer.createControl(container, getToolkit());

		getController().setLogViewer(this);
	}

	private Link createGotoLink(ScrolledForm form, String label) {
		Link link = new Link(form.getBody(), SWT.None);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 1;
		link.setBackground(form.getBackground());
		link.setLayoutData(td);
		link.setText(label);
		link.addSelectionListener(this);
		return link;
	}

	@Override
	public void setEnable(boolean enable) {
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public String isValid() {
		return null;
	}

	@Override
	public void setCurrentValue(Object arg0) {
	}

	protected void updateViewer() {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == gotoSourceProjectLink) {

		} else if (e.getSource() == gotoConversionResult) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						FileEditorInput input = new FileEditorInput(getLogFile());
						IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(input);
						if (editor != null) {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
						}
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getLogFile());
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		} else if (e.getSource() == gotoGlobalOptionLink) {

		} else if (e.getSource() == gotoGlobalOptionLink) {

		}
	}

	protected IFile getLogFile() {
		return ConversionUtils.getLogFile(((Controller) getPropertyEditorHelper().getController()).getModelFile(), getModel());
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public String isValidTargetName(String newText) {
		return null;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
	}

	@Override
	protected Composite getRootComposite() {
		return rootComposite;
	}

	@Override
	public void show() {
		super.show();
		if (!viewer.isInputSet()) {
			viewer.refresh();
		}
	}

	@Override
	public void dispose() {
		viewer.dispose();
		getController().setLogViewer(null);
		super.dispose();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data != null) {
			status = (IStatus) data;
			if (status.getSeverity() == Status.INFO) {
				status = null;
			}
			changed();
		} else {
			viewer.refresh();
		}
	}

	@Override
	public IStatus getStatus() {
		if (status == null && getModel().isResultOutOfSync()) {
			status = new org.eclipse.core.runtime.Status(Status.WARNING, WESBConversionPlugin.getDefault().getBundle()
					.getSymbolicName(), WESBConversionMessages.warningResultOutOfSync);
		}
		return status;
	}
}
