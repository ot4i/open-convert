/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor.resourceoptions.pages;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.ResourceOptionsEditor;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBProjectOptionPage extends DefaultResourceOptionPage implements IResourceOptionPage, ISelectionChangedListener {

	private WESBConversionDataType model;
	private WESBProject project;
	private ComboViewer landingPointsViewer;

	/**
	 * @param owner
	 * @param parent
	 * @param style
	 */
	public WESBProjectOptionPage(ResourceOptionsEditor owner, Composite parent, int style) {
		super(owner, parent, style);
	}

	@Override
	protected void createOptions() {
		new Label(this, SWT.None).setText(WESBConversionMessages.convertedTo);
		landingPointsViewer = new ComboViewer(this, SWT.None);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		landingPointsViewer.getControl().setLayoutData(data);
		landingPointsViewer.addSelectionChangedListener(this);
		landingPointsViewer.setContentProvider(new ArrayContentProvider());
		landingPointsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return WESBConversionMessages.getMessage(element.toString());
			}
		});
	}

	@Override
	public void setDetail(Object model, Object detail) {
		this.model = (WESBConversionDataType) model;
		this.project = (WESBProject) detail;
		landingPointsViewer.setInput(this.project.getApplicableLandingPoints());
		landingPointsViewer.getControl().setEnabled(this.project.getApplicableLandingPoints().size() > 1);
		if (project.getLandingPoint() != null) {
			landingPointsViewer.setSelection(new StructuredSelection(project.getLandingPoint()));
		} else if (this.project.getApplicableLandingPoints().size() > 0) {
			landingPointsViewer.setSelection(new StructuredSelection(this.project.getApplicableLandingPoints().get(0)));
		}
		conversionNotes.setText(getMessage());
	}

	protected String getMessage() {
		String message = WESBConversionConstants.TYPE_MODULE.equals(project.getType()) ? WESBConversionMessages.previewMessage_MODULE
				: WESBConversionMessages.previewMessage_LIB;
		return NLS.bind(message, new Object[] { project.getName(), WESBConversionMessages.getMessage(project.getLandingPoint()),
				project.getTargetName() });
	}

	protected String getSelectedLandingPointDisplayName() {
		IStructuredSelection sel = (IStructuredSelection) landingPointsViewer.getSelection();
		if (sel.isEmpty()) {
			return ""; //$NON-NLS-1$
		} else {
			return WESBConversionMessages.getMessage(sel.getFirstElement().toString());
		}
	}

	protected String getSelectedLandingPoint() {
		IStructuredSelection sel = (IStructuredSelection) landingPointsViewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		} else {
			return sel.getFirstElement().toString();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		if (e.getSource() == landingPointsViewer) {
			project.setLandingPoint(getSelectedLandingPoint());
		}
	}

	@Override
	protected int getGridColumnCount() {
		return 3;
	}
}
