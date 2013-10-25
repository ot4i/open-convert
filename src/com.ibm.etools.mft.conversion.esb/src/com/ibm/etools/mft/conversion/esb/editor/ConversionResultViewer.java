/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.SummaryEditor;
import com.ibm.etools.mft.conversion.esb.extension.render.AllMarkersRenderer;
import com.ibm.etools.mft.conversion.esb.extension.render.ConversionErrorRenderer;
import com.ibm.etools.mft.conversion.esb.extension.render.ConversionSummaryRenderer;
import com.ibm.etools.mft.conversion.esb.extension.render.DefaultLogEntriesRenderer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionUIManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.navigator.DecoratingTreeLabelProvider;
import com.ibm.etools.patterns.IPatternIconsConstants;
import com.ibm.etools.patterns.PatternsUIPlugin;
import com.ibm.etools.patterns.utils.ui.FormToolkit;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionResultViewer implements ISelectionChangedListener, SelectionListener, IResourceChangeListener,
		IDoubleClickListener {

	private ConversionLog model;
	private TreeViewer navigator;
	private PageBook details;
	private Composite empty;
	private MenuManager menuManager;
	private HashMap<Class, IAction> actionRegistry = new HashMap<Class, IAction>();
	private Button showAll;
	private Button showToDo;
	private HashMap<String, Control> detailPages = new HashMap<String, Control>();
	private HashMap<String, IConversionResultRenderer> detailRenderers = new HashMap<String, IConversionResultRenderer>();
	private ConversionLogContentProvider contentProvider;
	private IConversionResultRenderer currentRenderer;
	private IResource currentResource;
	private SummaryEditor owner;
	private Object currentSelectedTreeObject = null;

	/**
	 * 
	 */
	public ConversionResultViewer(SummaryEditor owner) {
		this.owner = owner;
		this.model = owner.getLog();
	}

	public void createControl(Composite container, FormToolkit toolkit) {
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		showAll = new Button(container, SWT.RADIO);
		GridData d = new GridData();
		d.horizontalAlignment = SWT.BEGINNING;
		showAll.setLayoutData(d);
		showAll.setText(WESBConversionMessages.genresEditor_ShowAll);
		showAll.addSelectionListener(this);
		showAll.setSelection(true);

		showToDo = new Button(container, SWT.RADIO);
		d = new GridData();
		d.horizontalAlignment = SWT.BEGINNING;
		showToDo.setLayoutData(d);
		showToDo.setText(WESBConversionMessages.genresEditor_ShowTodo);
		showToDo.addSelectionListener(this);

		SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
		d = new GridData(GridData.FILL_BOTH);
		d.horizontalSpan = 2;
		sashForm.setLayoutData(d);

		initActionRegistry();

		createNavigator(sashForm);
		createDetails(sashForm);

		if (navigator.getTree().getItems().length > 0) {
			navigator.setSelection(new StructuredSelection(navigator.getTree().getItems()[0].getData()));
		}
		sashForm.setWeights(new int[] { 1, 1 });
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	protected Image getFormImage() {
		return PatternsUIPlugin.getInstance().getImageRegistry().get(IPatternIconsConstants.ICON_PATTERN_SPECIFICATION_KEY);
	}

	private void createDetails(SashForm form) {
		Composite container = new Composite(form, SWT.None);
		GridLayout l = new GridLayout();
		l.marginHeight = 0;
		l.marginLeft = 0;
		container.setLayout(l);
		GridData d = new GridData(GridData.FILL_BOTH);
		d.horizontalIndent = 0;
		d.verticalIndent = 0;
		container.setLayoutData(d);

		details = new PageBook(container, SWT.None);
		d = new GridData(GridData.FILL_BOTH);
		d.horizontalIndent = 0;
		d.verticalIndent = 0;
		details.setLayoutData(d);

		empty = new Composite(details, SWT.None);

		for (String ext : ConversionUIManager.getInstance().getResultRenderers().keySet()) {
			IConversionResultRenderer renderer = ConversionUIManager.getInstance().getResultRenderers().get(ext);
			addDetailPage(renderer);
		}
		addDetailPage(new DefaultLogEntriesRenderer());
		addDetailPage(new AllMarkersRenderer());
		addDetailPage(new ConversionSummaryRenderer());
		addDetailPage(new ConversionErrorRenderer());
	}

	private void addDetailPage(IConversionResultRenderer renderer) {
		Composite c = new Composite(details, SWT.None);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout l = new GridLayout();
		l.marginHeight = 0;
		l.marginWidth = 0;
		c.setLayout(l);
		renderer.createControl(this, c);
		detailPages.put(renderer.getId(), c);
		detailRenderers.put(renderer.getId(), renderer);
	}

	private void createNavigator(SashForm container) {
		navigator = new TreeViewer(container, SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData d = new GridData(GridData.FILL_BOTH);
		d.horizontalSpan = 2;
		navigator.getControl().setLayoutData(d);
		navigator.setLabelProvider(new DecoratingTreeLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IFile) {
					String fileName = super.getText(element);
					fileName += " - " + ((IFile) element).getFullPath().toString(); //$NON-NLS-1$
					return fileName;
				} else if (element instanceof String) {
					return element.toString();
				} else if (element == TodoEntry.class) {
					return WESBConversionMessages.allToDos;
				} else if (element == IMarker.class) {
					return WESBConversionMessages.allMarkers;
				} else if (element instanceof IProject) {
					if (model.getAllTargetProjects().contains(element)) {
						return WESBConversionMessages.ConversionResultViewer_convertedTo + super.getText(element);
					} else {
						return WESBConversionMessages.ConversionResultViewer_source + super.getText(element);
					}
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element.toString().equals(WESBConversionMessages.errors)) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_ERROR);
				} else if (element == TodoEntry.class) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_OUTSTANDING_TODO);
				} else if (element == IMarker.class) {
					return WESBConversionImages.getImage(WESBConversionImages.IMAGE_ERROR);
				}
				return super.getImage(element);
			}
		});
		contentProvider = new ConversionLogContentProvider();
		navigator.setContentProvider(contentProvider);
		navigator.getTree().setLinesVisible(false);

		String OS = System.getProperty("os.name").toLowerCase();//$NON-NLS-1$

		if (OS.equals("linux")) {//$NON-NLS-1$

			navigator.getTree().addListener(SWT.KeyDown, new Listener() {

				@Override
				public void handleEvent(Event e) {

					if (e.keyCode == 32 || e.keyCode == 13) {// SPACE or ENTER
																// key

						if (null != currentSelectedTreeObject) {

							boolean expanded = navigator.getExpandedState(currentSelectedTreeObject);

							if (!expanded)
								navigator.expandToLevel(currentSelectedTreeObject, 1);
							else
								navigator.collapseToLevel(currentSelectedTreeObject, 1);

						}// if
					}

				}
			});

		}// if

		navigator.addSelectionChangedListener(this);
		navigator.expandAll();
		navigator.addDoubleClickListener(this);

		createContextMeunu(navigator);
		createDnDSupport(navigator);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		if (e.getSource() == navigator) {
			IStructuredSelection s = (IStructuredSelection) e.getSelection();
			if (!s.isEmpty()) {
				Object o = ((IStructuredSelection) e.getSelection()).getFirstElement();
				currentSelectedTreeObject = o;
				if (o instanceof IResource) {
					if ((o instanceof IProject) && ConversionUtils.isESBProject((IProject) o)) {
						details.showPage(empty);
						return;
					}
					IResource r = (IResource) o;
					currentResource = r;
					String ext = r.getFileExtension();
					Control page = detailPages.get(ext);
					if (page == null) {
						ext = DefaultLogEntriesRenderer.ID;
					}
					showPage(ext, r,
							new IConversionResultRenderer.ConversionLogEntryData(model.getLog().get(r.getFullPath().toString()), r,
									showAll.getSelection()));
				} else if (o == IMarker.class) {
					showPage(AllMarkersRenderer.ID, null, new IConversionResultRenderer.MarkersData(getAllErrorsAndWarnings()));
				} else if (o.toString().equals(WESBConversionMessages.summaryInformation)) {
					showPage(ConversionSummaryRenderer.ID, null,
							new IConversionResultRenderer.ConversionSummaryData(model.getSourceToTargetResources()));
				} else if (o.toString().equals(WESBConversionMessages.errors)) {
					showPage(DefaultLogEntriesRenderer.ID, null,
							new IConversionResultRenderer.ConversionLogEntryData(model.getErrors(), null, showAll.getSelection()));
				} else if (o.toString().equals(WESBConversionMessages.debugMessages)) {
					showPage(
							DefaultLogEntriesRenderer.ID,
							null,
							new IConversionResultRenderer.ConversionLogEntryData(model.getDebugMessages(), null, showAll
									.getSelection()));
				} else if (o.toString().equals(WESBConversionMessages.errorInformation)) {
					showPage(ConversionErrorRenderer.ID, null, new IConversionResultRenderer.ConversionErrorData(model));
				} else if (o == TodoEntry.class) {
					showPage(DefaultLogEntriesRenderer.ID, null,
							new IConversionResultRenderer.ConversionLogEntryData(model.getErrors(), null, showAll.getSelection()));
					currentRenderer.setData(new IConversionResultRenderer.ConversionLogEntryData(model.getAllTodoes(), null,
							showAll.getSelection()));
				} else {
					details.showPage(empty);
				}
			} else {
				currentSelectedTreeObject = null;
				details.showPage(empty);
			}

		}
	}

	private List<IMarker> getAllErrorsAndWarnings() {

		List<IMarker> errors = new ArrayList<IMarker>();
		List<IMarker> warnings = new ArrayList<IMarker>();
		for (IProject p : model.getAllTargetProjects()) {
			try {
				IMarker[] ms = p.findMarkers(IMarker.PROBLEM, true, -1);
				for (IMarker m : ms) {
					if (m.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
						errors.add(m);
					} else if (m.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
						warnings.add(m);
					}
				}
			} catch (CoreException e1) {
			}
		}
		errors.addAll(warnings);
		return errors;
	}

	protected void showPage(String ext, IResource r, Object data) {
		currentResource = r;
		Control page = detailPages.get(ext);
		details.showPage(page);
		currentRenderer = detailRenderers.get(ext);
		currentRenderer.setData(data);
	}

	public void fillContextMenu(IMenuManager manager) {
		manager.removeAll();
		IStructuredSelection selection = (IStructuredSelection) navigator.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		if (selection.size() > 1) {
			return;
		}
	}

	private void createContextMeunu(TreeViewer viewer) {
		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void initActionRegistry() {
		actionRegistry.clear();
	}

	private void addMenu(IMenuManager manager, Class clazz, IStructuredSelection selection) {
		IAction action = actionRegistry.get(clazz);
		((IActionDelegate) action).selectionChanged(action, selection);
		manager.add(action);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public ConversionLog getModel() {
		return model;
	}

	private void createDnDSupport(TreeViewer viewer) {
		int ops = DND.DROP_MOVE;
		// Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance()
		// };
		// viewer.addDragSupport(ops, transfers, new DragAdapter(viewer));
		// viewer.addDropSupport(ops, transfers, new DropAdapter(viewer));
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == showAll) {
			contentProvider.setShowAll(showAll.getSelection());
			if (currentRenderer != null) {
				currentRenderer.setShowAll(showAll.getSelection());
			}
			navigator.refresh();
		} else if (e.getSource() == showToDo) {
			contentProvider.setShowAll(showAll.getSelection());
			if (currentRenderer != null) {
				currentRenderer.setShowAll(showAll.getSelection());
			}
			navigator.refresh();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (currentResource != null) {
			visit(event.getDelta());
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					showToDo.setEnabled(contentProvider.hasOutstandingTasks());
				}
			});
		} else if (currentRenderer instanceof AllMarkersRenderer) {
			if (isMarkerChanged(event.getDelta())) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						currentRenderer.setData(new IConversionResultRenderer.MarkersData(getAllErrorsAndWarnings()));
					}
				});
			}
		}

	}

	private boolean isMarkerChanged(IResourceDelta delta) {
		if (delta == null || delta.getResource() == null) {
			return false;
		}
		IProject p = delta.getResource().getProject();
		if (p != null && !getModel().getAllTargetProjects().contains(p)) {
			return false;
		}
		if (delta.getMarkerDeltas().length > 0) {
			return true;
		}
		for (IResourceDelta d : delta.getAffectedChildren()) {
			if (isMarkerChanged(d)) {
				return true;
			}
		}
		return false;
	}

	private void visit(IResourceDelta delta) {
		if (delta == null || delta.getResource() == null) {
			return;
		}
		if (delta.getResource().getFullPath().equals(currentResource.getFullPath())) {
			refreshCurrentRenderer();
		} else {
			for (IResourceDelta d : delta.getAffectedChildren()) {
				visit(d);
			}
		}
	}

	private void refreshCurrentRenderer() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				currentRenderer.refresh();
			}
		});
	}

	public void changed() {
		owner.changed();
	}

	public void refresh() {
		navigator.setInput(model);
		navigator.refresh();
		if (navigator.getSelection().isEmpty()) {
			navigator.setSelection(new StructuredSelection(((ITreeContentProvider) navigator.getContentProvider())
					.getElements(navigator.getInput())[0]));
		} else {
			navigator.setSelection(new StructuredSelection(((IStructuredSelection) navigator.getSelection()).getFirstElement()));
		}
		showToDo.setEnabled(contentProvider.hasOutstandingTasks());
		showAll.setVisible(model.getErrors().size() == 0);
		showToDo.setVisible(model.getErrors().size() == 0);
	}

	public boolean isInputSet() {
		return navigator.getInput() != null;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSource() == navigator) {
			IStructuredSelection s = (IStructuredSelection) event.getSelection();
			if (s.isEmpty()) {
				return;
			}
			if (s.getFirstElement() instanceof IFile) {
				try {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							(IFile) s.getFirstElement());
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
