/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class DefaultLogEntriesRenderer extends DefaultConversionResultRenderer {

	public static final String ID = "DefaultLogEntriesRenderer"; //$NON-NLS-1$

	protected ConversionLogEntriesContentProvider contentProvider = new ConversionLogEntriesContentProvider();

	/**
	 * 
	 */
	public DefaultLogEntriesRenderer() {
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControl(ConversionResultViewer editor, Composite parent) {
		ColumnTitles = new String[] { WESBConversionMessages.defaultConversionResultRenderer_MessageColumn };
		ColumnWidths = new int[] { 1000 };
		super.createControl(editor, parent);
	}

	public void fillContextMenu(IMenuManager manager) {
		manager.removeAll();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		if (selection.size() > 1) {
			return;
		}
		if (selection.getFirstElement() instanceof TodoEntry) {
			addMenu(manager, CompleteToDo.class, selection);
			manager.add(new Separator());
			addMenu(manager, ReopenToDo.class, selection);
		}
	}

	protected void initActionRegistry() {
		actionRegistry.clear();

		actionRegistry.put(CompleteToDo.class, new CompleteToDo(this));
		actionRegistry.put(ReopenToDo.class, new ReopenToDo(this));
	}

	@Override
	public void setData(Object data) {
		if (data instanceof IConversionResultRenderer.ConversionLogEntryData) {
			IConversionResultRenderer.ConversionLogEntryData d = (ConversionLogEntryData) data;
			contentProvider.setShowAll(d.showAll);
			contentProvider.setUserLogEntries(d.entries);
			contentProvider.setResource(d.resource);
			viewer.setInput(this);
			viewer.expandAll();
			if (d.entries != null && d.entries.size() > 0) {
				viewer.setSelection(new StructuredSelection(d.entries.get(0)));
			}
		}
	}

	@Override
	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
		contentProvider.setShowAll(showAll);
		viewer.refresh();
	}

	public boolean isShowAll() {
		return showAll;
	}

	@Override
	protected IContentProvider createContentProvider() {
		return contentProvider;
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new ConversionLogEntriesLabelProvider();
	}
}
