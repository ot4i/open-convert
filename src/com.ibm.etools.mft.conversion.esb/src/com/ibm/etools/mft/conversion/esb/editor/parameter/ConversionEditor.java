/**********************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="" 
 *  years="2013" 
 *  crc="992508986" > 
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.ESBConversionException;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WESBConversionPlugin;
import com.ibm.etools.mft.conversion.esb.editor.controller.Controller;
import com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages.UsageDialog;
import com.ibm.etools.mft.conversion.esb.extension.resource.WESBConversionManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.BindingConverter;
import com.ibm.etools.mft.conversion.esb.model.PrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.msg.wsdl.ui.internal.properties.LabelProvider;

/**
 * @author Zhongming Chen
 * 
 * 
 */
public class ConversionEditor extends WESBConversionParameterEditor implements SelectionListener, ISelectionChangedListener,
		IDoubleClickListener {
	public static final String copyright = "Licensed Materials - Property of IBM " //$NON-NLS-1$
			+ "(C) Copyright IBM Corp. 2013  All Rights Reserved. " //$NON-NLS-1$
			+ "US Government Users Restricted Rights - Use, duplication or " //$NON-NLS-1$
			+ "disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static final String[] COLUMN_HEADINGS = new String[] { WESBConversionMessages.ConversionEditor_resource,
			WESBConversionMessages.ConversionEditor_type, WESBConversionMessages.ConversionEditor_option,
			WESBConversionMessages.ConversionEditor_value };
	private static final String[] PROPERTIES = new String[] { "WESB Resource", "Type", "Option", "Value" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final int[] WIDTHES = new int[] { 200, 100, 300, 300 };

	static final String WESBJAVA_SUFFIX = "_WESBJava"; //$NON-NLS-1$
	static final String JCN_SUFFIX = "Java"; //$NON-NLS-1$

	public class ResourceOptions {
		public String resource;
		public String type;
		public String option;
		public String value;
		public Object model = null;

		public ResourceOptions(String resource, String type, String option, String value) {
			this.resource = resource;
			this.option = option;
			this.type = type;
			this.value = value;
		}

		public ResourceOptions(String resource, String type, String option, String value, Object model) {
			this.resource = resource;
			this.option = option;
			this.type = type;
			this.value = value;
			this.model = model;
		}
	}

	public class OptionLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ResourceOptions option = (ResourceOptions) element;
			switch (columnIndex) {
			case 0:
				return option.resource;
			case 1:
				return option.type;
			case 2:
				return option.option;
			case 3:
				return option.value;
			}
			return null;
		}

	}

	private Composite rootComposite;

	private TableViewer optionViewer;

	private Button startConversion;

	@Override
	public void createControls(Composite parent) {
		Composite container = new Composite(parent, SWT.None);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComposite = container;

		FormToolkit toolkit = getToolkit();
		final ScrolledForm form = toolkit.createScrolledForm(container);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		form.getBody().setLayout(new GridLayout());

		Label label = toolkit.createLabel(form.getBody(), WESBConversionMessages.ConversionEditor_summaryOfConversionConfiguration);
		createOptionTable(form);

		toolkit.createLabel(form.getBody(), "\n  "); //$NON-NLS-1$
		startConversion = toolkit.createButton(form.getBody(), WESBConversionMessages.ConversionEditor_startConversion, SWT.None);
		startConversion.addSelectionListener(this);

	}

	protected void createOptionTable(final ScrolledForm form) {

		final TableViewer viewer = new TableViewer(form.getBody(), SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 130;
		viewer.getControl().setLayoutData(data);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new OptionLabelProvider());
		viewer.addDoubleClickListener(this);

		for (int i = 0; i < COLUMN_HEADINGS.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.None);
			column.setText(COLUMN_HEADINGS[i]);
			column.setResizable(true);
			column.setWidth(WIDTHES[i]);
		}
		viewer.setColumnProperties(PROPERTIES);

		optionViewer = viewer;
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
		if (e.getSource() == startConversion) {
			// if (!showFlashWarning()) {
			// return;
			// }

			if (!checkAllConverters()) {
				return;
			}

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			try {
				pmd.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						if (!showWarningIfNecessary()) {
							return;
						}

						WESBConversionManager manager = new WESBConversionManager(getModel(), getLog());

						manager.convert(((Controller) getPropertyEditorHelper().getController()).getModelFile(), monitor);
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								getController().getConvertStep().setCompleted(true);
								getController().getEditor().getNavigationbar().selectNextItem();
								getController().refreshLogStatus(
										new Status(Status.INFO, WESBConversionPlugin.getDefault().getBundle().getSymbolicName(),
												WESBConversionMessages.messageConversionCompleted));
								getController().refreshLogViewer();
								getController().refreshNavigationBar();
								changed();
							}
						});
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected boolean showFlashWarning() {
		final List<Object> result = new ArrayList<Object>();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						WESBConversionMessages.ConversionEditor_warningTitle, WESBConversionMessages.warningFlashingScreen)) {
					result.add(Boolean.TRUE);
				}
			}
		});
		return result.size() > 0;
	}

	protected boolean checkAllConverters() {
		for (PrimitiveConverter c : getModel().getGlobalConfiguration().getPrimitiveConverters()) {
			if (c.getClazz() != null) {
				try {
					IPrimitiveConverter converter = PrimitiveManager.getConverter(c.getType(), null, getModel());
					if (converter instanceof DefaultMediationPrimitiveConverter) {
						throw new ESBConversionException(WESBConversionMessages.errorConverterNotResolvable);
					}
					if (!c.getType().equalsIgnoreCase(converter.getType())) {
						MessageDialog.openError(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								WESBConversionMessages.errorTitle,
								NLS.bind(WESBConversionMessages.errorWrongPrimitiveConverterType, new Object[] {
										c.getClazz().getClazz(), c.getType(), converter.getType() }));
						return false;
					}
				} catch (InstantiationException e) {
					String errorMsg = NLS.bind(WESBConversionMessages.errorCannotInstantiate, c.getClazz().getClazz());
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.errorTitle,
							NLS.bind(WESBConversionMessages.errorConverterNotLoadable, c.getClazz().getClazz(), errorMsg));
					return false;
				} catch (Throwable e) {
					MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.errorTitle,
							NLS.bind(WESBConversionMessages.errorConverterNotLoadable, c.getClazz().getClazz(),
									e.getLocalizedMessage()));
					return false;
				}
			}
		}
		for (BindingConverter c : getModel().getGlobalConfiguration().getBindingConverters()) {
			if (c.getClazz() != null) {
				try {
					IBindingConverter converter = BindingManager.getConverter(c.getType(), null, getModel());
					if (converter instanceof DefaultBindingConverter) {
						throw new ESBConversionException(WESBConversionMessages.errorConverterNotResolvable);
					}
					if (!c.getType().equalsIgnoreCase(converter.getType())) {
						MessageDialog.openError(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								WESBConversionMessages.errorTitle,
								NLS.bind(WESBConversionMessages.errorWrongBindingConverterType, new Object[] {
										c.getClazz().getClazz(), c.getType(), converter.getType() }));
						return false;
					}
				} catch (InstantiationException e) {
					String errorMsg = NLS.bind(WESBConversionMessages.errorCannotInstantiate, c.getClazz().getClazz());
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.errorTitle,
							NLS.bind(WESBConversionMessages.errorConverterNotLoadable, c.getClazz().getClazz(), errorMsg));
					return false;
				} catch (Throwable e) {
					MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.errorTitle,
							NLS.bind(WESBConversionMessages.errorConverterNotLoadable, c.getClazz().getClazz(),
									e.getLocalizedMessage()));
					return false;
				}
			}
		}
		return true;
	}

	protected boolean showWarningIfNecessary() {
		final StringBuffer msg = new StringBuffer();
		HashMap<String, WESBProject> projectsToConvert = new HashMap<String, WESBProject>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (WESBProject p : getModel().getSourceProjects()) {
			if (p.isToConvert()) {
				IProject prj = ConversionUtils.getProject(p.getTargetName());
				if (prj.isAccessible()) {
					if (msg.length() > 0) {
						msg.append("\n"); //$NON-NLS-1$
					}
					String IIBprojectName = prj.getName();
					msg.append(IIBprojectName);
					IProject jcnProject = root.getProject(IIBprojectName + JCN_SUFFIX);
					// Can't tell if the WESB project will create a JCN so just
					// check whether a JCN project already exists
					if (jcnProject.exists()) {
						msg.append("\n");
						msg.append(IIBprojectName + JCN_SUFFIX);
					}
					// Check whether the WESB project will create a Java project
					if (p.getJavas() != null) {
						IProject javaProject = root.getProject(IIBprojectName + WESBJAVA_SUFFIX);
						if (javaProject.exists()) {
							msg.append("\n");
							msg.append(IIBprojectName + WESBJAVA_SUFFIX);
						}
					}
				}
				projectsToConvert.put(p.getName(), p);
			}
		}

		if (msg.length() > 0) {
			msg.insert(0, WESBConversionMessages.ConversionEditor_projectsAlreadyExist);
		}

		if (msg.length() > 0) {
			final List<Object> result = new ArrayList<Object>();
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							WESBConversionMessages.ConversionEditor_warningTitle, msg.toString())) {
						result.add(Boolean.TRUE);
					}
				}
			});
			return result.size() > 0;
		}
		return true;
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

		optionViewer.setInput(calculteOptions());
	}

	protected Object calculteOptions() {
		List<ResourceOptions> options = new ArrayList<ConversionEditor.ResourceOptions>();
		for (WESBProject p : getModel().getSourceProjects()) {
			if (!p.isToConvert()) {
				continue;
			}
			if (p.getMaps() != null) {
				if (p.getMaps().getAllMaps().size() > 0) {
					options.add(new ResourceOptions(p.getName(), WESBConversionMessages.ConversionEditor_projectType,
							WESBConversionMessages.ConversionEditor_mapOption, calculateMapsToConvert(p.getMaps().getAllMaps()), p
									.getMaps()));
				}
			}
		}
		options.add(new ResourceOptions(
				"", WESBConversionMessages.ConversionEditor_globalOptionType, WESBConversionMessages.ConversionEditor_mergeResultOption, Boolean.toString(getModel() //$NON-NLS-1$
								.getGlobalConfiguration().isMergeResult())));
		for (PrimitiveConverter p : getModel().getGlobalConfiguration().getPrimitiveConverters()) {
			if (p.getClazz() != null && ConversionUtils.hasValue(p.getClazz().getClazz())) {
				options.add(new ResourceOptions(p.getType(), WESBConversionMessages.ConversionEditor_primitive,
						WESBConversionMessages.ConversionEditor_converter, p.getClazz().getClazz()));
			}
		}
		for (BindingConverter p : getModel().getGlobalConfiguration().getBindingConverters()) {
			if (p.getClazz() != null && ConversionUtils.hasValue(p.getClazz().getClazz())) {
				options.add(new ResourceOptions(p.getType(), WESBConversionMessages.ConversionEditor_binding,
						WESBConversionMessages.ConversionEditor_converter, p.getClazz().getClazz()));
			}
		}
		return options;
	}

	private String calculateMapsToConvert(List<WESBMap> maps) {
		StringBuffer sb = new StringBuffer();
		for (WESBMap m : maps) {
			if (m.isTobeConverted()) {
				if (sb.length() > 0) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(m.getName());
			}
		}
		return sb.toString();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection().isEmpty()) {
			return;
		}
		Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
		ResourceOptions op = (ResourceOptions) o;
		if (op.option.equals(WESBConversionMessages.ConversionEditor_mapOption)) {
			String message = null;
			String desc = WESBConversionMessages.ConversionEditor_MapsOptionDescription;
			String title = WESBConversionMessages.ConversionEditor_MapsTitle;
			String heading = WESBConversionMessages.ConversionEditor_MapsOption;
			message = WESBConversionMessages.ConversionEditor_MapsOptionMessage;
			List<String> maps = new ArrayList<String>();
			for (WESBMap m : ((WESBMaps) op.model).getAllMaps()) {
				maps.add(m.getName());
			}
			UsageDialog d = new UsageDialog(heading, title, desc, message, maps);
			d.open();
		}
	}

}
