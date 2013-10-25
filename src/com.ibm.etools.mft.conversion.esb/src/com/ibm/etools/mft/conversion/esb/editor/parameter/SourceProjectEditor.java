/**********************************************************************
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) Copyright IBM Corp. 2013  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise  
 * divested of its trade secrets, irrespective of what has been 
 * deposited with the U.S. Copyright Office.
 **********************************************************************/
package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.wizard.WESBProjectInterchangeImportWizard;
import com.ibm.etools.mft.navigator.DecoratingTreeLabelProvider;
import com.ibm.etools.mft.uri.udn.UDNUtils;
import com.ibm.etools.mft.wizard.editor.property.editors.CustomTextCellEditor;

/**
 * @author Zhongming Chen
 * 
 */
public class SourceProjectEditor extends WESBConversionParameterEditor implements SelectionListener, ICheckStateListener,
		IResourceChangeListener {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static final String[] COLUMN_HEADINGS = new String[] { WESBConversionMessages.SourceProjectEditor_sourceProject,
			WESBConversionMessages.SourceProjectEditor_targetProject };
	private static final String[] PROPERTIES = new String[] { "WESB Source Project", "Target IB Project" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final int[] WIDTHES = new int[] { 300, 300 };
	private CheckboxTreeViewer viewer;

	private Button selectAll;

	private Button deselectAll;

	private Button includeReferencedProjectOption;

	private HashMap<String, WESBProject> projectsInModel = new HashMap<String, WESBProject>();

	private Link importLink;

	private Composite rootComposite;

	private IProject projectBeingEditted = null;

	public class ProjectLabelProvider extends DecoratingTreeLabelProvider implements ITableLabelProvider {

		public ProjectLabelProvider() {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof IProject) {
					if (ConversionUtils.isESBLib((IProject) element)) {
						return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_LIBRAY);
					} else if (ConversionUtils.isESBModule((IProject) element)) {
						return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_MODULE);
					}
				}
				return super.getImage(element);
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return super.getText(element);
			case 1:
				WESBProject wesbp = projectsInModel.get(((IProject) element).getName());
				if (wesbp == null) {
					return ""; //$NON-NLS-1$
				} else {
					String s = wesbp.getTargetName();
					if ((s == null || s.length() == 0) && wesbp.isToConvert()) {
						wesbp.setTargetName(ConversionUtils.getDefaultTargetProjectName(wesbp.getName()));
						changed();
					}
					return wesbp.getTargetName();
				}
			default:
				break;
			}
			return null;
		}
	}

	@Override
	public void createControls(Composite parent) {
		updateProjectsInModel();

		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComposite = container;

		importLink = new Link(container, SWT.None);
		importLink.setText(WESBConversionMessages.SourceProjectEditor_importLink);
		GridData data = new GridData();
		data.verticalAlignment = SWT.BEGINNING;
		data.horizontalSpan = 2;
		importLink.setLayoutData(data);
		importLink.addSelectionListener(this);

		viewer = new CheckboxTreeViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		// data.heightHint = 200;
		viewer.getTree().setLayoutData(data);
		for (int i = 0; i < COLUMN_HEADINGS.length; i++) {
			TreeColumn column = new TreeColumn(viewer.getTree(), SWT.None);
			column.setText(COLUMN_HEADINGS[i]);
			column.setResizable(true);
			column.setWidth(WIDTHES[i]);
		}
		viewer.addCheckStateListener(this);

		viewer.setContentProvider(new WESBProjectContentProvider());
		viewer.setLabelProvider(new ProjectLabelProvider());
		CustomTextCellEditor cellEditor = new CustomTextCellEditor(viewer.getTree()) {
			@Override
			public String isValid(Object value) {
				return getErrorMessage();
			}
		};
		viewer.setCellEditors(new CellEditor[] { null, cellEditor });
		cellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value) {
				return isValidTargetName(value.toString());
			}
		});

		viewer.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
				IProject p = (IProject) ((TreeItem) element).getData();
				WESBProject wesbp = projectsInModel.get(p.getName());
				if (wesbp == null) {
					wesbp = new WESBProject();
					wesbp.setName(p.getName());
					getModel().getSourceProjects().add(wesbp);
					updateProjectsInModel();
				}
				if (value != null) {
					wesbp.setTargetName(value.toString());
					viewer.refresh(p);
					changed();
				}
			}

			@Override
			public Object getValue(Object element, String property) {
				IProject p = (IProject) element;
				WESBProject wesbp = projectsInModel.get(p.getName());
				if (wesbp == null) {
					return ""; //$NON-NLS-1$
				}
				String targetName = wesbp.getTargetName();
				return targetName == null ? "" : targetName; //$NON-NLS-1$
			}

			@Override
			public boolean canModify(Object element, String property) {
				projectBeingEditted = (IProject) element;
				return true;
			}
		});
		viewer.setInput(ResourcesPlugin.getWorkspace());
		viewer.setColumnProperties(PROPERTIES);

		FocusCellOwnerDrawHighlighter projectHighlighter = new FocusCellOwnerDrawHighlighter(viewer);

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(viewer, projectHighlighter);

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {

			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {

				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		activationSupport.setEnableEditorActivationWithKeyboard(true);

		TreeViewerEditor.create(viewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		Composite container2 = new Composite(container, SWT.None);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		container2.setLayout(layout);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalIndent = 0;
		container2.setLayoutData(data);

		selectAll = new Button(container2, SWT.NONE);
		selectAll.setText(WESBConversionMessages.projectSelectionPage_SelectAll);
		data = new GridData();
		data.horizontalAlignment = SWT.BEGINNING;
		selectAll.setLayoutData(data);
		selectAll.addSelectionListener(this);

		deselectAll = new Button(container2, SWT.NONE);
		deselectAll.setText(WESBConversionMessages.projectSelectionPage_DeselectAll);
		data = new GridData();
		data.horizontalAlignment = SWT.BEGINNING;
		deselectAll.setLayoutData(data);
		deselectAll.addSelectionListener(this);

		includeReferencedProjectOption = new Button(container, SWT.CHECK);
		includeReferencedProjectOption.setText(WESBConversionMessages.projectSelectionPage_IncludeReferencedProject);
		includeReferencedProjectOption.setSelection(getModel().getGlobalConfiguration().isIncludeReferencedProject());

		updateViewer();

		if (includeReferencedProjectOption.getSelection()) {
			ensureAllReferencedProjectsAreChecked();
			updateModel();
		}

		includeReferencedProjectOption.addSelectionListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

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
		includeReferencedProjectOption.removeSelectionListener(this);
		for (WESBProject p : getModel().getSourceProjects()) {
			if (p.isToConvert()) {
				viewer.setChecked(UDNUtils.getProject(p.getName()), true);
			}
		}
		includeReferencedProjectOption.addSelectionListener(this);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == selectAll) {
			checkAll(true);
		} else if (e.getSource() == deselectAll) {
			checkAll(false);
		} else if (e.getSource() == includeReferencedProjectOption) {
			if (includeReferencedProjectOption.getSelection()) {
				ensureAllReferencedProjectsAreChecked();
				updateModel();
			}
			getModel().getGlobalConfiguration().setIncludeReferencedProject(includeReferencedProjectOption.getSelection());
			changed();
		} else if (e.getSource() == importLink) {
			WESBProjectInterchangeImportWizard wizard = new WESBProjectInterchangeImportWizard(false);
			WizardDialog d = new WizardDialog(importLink.getShell(), wizard);
			if (d.open() == WizardDialog.OK) {
				viewer.refresh();
				for (IProject p : wizard.getProjectsToConvert()) {
					viewer.setChecked(p, true);

				}
				if (wizard.getProjectsToConvert().size() > 0) {
					updateModel();
					changed();
				}
			}
		}
	}

	private void checkAll(boolean state) {
		viewer.setAllChecked(state);
		updateModel();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	private void ensureAllReferencedProjectsAreChecked() {
		HashSet<IProject> ps = new HashSet<IProject>((Collection<? extends IProject>) Arrays.asList(viewer.getCheckedElements()));
		do {
			List<IProject> toAdd = new ArrayList<IProject>();
			for (IProject p : ps) {
				try {
					IProject[] rps = p.getDescription().getReferencedProjects();
					for (IProject rp : rps) {
						if (ps.contains(rp) || !ConversionUtils.isESBProject(rp)) {
							continue;
						}
						toAdd.add(rp);
					}
				} catch (CoreException e) {
				}
			}
			if (toAdd.size() == 0) {
				break;
			}
			ps.addAll(toAdd);
		} while (true);
		viewer.setCheckedElements(ps.toArray());
	}

	private void updateModel() {
		boolean changed = false;
		HashSet<String> checkedProjects = new HashSet<String>();
		for (Object o : viewer.getCheckedElements()) {
			IProject p = (IProject) o;
			if (!projectsInModel.containsKey(p.getName())) {
				WESBProject wesbp = new WESBProject();
				wesbp.setName(p.getName());
				wesbp.setType(ConversionUtils.getESBProjectType(p));
				wesbp.setTargetName(ConversionUtils.getDefaultTargetProjectName(p.getName()));
				getModel().getSourceProjects().add(wesbp);
				wesbp.setToConvert(true);
				changed = true;
			} else {
				WESBProject wesbp = projectsInModel.get(p.getName());
				if (!wesbp.isToConvert()) {
					wesbp.setToConvert(true);
					changed = true;
				}
				String type = ConversionUtils.getESBProjectType(p);
				if (!type.equals(wesbp.getType())) {
					wesbp.setType(type);
					changed = true;
				}
			}
			checkedProjects.add(p.getName());
		}
		for (WESBProject wesbp : getModel().getSourceProjects()) {
			if (!checkedProjects.contains(wesbp.getName()) && wesbp.isToConvert()) {
				wesbp.setToConvert(false);
				changed = true;
			}
		}
		updateProjectsInModel();
		viewer.refresh();
		if (changed) {
			changed();
		}
	}

	private void updateProjectsInModel() {
		projectsInModel.clear();
		for (WESBProject p : getModel().getSourceProjects()) {
			projectsInModel.put(p.getName(), p);
		}
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		if (event.getSource() == viewer) {
			if (getModel().getGlobalConfiguration().isIncludeReferencedProject()) {
				ensureAllReferencedProjectsAreChecked();
			}
			updateModel();
			viewer.refresh();
		}
	}

	public String isValidTargetName(String newText) {
		try {
			newText = newText.trim();
			if (newText.length() == 0) {
				if (projectBeingEditted != null && viewer.getChecked(projectBeingEditted)) {
					return WESBConversionMessages.errorEmptyConvertedProjectName;
				} else {
					return null;
				}
			}
			for (int i = 0; i < newText.length(); i++) {
				char c = newText.charAt(i);
				if (!(Character.isLetterOrDigit(c) || c == '_')) {
					return WESBConversionMessages.errorInvalidCharacterOnProjectName;
				}
			}
			ConversionUtils.getProject(newText);
			for (WESBProject esbP : getModel().getSourceProjects()) {
				if (projectBeingEditted != null && projectBeingEditted.getName().equals(esbP.getName())) {
					continue;
				}
				if ("WESB_Conversions".equals(newText)) //$NON-NLS-1$
				{
					return WESBConversionMessages.errorWESBConversionsUsed;
				}
				for (WESBProject esbP2 : getModel().getSourceProjects()) {
					if (newText.equals(esbP2.getName())) {
						return WESBConversionMessages.errorWESBProjectNameUsed;
					}
				}
				if (newText.equals(esbP.getTargetName())
						|| (esbP.getTargetName() == null && newText.equals("IIB_" + esbP.getName()))) { //$NON-NLS-1$
					return WESBConversionMessages.projectNameInUse;
				}
			}
		} catch (Throwable e) {
			return e.getLocalizedMessage();
		}
		return null;
	}

	@Override
	protected Composite getRootComposite() {
		return rootComposite;
	}

	@Override
	public void changed() {
		super.changed();
		((Controller) getPropertyEditorHelper().getController()).sourceProjectsChanged();
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (!viewer.getControl().isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}

	}

}