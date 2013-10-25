/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionUIManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ILogEntryRenderer;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class DefaultConversionResultRenderer implements IConversionResultRenderer, ISelectionChangedListener,
		IDoubleClickListener {

	protected TreeViewer viewer;
	protected PageBook details;
	protected Composite empty;
	protected HashMap<String, Control> detailPages = new HashMap<String, Control>();
	protected HashMap<String, ILogEntryRenderer> detailRenderers = new HashMap<String, ILogEntryRenderer>();
	protected String[] ColumnTitles = new String[] { WESBConversionMessages.defaultConversionResultRenderer_MessageColumn };
	protected int[] ColumnWidths = new int[] { 1000 };
	protected MenuManager menuManager;
	protected HashMap<Class, IAction> actionRegistry = new HashMap<Class, IAction>();
	protected boolean showAll = true;
	protected ConversionResultViewer owner;
	protected MarkerPage markerPage;

	/**
	 * 
	 */
	public DefaultConversionResultRenderer() {
	}

	@Override
	public void createControl(ConversionResultViewer editor, Composite parent) {
		this.owner = editor;

		initActionRegistry();
		SashForm container = new SashForm(parent, SWT.VERTICAL);
		GridData d = new GridData(GridData.FILL_BOTH);
		d.horizontalIndent = 0;
		d.verticalIndent = 0;
		container.setLayoutData(d);
		container.setLayout(new GridLayout());

		viewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		d = new GridData(GridData.FILL_BOTH);
		d.horizontalIndent = 0;
		d.verticalIndent = 0;
		viewer.getControl().setLayoutData(d);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);

		viewer.setContentProvider(createContentProvider());
		viewer.setLabelProvider(createLabelProvider());
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(this);

		for (int i = 0; i < ColumnTitles.length; i++) {
			TreeColumn tc = new TreeColumn(viewer.getTree(), SWT.None);
			tc.setText(ColumnTitles[i]);
			tc.setWidth(ColumnWidths[i]);
		}

		createContextMeunu(viewer);

		details = new PageBook(container, SWT.None);
		d = new GridData(GridData.FILL_BOTH);
		d.horizontalIndent = 0;
		d.verticalIndent = 0;
		details.setLayoutData(d);

		createDetails();

		container.setWeights(new int[] { 50, 50 });
	}

	abstract protected IBaseLabelProvider createLabelProvider();

	abstract protected IContentProvider createContentProvider();

	protected void fillContextMenu(IMenuManager manager) {
	}

	protected void addMenu(IMenuManager manager, Class clazz, IStructuredSelection selection) {
		IAction action = actionRegistry.get(clazz);
		((IActionDelegate) action).selectionChanged(action, selection);
		manager.add(action);
	}

	protected void initActionRegistry() {
	}

	protected void createContextMeunu(TreeViewer viewer) {
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

	private void createDetails() {
		empty = new Composite(details, SWT.None);

		for (String type : ConversionUIManager.getInstance().getLogEntryRenderers().keySet()) {
			try {
				ILogEntryRenderer renderer = ConversionUIManager.getInstance().getLogEntryRenderers().get(type).getClass()
						.newInstance();
				Composite c = new Composite(details, SWT.None);
				GridData d = new GridData(GridData.FILL_BOTH);
				d.horizontalIndent = 0;
				d.verticalIndent = 0;
				c.setLayoutData(d);
				GridLayout l = new GridLayout();
				l.marginHeight = 0;
				l.marginWidth = 0;
				c.setLayout(l);
				renderer.createControl(c);
				detailPages.put(renderer.getType(), c);
				detailRenderers.put(renderer.getType(), renderer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		markerPage = new MarkerPage(details);

	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		if (e.getSource() == viewer) {
			if (e.getSelection().isEmpty()) {
				details.showPage(empty);
			} else {
				Object o = ((IStructuredSelection) e.getSelection()).getFirstElement();
				if (o instanceof ConversionLogEntry) {
					ConversionLogEntry le = (ConversionLogEntry) o;
					Control page = detailPages.get(le.getType());
					if (page != null) {
						details.showPage(page);
						detailRenderers.get(le.getType()).setData(le);
					} else {
						details.showPage(empty);
					}
				} else if (o instanceof IMarker) {
					details.showPage(markerPage);
					markerPage.setMarker((IMarker) o);
				}
			}
		}
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public void markDirty() {
		owner.changed();
	}

	public void refreshViewer(TodoEntry selected) {
		if (showAll) {
			viewer.refresh(selected);
		} else {
			viewer.refresh(selected);
		}
	}

	@Override
	public void refresh() {
		viewer.refresh();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSource() == viewer) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			Object o = null;
			if (!s.isEmpty()) {
				o = s.getFirstElement();
			}
			if (o instanceof IMarker) {
				IMarker m = (IMarker) s.getFirstElement();
				if (m.getResource() instanceof IFile) {
					try {
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
								(IFile) m.getResource());
					} catch (PartInitException e) {
						// do nothing
					}
				}
			} else if (o instanceof TodoEntry) {
				TodoEntry e = ((TodoEntry) o);
				try {
					if (e.getData() != null) {
						IResource r = ConversionUtils.getResource(new Path(e.getData().toString()));
						if (r.exists() && (r instanceof IFile)) {
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) r);
						}
					} else {
						IResource r = (IResource) e.getResource();
						if (r.exists() && (r instanceof IFile)) {
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) r);
						}
					}
				} catch (Throwable ex) {
				}
			}
		}
	}

}
