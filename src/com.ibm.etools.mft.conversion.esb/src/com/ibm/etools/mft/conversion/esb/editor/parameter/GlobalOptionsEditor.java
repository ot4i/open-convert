/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2013" 
 *  crc="3515566394" > 
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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.UsageDialog;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.BindingConverter;
import com.ibm.etools.mft.conversion.esb.model.ClassDefinition;
import com.ibm.etools.mft.conversion.esb.model.Converter;
import com.ibm.etools.mft.conversion.esb.model.PrimitiveConverter;
import com.ibm.etools.msg.wsdl.ui.internal.properties.LabelProvider;

/**
 * @author Zhongming Chen
 * 
 * 
 */
public class GlobalOptionsEditor extends WESBConversionParameterEditor implements SelectionListener, ISelectionChangedListener,
		IDoubleClickListener {

	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static final String[] PRIMITIVE_COLUMN_HEADINGS = new String[] { WESBConversionMessages.GlobalOptionsEditor_primtive,
			WESBConversionMessages.GlobalOptionsEditor_convertTo, WESBConversionMessages.GlobalOptionsEditor_usage,
			WESBConversionMessages.GlobalOptionsEditor_converterClass };
	private static final String[] PRIMITIVE_PROPERTIES = new String[] { "Mediation Primitive", "Convert To", "Usage", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"Converter Class" }; //$NON-NLS-1$
	private static final int[] PRIMITIVE_WIDTHES = new int[] { 100, 200, 200, 300 };

	private static final String[] BINDING_COLUMN_HEADINGS = new String[] { WESBConversionMessages.GlobalOptionsEditor_binding,
			WESBConversionMessages.GlobalOptionsEditor_convertTo, WESBConversionMessages.GlobalOptionsEditor_usage,
			WESBConversionMessages.GlobalOptionsEditor_converterClass };
	private static final String[] BINDING_PROPERTIES = new String[] { "Binding", "Convert To", "Usage", "Converter Class" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final int[] BINDING_WIDTHES = new int[] { 100, 200, 200, 300 };

	public class PrimitiveConvertersLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			PrimitiveConverter converter = (PrimitiveConverter) element;
			IPrimitiveConverter converterInstance = null;
			try {
				converterInstance = PrimitiveManager.getConverter(converter.getType(), null, getModel());
			} catch (Throwable e) {
			}
			switch (columnIndex) {
			case 0:
				return converter.getType();
			case 1:
				if ((converterInstance instanceof DefaultMediationPrimitiveConverter) && converter.getClazz() != null) {
					return ""; //$NON-NLS-1$
				} else if (converterInstance != null) {
					return converterInstance.getConvertedTo();
				} else {
					return ""; //$NON-NLS-1$
				}
			case 2:
				return ConversionUtils.getUsage(converter.getUsages());
			case 3:
				if (converter.getClazz() != null) {
					return converter.getClazz().getClazz();
				} else {
					return PrimitiveManager.getConverterDisplayName(converterInstance.getClass().getName());
				}
			}
			return null;
		}

	}

	public class BindingConvertersLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			BindingConverter converter = (BindingConverter) element;
			IBindingConverter converterInstance = null;
			try {
				converterInstance = BindingManager.getConverter(converter.getType(), null, getModel());
			} catch (Throwable e) {
			}
			switch (columnIndex) {
			case 0:
				return BindingManager.getDisplayName(converter, converterInstance);
			case 1:
				if ((converterInstance instanceof DefaultBindingConverter) && converter.getClazz() != null) {
					return ""; //$NON-NLS-1$
				} else if (converterInstance != null) {
					return converterInstance.getConvertedTo();
				} else {
					return ""; //$NON-NLS-1$
				}
			case 2:
				return ConversionUtils.getUsage(converter.getUsages());
			case 3:
				if (converter.getClazz() != null) {
					return converter.getClazz().getClazz();
				} else {
					return PrimitiveManager.getConverterDisplayName(converterInstance.getClass().getName());
				}
			}
			return null;
		}

	}

	private Composite rootComposite;

	private TableViewer primtiveConverterViewer;

	private TableViewer bindingConverterViewer;

	private FocusCellOwnerDrawHighlighter primitiveHighlighter;
	private FocusCellOwnerDrawHighlighter bindingHighlighter;

	private Button mergeResult;

	protected Converter converterBeingEditted;

	@Override
	public void createControls(Composite parent) {
		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComposite = container;

		FormToolkit toolkit = getToolkit();
		final ScrolledForm form = toolkit.createScrolledForm(container);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		form.getBody().setLayout(layout);

		Section section = getToolkit().createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 1;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText(WESBConversionMessages.GlobalOptionsEditor_conversionResult);
		section.setDescription(WESBConversionMessages.GlobalOptionsEditor_conversionResult_desc);
		Composite sectionClient = getToolkit().createComposite(section);
		sectionClient.setLayout(new GridLayout(2, false));
		section.setClient(sectionClient);

		mergeResult = toolkit.createButton(sectionClient, WESBConversionMessages.GlobalOptionsEditor_mergeResult, SWT.CHECK);
		mergeResult.addSelectionListener(this);

		primtiveConverterViewer = createConverterSection(form, WESBConversionMessages.GlobalOptionsEditor_primitiveConverters,
				WESBConversionMessages.GlobalOptionsEditor_primitiveConverters_desc, PRIMITIVE_COLUMN_HEADINGS,
				PRIMITIVE_PROPERTIES, PRIMITIVE_WIDTHES, WESBConversionMessages.GlobalOptionsEditor_primitiveConverterClassDesc,
				new PrimitiveConvertersLabelProvider(), AbstractMediationPrimitiveConverter.class);

		bindingConverterViewer = createConverterSection(form, WESBConversionMessages.GlobalOptionsEditor_bindingConverters,
				WESBConversionMessages.GlobalOptionsEditor_bindingConverters_desc, BINDING_COLUMN_HEADINGS, BINDING_PROPERTIES,
				BINDING_WIDTHES, WESBConversionMessages.GlobalOptionsEditor_bindingConverterClassDesc,
				new BindingConvertersLabelProvider(), AbstractBindingConverter.class);

	}

	protected TableViewer createConverterSection(final ScrolledForm form, String sectionTitle, String sectionDesc,
			String[] columnHeadings, String[] columnProperties, int[] columnWidthes, final String classSelectionMessage,
			ILabelProvider labelProvider, final Class baseClass) {
		Section section = getToolkit().createSection(form.getBody(),
				Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 1;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText(sectionTitle);
		section.setDescription(sectionDesc);
		Composite sectionClient = getToolkit().createComposite(section);
		sectionClient.setLayout(new GridLayout(2, false));
		section.setClient(sectionClient);

		final TableViewer viewer = new TableViewer(sectionClient, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 130;
		viewer.getControl().setLayoutData(data);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(labelProvider);
		viewer.addDoubleClickListener(this);
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Converter c1 = (Converter) e1;
				Converter c2 = (Converter) e2;
				return c1.getType().compareTo(c2.getType());
			}
		});

		for (int i = 0; i < columnHeadings.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.None);
			column.setText(columnHeadings[i]);
			column.setResizable(true);
			column.setWidth(columnWidthes[i]);
		}
		viewer.setColumnProperties(columnProperties);

		viewer.setCellEditors(new CellEditor[] { null, null, null, new DialogCellEditor((Composite) viewer.getControl()) {
			@Override
			protected Object openDialogBox(Control cellEditorWindow) {
				Converter converter = (Converter) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				ClassSelectionDialog d = new ClassSelectionDialog(cellEditorWindow.getShell(), (ClassDefinition) getValue(),
						converter.getType(), classSelectionMessage, baseClass);
				if (d.open() == ClassSelectionDialog.OK) {
					if (d.getClazz() == null) {
						return d.getModel();
					}
					if (d.getClazz().length() == 0) {
						doSetValue(null);
						return null;
					}
					ClassDefinition def = new ClassDefinition();
					def.setResourceType(d.getResourceType());
					def.setResourcePath(d.getResourcePath());
					def.setClazz(d.getClazz());
					return def;
				}
				return d.getModel();
			}

			protected void updateContents(Object value) {
				if (value != null) {
					if (value != null && value.toString().length() == 0) {
						value = WESBConversionMessages.GlobalOptionsEditor_defaultConverter;
					} else if (value instanceof ClassDefinition) {
						value = ((ClassDefinition) value).getClazz();
					}
				} else {
					ClassDefinition oldValue = null;
					if (converterBeingEditted != null) {
						oldValue = converterBeingEditted.getClazz();
						converterBeingEditted.setClazz(null);
					}
					try {
						if (converterBeingEditted instanceof PrimitiveConverter) {
							IPrimitiveConverter ci = PrimitiveManager.getConverter(converterBeingEditted.getType(), null,
									getModel());
							String s = ci.getClass().getName();
							value = PrimitiveManager.getConverterDisplayName(s);
						} else if (converterBeingEditted instanceof BindingConverter) {
							IBindingConverter ci = BindingManager.getConverter(converterBeingEditted.getType(), null, getModel());
							String s = ci.getClass().getName();
							value = BindingManager.getConverterDisplayName(s);
						}
					} catch (Exception e) {
						value = ""; //$NON-NLS-1$
					} finally {
						if (converterBeingEditted != null) {
							converterBeingEditted.setClazz(oldValue);
						}
					}
				}
				super.updateContents(value);
			}
		} });
		viewer.setCellModifier(new ICellModifier() {

			@Override
			public void modify(Object element, String property, Object value) {
				TableItem ti = (TableItem) element;
				Converter c = (Converter) ti.getData();
				c.setClazz((ClassDefinition) value);
				viewer.refresh(c);
				changed();
			}

			@Override
			public Object getValue(Object element, String property) {
				Converter c = (Converter) element;
				converterBeingEditted = (Converter) element;
				return c.getClazz();
			}

			@Override
			public boolean canModify(Object element, String property) {
				if ("Converter Class".equals(property)) { //$NON-NLS-1$
					return true;
				}
				return false;
			}

		});

		FocusCellOwnerDrawHighlighter highlighter = new FocusCellOwnerDrawHighlighter(viewer);

		if (WESBConversionMessages.GlobalOptionsEditor_primitiveConverters.equals(sectionTitle))
			primitiveHighlighter = highlighter;
		else if (WESBConversionMessages.GlobalOptionsEditor_bindingConverters.equals(sectionTitle))
			bindingHighlighter = highlighter;

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, highlighter);

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

		TableViewerEditor.create(viewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		return viewer;
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
		if (e.getSource() == mergeResult) {
			getModel().getGlobalConfiguration().setMergeResult(mergeResult.getSelection());
			changed();
		}
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

		primtiveConverterViewer.setInput(getModel().getGlobalConfiguration().getPrimitiveConverters());
		bindingConverterViewer.setInput(getModel().getGlobalConfiguration().getBindingConverters());

		mergeResult.setSelection(getModel().getGlobalConfiguration().isMergeResult());

		((Controller) getPropertyEditorHelper().getController()).getGlobalOptionsStep().setCompleted(true);
		((Controller) getPropertyEditorHelper().getController()).refreshNavigationBar();
	}

	@Override
	public void changed() {
		super.changed();
		((Controller) getPropertyEditorHelper().getController()).globalOptionsChanged();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {

		if (event.getSelection().isEmpty()) {
			return;
		}

		Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();

		TableViewer viewer = (o instanceof PrimitiveConverter) ? primtiveConverterViewer
				: ((o instanceof BindingConverter) ? bindingConverterViewer : null);

		if (viewer != null) {

			boolean showUsage = false;
			Control c = null;

			if (viewer == primtiveConverterViewer) {
				ViewerCell vc = primitiveHighlighter.getFocusCell();
				int index = vc.getColumnIndex();

				if (index == 2)
					showUsage = true;// only show usage dialog for 3rd column
				else if (index == 3)// 4th column is being acted on, get the
									// (...) control
					c = vc.getControl();

			} else if (viewer == bindingConverterViewer) {
				ViewerCell vc = bindingHighlighter.getFocusCell();
				int index = vc.getColumnIndex();
				if (index == 2)
					showUsage = true;// only show usage dialog for 3rd column
				else if (index == 3) // 4th column is being acted on, get the
										// (...) control
					c = vc.getControl();
			}

			if (showUsage) {// usage dialog will come up if cell column 3 is
							// double-clicked or ENTER is pressed on it

				Converter converter = (Converter) o;
				String message = null;
				String desc = (o instanceof PrimitiveConverter) ? WESBConversionMessages.GlobalOptionsEditor_primitiveConvertersDescription
						: ((o instanceof BindingConverter) ? WESBConversionMessages.GlobalOptionsEditor_bindingConvertersDescription
								: ""); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
				String title = (o instanceof PrimitiveConverter) ? WESBConversionMessages.GlobalOptionsEditor_MPUsage_Title
						: ((o instanceof BindingConverter) ? WESBConversionMessages.GlobalOptionsEditor_BindingUsage_Title : ""); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
				String heading = WESBConversionMessages.GlobalOptionsEditor_UsageHeading;
				message = WESBConversionMessages.GlobalOptionsEditor_UsageMessage;
				UsageDialog d = new UsageDialog(heading, title, desc,
						NLS.bind(message,
								(o instanceof PrimitiveConverter) ? WESBConversionMessages.GlobalOptionsEditor_MP
										: ((o instanceof BindingConverter) ? WESBConversionMessages.GlobalOptionsEditor_binding
												: ""), ((ITableLabelProvider) viewer //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
										.getLabelProvider()).getColumnText(converter, 0)), converter.getUsages());
				d.open();

			}// if
			else if (null != c) {
				c.setFocus();// 4th column is being acted on, so show the (...)
								// button
			}

		}
	}

}
