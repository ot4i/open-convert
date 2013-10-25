/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.ibm.bpm.common.ui.project.interchange.ArrayContentProvider;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;

/**
 * @author Zhongming Chen
 * 
 */
public class AllMarkersRenderer extends DefaultConversionResultRenderer {

	public static final String ID = "AllMarkersRenderer"; //$NON-NLS-1$

	public class MarkersContentProvider extends ArrayContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getElements(element).length > 0;
		}

	}

	/**
	 * 
	 */
	public AllMarkersRenderer() {
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

	@Override
	public void setData(Object data) {
		if (data instanceof IConversionResultRenderer.MarkersData) {
			IConversionResultRenderer.MarkersData d = (IConversionResultRenderer.MarkersData) data;
			viewer.setInput(d.markers);
			viewer.expandAll();
			if (d.markers != null && d.markers.size() > 0) {
				viewer.setSelection(new StructuredSelection(d.markers.get(0)));
			}
		}
	}

	@Override
	public void setShowAll(boolean showAll) {
	}

	@Override
	protected IContentProvider createContentProvider() {
		return new MarkersContentProvider();
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new ConversionLogEntriesLabelProvider();
	}

	@Override
	public void refresh() {

		super.refresh();
	}
}
