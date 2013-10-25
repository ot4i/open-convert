/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2013" 
 *  crc="243189848" > 
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.PageBook;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.DefaultResourceOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.ExportResourceOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.IResourceOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.ImportResourceOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.JavaOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.MFCComponentResourceOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.SCAModuleOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.SchemaOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.WESBMapsOptionPage;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.WESBProjectOptionPage;
import com.ibm.etools.mft.conversion.esb.model.ExportResource;
import com.ibm.etools.mft.conversion.esb.model.ImportResource;
import com.ibm.etools.mft.conversion.esb.model.MFCComponentResource;
import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBJavas;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.WESBSchemas;

/**
 * @author Zhongming Chen
 * 
 */
public class ResourceOptionsEditor extends WESBConversionParameterEditor implements SelectionListener, ISelectionChangedListener {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private CheckboxTreeViewer viewer;

	private PageBook detailContainer;

	private Composite emptyPage;

	private HashMap<Object, IResourceOptionPage> detailPages = new HashMap<Object, IResourceOptionPage>();

	private TreeViewer treeViewer;

	private Composite rootComposite;

	private FilteredTree tree;

	private WESBResourceContentProvider contentProvider;

	private WESBResourceLabelProvider labelProvider;

	private PatternFilter filter = new PatternFilter();

	private Object currentSelectedTreeObject = null;

	@Override
	public void createControls(Composite parent) {
		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComposite = container;

		new Label(container, SWT.LEFT).setText(WESBConversionMessages.ResourceOptionsEditor_websphereResourcesLabel);

		SashForm s = new SashForm(container, SWT.HORIZONTAL);
		GridData d = new GridData(GridData.FILL_BOTH);
		d.horizontalSpan = 1;
		s.setLayoutData(d);

		tree = new FilteredTree(s, SWT.BORDER | SWT.SINGLE, filter, true);
		treeViewer = tree.getViewer();
		GridData data = new GridData(GridData.FILL_BOTH);
		treeViewer.getTree().setLayoutData(data);
		treeViewer.setLabelProvider(labelProvider = new WESBResourceLabelProvider(tree));
		treeViewer.setContentProvider(contentProvider = new WESBResourceContentProvider());
		treeViewer.addSelectionChangedListener(this);

		String OS = System.getProperty("os.name").toLowerCase();//$NON-NLS-1$

		if (OS.equals("linux")) {//$NON-NLS-1$

			treeViewer.getTree().addListener(SWT.KeyDown, new Listener() {

				@Override
				public void handleEvent(Event e) {

					if (e.keyCode == 32 || e.keyCode == 13) {// SPACE or ENTER
																// key

						if (null != currentSelectedTreeObject) {

							boolean expanded = treeViewer.getExpandedState(currentSelectedTreeObject);

							if (!expanded)
								treeViewer.expandToLevel(currentSelectedTreeObject, 1);
							else
								treeViewer.collapseToLevel(currentSelectedTreeObject, 1);

						}// if
					}

				}
			});

		}// if

		detailContainer = new PageBook(s, SWT.BORDER);
		detailContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDetailsPage(detailContainer);
		// detailContainer.showPage(emptyPage);

		treeViewer.setInput(getAllProjects());

		s.setWeights(new int[] { 20, 50 });
	}

	private Object getAllProjects() {
		List<WESBProject> list = ConversionUtils.getAllWESBProjectsToConvert(getModel().getSourceProjects());
		Collections.sort(list, new Comparator<WESBProject>() {
			public int compare(WESBProject object1, WESBProject object2) {
				IProject p1 = ConversionUtils.getProject(object1.getName());
				IProject p2 = ConversionUtils.getProject(object2.getName());
				if (ConversionUtils.isESBModule(p1) && !ConversionUtils.isESBModule(p2) || ConversionUtils.isESBLib(p1)
						&& !ConversionUtils.isESBLib(p2)) {
					return ConversionUtils.isESBLib(p1) ? -1 : 1;
				}
				return p1.getName().compareTo(p2.getName());
			}
		});
		return list;
	}

	protected void createDetailsPage(PageBook detailContainer) {
		emptyPage = new Composite(detailContainer, SWT.None);

		DefaultResourceOptionPage stringDetailPage = new DefaultResourceOptionPage(this, detailContainer, SWT.None);
		detailPages.put(String.class, (IResourceOptionPage) stringDetailPage);
		detailPages.put(WESBProject.class, new WESBProjectOptionPage(this, detailContainer, SWT.None));
		detailPages.put(WESBMaps.class, new WESBMapsOptionPage(this, detailContainer, SWT.None));
		detailPages.put(SCAModule.class, new SCAModuleOptionPage(this, detailContainer, SWT.None));
		detailPages.put(ExportResource.class, new ExportResourceOptionPage(this, detailContainer, SWT.None));
		detailPages.put(ImportResource.class, new ImportResourceOptionPage(this, detailContainer, SWT.None));
		detailPages.put(MFCComponentResource.class, new MFCComponentResourceOptionPage(this, detailContainer, SWT.None));
		detailPages.put(WESBJavas.class, new JavaOptionPage(this, detailContainer, SWT.None));
		detailPages.put(WESBSchemas.class, new SchemaOptionPage(this, detailContainer, SWT.None));
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
	}

	private void checkAll(boolean state) {
		viewer.setAllChecked(state);
		updateModel();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	private void updateModel() {
	}

	public String isValidTargetName(String newText) {
		return null;
	}

	public String warningInTargetName(String newText) {
		try {
			newText = newText.trim();
			IProject p = ConversionUtils.getProject(newText);
			if (p.exists()) {
				return NLS.bind(WESBConversionMessages.projectExists, newText);
			}
		} catch (Throwable e) {
			return e.getLocalizedMessage();
		}
		return null;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		if (e.getSource() == treeViewer) {
			if (e.getSelection().isEmpty()) {
				currentSelectedTreeObject = null;
				detailContainer.showPage(emptyPage);
			} else {
				Object n = ((IStructuredSelection) e.getSelection()).getFirstElement();
				currentSelectedTreeObject = n;
				IResourceOptionPage page = detailPages.get(n.getClass());
				if (page == null) {
					page = (IResourceOptionPage) emptyPage;
				}
				detailContainer.showPage((Control) page);
				page.setDetail(getModel(), n);
			}
		}
	}

	@Override
	protected Composite getRootComposite() {
		return rootComposite;
	}

	@Override
	public void show() {
		super.show();

		if (!getController().getResourceConfigurationStep().isComplete()) {
			treeViewer.setInput(getAllProjects());
			treeViewer.expandAll();
		}

		if (treeViewer.getSelection().isEmpty()) {
			List<Object> os = (List<Object>) treeViewer.getInput();
			if (os != null && os.size() > 0) {
				treeViewer.setSelection(new StructuredSelection(os.get(0)));
			}
		} else {
			treeViewer.setSelection(new StructuredSelection(((IStructuredSelection) treeViewer.getSelection()).getFirstElement()));
		}

		((Controller) getPropertyEditorHelper().getController()).getResourceConfigurationStep().setCompleted(true);
		((Controller) getPropertyEditorHelper().getController()).refreshNavigationBar();
	}

	public void refreshTreeViewer(WESBMaps maps) {
		treeViewer.refresh(maps);
	}

	@Override
	public void changed() {
		super.changed();
		((Controller) getPropertyEditorHelper().getController()).resourceOptionsChanged();
	}
}
