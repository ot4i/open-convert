/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.extension.resource.MapConverterHelper;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBMapsOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage, SelectionListener,
		ISelectionChangedListener, ICheckStateListener, IDoubleClickListener {

	private static final String[] COLUMN_HEADINGS = new String[] { WESBConversionMessages.WESBMapsOptionPage_map,
			WESBConversionMessages.WESBMapsOptionPage_usage };
	private static final int[] WIDTHES = new int[] { 300, 300 };

	private Button selectAll;

	private Button deselectAll;

	private CheckboxTableViewer viewer;
	private WESBMaps maps;

	public class TLP extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			WESBMap map = (WESBMap) element;
			switch (columnIndex) {
			case 0:
				IPath p = new Path(map.getName());
				if (p.segmentCount() > 1) {
					return p.lastSegment() + " - " + map.getName(); //$NON-NLS-1$
				} else {
					return p.lastSegment();
				}
			case 1:
				return ConversionUtils.getUsage(map.getUsages());
			}
			return null;
		}

	}

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public WESBMapsOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	protected void createOptions() {

		Label l = new Label(this, SWT.WRAP);
		l.setText(WESBConversionMessages.WESBMapsOptionPage_mapUsageDescription);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		viewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setLabelProvider(new TLP());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.addDoubleClickListener(this);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.heightHint = 200;
		viewer.getControl().setLayoutData(data);

		for (int i = 0; i < COLUMN_HEADINGS.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.None);
			column.setText(COLUMN_HEADINGS[i]);
			column.setResizable(true);
			column.setWidth(WIDTHES[i]);
		}
		viewer.addCheckStateListener(this);

		selectAll = new Button(this, SWT.NONE);
		selectAll.setText(WESBConversionMessages.projectSelectionPage_SelectAll);
		data = new GridData();
		data.horizontalAlignment = SWT.BEGINNING;
		selectAll.setLayoutData(data);
		selectAll.addSelectionListener(this);

		deselectAll = new Button(this, SWT.NONE);
		deselectAll.setText(WESBConversionMessages.projectSelectionPage_DeselectAll);
		data = new GridData();
		data.horizontalAlignment = SWT.BEGINNING;
		deselectAll.setLayoutData(data);
		deselectAll.addSelectionListener(this);
	}

	@Override
	public void setDetail(Object model, Object detail) {
		super.setDetail(model, detail);
		this.maps = (WESBMaps) detail;

		Collections.sort(maps.getAllMaps(), new Comparator<WESBMap>() {
			@Override
			public int compare(WESBMap m1, WESBMap m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});
		viewer.setInput(maps.getAllMaps());
		viewer.removeCheckStateListener(this);
		for (WESBMap m : MapConverterHelper.getMapsToConvert(maps)) {
			if (m.isTobeConverted()) {
				viewer.setChecked(m, true);
			}
		}
		viewer.addCheckStateListener(this);

		conversionNotes.setText(WESBConversionMessages.WESBMapsOptionPage_previewMessageMaps);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == selectAll) {
			checkAll(true);
		} else if (e.getSource() == deselectAll) {
			checkAll(false);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		if (event.getSource() == viewer) {
			WESBMap m = (WESBMap) event.getElement();
			if (!event.getChecked()) {
				if (!ConversionUtils.canMapBeUnchecked(m, maps)) {
					// can not uncheck.
					try {
						viewer.removeCheckStateListener(this);
						viewer.setChecked(m, true);
						viewer.refresh(m);
						MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								WESBConversionMessages.WESBMapsOptionPage_WarningTitle,
								WESBConversionMessages.WESBMapsOptionPage_WarningUsedMapCannotBeDeSelected);
						return;
					} finally {
						viewer.addCheckStateListener(this);
					}
				}
			} else {
				List<WESBMap> checked = ConversionUtils.ensureReferencedMapAreChecked(m, maps);
				if (checked.size() > 0) {
					try {
						viewer.removeCheckStateListener(this);
						for (WESBMap m1 : checked) {
							viewer.setChecked(m1, true);
						}
					} finally {
						viewer.addCheckStateListener(this);
					}
				}
			}
			updateModel();
		}
	}

	protected void updateModel() {
		HashSet<WESBMap> checked = new HashSet<WESBMap>();
		for (Object o : viewer.getCheckedElements()) {
			WESBMap m = (WESBMap) o;
			checked.add(m);
		}
		for (WESBMap m : maps.getAllMaps()) {
			m.setTobeConverted(checked.contains(m));
		}
		this.owner.refreshTreeViewer(maps);
		this.owner.changed();
	}

	private void checkAll(boolean state) {
		if (state) {
			viewer.setAllChecked(state);
			updateModel();
		} else {
			boolean containsMapCanNotBeDeselected = false;
			for (WESBMap m : maps.getAllMaps()) {
				if (m.getUsages().size() > 0) {
					containsMapCanNotBeDeselected = true;
				} else {
					viewer.setChecked(m, false);
				}
			}
			if (containsMapCanNotBeDeselected) {
				MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						WESBConversionMessages.WESBMapsOptionPage_WarningTitle,
						WESBConversionMessages.WESBMapsOptionPage_WarningUsedMapCannotBeDeSelected);
			}
			updateModel();
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSource() == viewer) {
			IStructuredSelection s = (IStructuredSelection) event.getSelection();
			if (s.isEmpty()) {
				return;
			}

			if (s.getFirstElement() instanceof WESBMap) {
				WESBMap map = (WESBMap) s.getFirstElement();
				String message = null;
				String desc = WESBConversionMessages.WESBMapsOptionPage_MapsUsageDialogDescription;
				String title = WESBConversionMessages.WESBMapsOptionPage_MapUsageDialog_Title;
				String heading = WESBConversionMessages.WESBMapsOptionPage_MapUsageDialog_Desc;
				if (map.getUsages().size() == 0) {
					message = WESBConversionMessages.WESBMapsOptionPage_Message_UnreferencedMap;
				} else {
					message = WESBConversionMessages.WESBMapsOptionPage_Message_ReferencedMap;
				}
				UsageDialog d = new UsageDialog(heading, title, desc, NLS.bind(message, map.getName()), map.getUsages());
				d.open();
			}
		}
	}

	@Override
	protected int getGridColumnCount() {
		return 2;
	}
}
