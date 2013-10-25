package com.ibm.etools.mft.conversion.esb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.broker.config.appdev.FlowRendererMSGFLOW;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.etools.fcb.actions.FCBLayoutAction;
import com.ibm.etools.fcb.actions.IFCBActionConstants;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.flow.editor.MFTGraphicalEditorPart;

/**
 * The class manage all uncommitted message flows during conversion. Auto-layout
 * will apply at the end of the conversion.
 * 
 * @author Zhongming Chen
 * 
 */
public class FlowResourceManager {

	private HashMap<String, FlowResource> flows = new HashMap<String, FlowResource>();
	private HashMap<IResource, List<ConversionLogEntry>> log = new HashMap<IResource, List<ConversionLogEntry>>();
	private HashSet<FlowResource> delta = new HashSet<FlowResource>();

	public void commit(ConversionContext context) throws Exception {
		for (FlowResource r : flows.values()) {
			ConversionUtils.createFolder(r.file.getLocation().toFile());
			FlowRendererMSGFLOW.write(r.flow, r.file.getProject().getLocation().toFile().toString());
			r.file.refreshLocal(1, new NullProgressMonitor());
			if (r.requireAutoLayout) {
				context.monitor.setTaskName(WESBConversionMessages.FlowResourceManager_conversionProgressLayoutMessageFlows);
				autoLayout(r.file);
			}
		}
		for (IResource r : log.keySet()) {
			for (ConversionLogEntry e : log.get(r)) {
				context.log.addEntry(r, e);
			}
		}
	}

	protected void autoLayout(final IFile file) {
		long startTime = System.currentTimeMillis();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IEditorPart e = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.findEditor(new FileEditorInput(file));
					if (e != null) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(e, false);
					}
					MFTGraphicalEditorPart editor = (MFTGraphicalEditorPart) IDE.openEditor(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(), file, "com.ibm.etools.mft.flow.editor"); //$NON-NLS-1$
					FCBLayoutAction action = (FCBLayoutAction) editor.getActionRegistry().getAction(
							IFCBActionConstants.LAYOUT_LEFT_TO_RIGHT);
					action.run();
					editor.save(new NullProgressMonitor());
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		long duration = System.currentTimeMillis() - startTime;
		// defect 21188. The interval of autolayout must be longer than 500 ms
		// in order to comply accessibility guideline.
		if (duration < 500) {
			try {
				Thread.sleep(duration + 10);
			} catch (InterruptedException e) {
			}
		}
	}

	public IFile getFlowResource(MessageFlow flow) {
		return flows.get(PrimitiveManager.getFullyQualifiedFlowName(flow)).file;
	}

	public void log(IResource r, ConversionLogEntry e) {
		List<ConversionLogEntry> entries = log.get(r);
		if (entries == null) {
			log.put(r, entries = new ArrayList<ConversionLogEntry>());
		}
		entries.add(e);
	}

	public void beginSnapshot() {
		delta.clear();
	}

	public void addFlow(String key, FlowResource fr) {
		if (!flows.containsKey(key)) {
			delta.add(fr);
		}
		flows.put(key, fr);
	}

	public IFile getFlowResource(String fullyQualifiedFlowName) {
		return flows.get(fullyQualifiedFlowName).file;
	}

	public MessageFlow getFlow(String fullyQualifiedFlowName) {
		return flows.get(fullyQualifiedFlowName).flow;
	}

	public HashSet<FlowResource> getDelta() {
		return delta;
	}
}