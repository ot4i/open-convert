package com.ibm.etools.mft.conversion.esb.editor.controller;

import java.beans.PropertyChangeEvent;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WESBConversionPlugin;
import com.ibm.etools.mft.conversion.esb.editor.WESBConversionEditor;
import com.ibm.etools.mft.conversion.esb.model.ObjectFactory;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.wizard.editor.model.IBaseConfigurationStep;
import com.ibm.etools.mft.wizard.editor.model.IController;
import com.ibm.etools.mft.wizard.editor.model.TreeModel;
import com.ibm.mb.common.model.Parameters.Parameter;
import com.ibm.mb.common.model.TranslatableText;

/**
 * @author Zhongming Chen
 * 
 */
public class Controller implements IController {
	private List<IBaseConfigurationStep> steps = null;
	private IFile modelFile = null;
	private WESBConversionDataType model = null;
	private WESBConversionEditor editor;
	private SourceSelectionStep sourceSelectionStep;
	private ResourceConfigurationStep resourceConfigurationStep;
	private GlobalOptionsStep globalOptionsStep;
	private ConvertStep convertStep;
	private SummaryStep summaryStep;
	private ConversionLog Log;
	private Observer logViewer = null;

	public static String WESBTYPE = "wesbToIB"; //$NON-NLS-1$

	public Controller(WESBConversionEditor editor, IFile file) throws CoreException {
		modelFile = file;
		this.editor = editor;
		loadModel();
		createSteps();
	}

	protected void loadModel() {
		model = ConversionUtils.loadWESBConversionModel(ConversionUtils.readFile(modelFile));
		Log = new ConversionLog(model, modelFile);
	}

	/**
	 * This method will load the connector framework and gets all the steps
	 * provided by the connector
	 * 
	 * @return
	 * @throws CoreException
	 */
	private List<IBaseConfigurationStep> createSteps() throws CoreException {

		steps = new ArrayList<IBaseConfigurationStep>();

		steps.add(sourceSelectionStep = new SourceSelectionStep(model, this));
		steps.add(resourceConfigurationStep = new ResourceConfigurationStep(model, this));
		steps.add(globalOptionsStep = new GlobalOptionsStep(model, this));
		steps.add(convertStep = new ConvertStep(model, this));
		steps.add(summaryStep = new SummaryStep(model, this));

		return steps;
	}

	@Override
	public List<IBaseConfigurationStep> getSteps() throws CoreException {
		return steps;
	}

	@Override
	public void save(IProgressMonitor monitor) {
		String content = ConversionUtils.saveModel(new ObjectFactory().createWesbConversionData(model),
				WESBConversionConstants.PACKAGE_WESB_CONVERSION_TYPE);
		try {
			modelFile.setContents(new ByteArrayInputStream(content.getBytes("UTF-8")), true, true, monitor); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getModel() {
		return model;
	}

	@Override
	public Object getValue(Parameter parameterToGetValueOf) {
		return null;
	}

	@Override
	public IStatus validate(Parameter parameterToValidate) {
		// TODO Add validation logic
		return Status.OK_STATUS;
	}

	@Override
	public void valueChanged(PropertyChangeEvent event) {
		System.out.println();
	}

	@Override
	public String getTranslatedValue(TranslatableText nlKey) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void reset() {
	}

	@Override
	public TreeModel getSummaryTree() {
		return null;
	}

	@Override
	public boolean canContainUnlimitedSelectedObjects() {
		return false;
	}

	public IFile getModelFile() {
		return modelFile;
	}

	public WESBConversionEditor getEditor() {
		return editor;
	}

	public void sourceProjectsChanged() {
		sourceSelectionStep.setChanged(true);
		resourceConfigurationStep.setCompleted(false);
		globalOptionsStep.setCompleted(false);
		convertStep.setCompleted(false);
		summaryStep.setCompleted(false);
		refreshNavigationBar();
		resultOutOfSync();
	}

	protected void resultOutOfSync() {
		refreshLogStatus(new Status(Status.WARNING, WESBConversionPlugin.getDefault().getBundle().getSymbolicName(),
				WESBConversionMessages.warningResultOutOfSync));
	}

	public void resourceOptionsChanged() {
		globalOptionsStep.setCompleted(false);
		convertStep.setCompleted(false);
		summaryStep.setCompleted(false);
		refreshNavigationBar();
		resultOutOfSync();
	}

	public void globalOptionsChanged() {
		convertStep.setCompleted(false);
		summaryStep.setCompleted(false);
		refreshNavigationBar();
		resultOutOfSync();
	}

	public ConvertStep getConvertStep() {
		return convertStep;
	}

	public GlobalOptionsStep getGlobalOptionsStep() {
		return globalOptionsStep;
	}

	public ResourceConfigurationStep getResourceConfigurationStep() {
		return resourceConfigurationStep;
	}

	public SourceSelectionStep getSourceSelectionStep() {
		return sourceSelectionStep;
	}

	public SummaryStep getSummaryStep() {
		return summaryStep;
	}

	public void refreshNavigationBar() {
		editor.refreshNavigatoinBar();
	}

	@Override
	public IResource getInputResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCustomTreeEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	public ConversionLog getLog() {
		return Log;
	}

	public Observer getLogViewer() {
		return logViewer;
	}

	public void setLogViewer(Observer logViewer) {
		this.logViewer = logViewer;
	}

	public void refreshLogViewer() {
		logViewer.update(null, null);
	}

	public void refreshLogStatus(IStatus status) {
		model.setResultOutOfSync(status.getSeverity() != Status.INFO);
		if (logViewer != null) {
			logViewer.update(null, status);
		}
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setResultStatus(IStatus status) {
	}
}
