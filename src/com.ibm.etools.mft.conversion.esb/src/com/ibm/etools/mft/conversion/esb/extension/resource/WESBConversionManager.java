package com.ibm.etools.mft.conversion.esb.extension.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.ESBConversionException;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.WESBResourceManager;
import com.ibm.etools.mft.conversion.esb.model.BindingConverter;
import com.ibm.etools.mft.conversion.esb.model.PrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.ErrorEntry;

/**
 * @author Zhongming Chen
 *
 */
public class WESBConversionManager {

	protected ConversionContext context;

	public WESBConversionManager(WESBConversionDataType model, ConversionLog log) {
		this.context = new ConversionContext(model, log);
	}

	private void preview(ConversionContext context) {
		context.resource = ConversionUtils.getProject(context.wesbResource.getName());
		if (ConversionUtils.isESBLib((IProject) context.resource)) {
			WESBResourceManager.getWESBLibConverter().preview(context);
		} else if (ConversionUtils.isESBModule((IProject) context.resource)) {
			WESBResourceManager.getWESBModuleConverter().preview(context);
		}
	}

	public void preview(IProgressMonitor monitor) {

		List<IProject> ps = new ArrayList<IProject>();
		for (WESBProject esbp : context.model.getSourceProjects()) {
			ps.add(ConversionUtils.getProject(esbp.getName()));
		}

		context.monitor = monitor;
		context.projects = ps;

		context.monitor.beginTask(WESBConversionMessages.progress_Previewing, context.projects.size());
		initializeGlobalOptions(context.model);

		for (WESBProject esbp : context.model.getSourceProjects()) {
			if (!esbp.isToConvert())
				continue;
			context.monitor.setTaskName(NLS.bind(WESBConversionMessages.progress_PreviewingProject, esbp.getName()));
			context.wesbResource = esbp;
			preview(context);
			context.monitor.worked(1);
		}

		tidyupGlobalOptions(context.model);

	}

	public void tidyupGlobalOptions(WESBConversionDataType model) {
		List<Object> toRemove = new ArrayList<Object>();
		for (PrimitiveConverter pc : model.getGlobalConfiguration().getPrimitiveConverters()) {
			if (pc.getUsages().size() == 0) {
				toRemove.add(pc);
			}
		}
		model.getGlobalConfiguration().getPrimitiveConverters().removeAll(toRemove);
		for (BindingConverter pc : model.getGlobalConfiguration().getBindingConverters()) {
			if (pc.getUsages().size() == 0) {
				toRemove.add(pc);
			}
		}
		model.getGlobalConfiguration().getBindingConverters().removeAll(toRemove);
	}

	private void initializeGlobalOptions(WESBConversionDataType model) {
		for (PrimitiveConverter pc : model.getGlobalConfiguration().getPrimitiveConverters()) {
			pc.getUsages().clear();
		}
		for (BindingConverter pc : model.getGlobalConfiguration().getBindingConverters()) {
			pc.getUsages().clear();
		}
	}

	public void convert(IFile conversionFile, IProgressMonitor monitor) {
		context.monitor = monitor;
		context.log.begin();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				try {

					HashMap<IProject, WESBProject> projectsInvolved = new HashMap<IProject, WESBProject>();
					for (WESBProject p : context.model.getSourceProjects()) {
						if (p.isToConvert()) {
							IProject proj = ConversionUtils.getProject(p.getName());
							projectsInvolved.put(proj, p);
						}
					}

					context.monitor.beginTask(WESBConversionMessages.progressBeginConversion, projectsInvolved.size());

					context.projects = new ArrayList<IProject>(projectsInvolved.keySet());
					PrimitiveManager.setContext(context);
					BindingManager.setContext(context);

					final List<IProject> projectOrder = getProjectOrder(new HashSet<IProject>(projectsInvolved.keySet()));
					for (IProject p : projectOrder) {
						context.monitor.setTaskName(WESBConversionMessages.progressConverting + p.getName());
						WESBProject wesbp = projectsInvolved.get(p);
						if (!wesbp.isToConvert()) {
							continue;
						}
						context.wesbResource = wesbp;
						context.resource = p;
						if (ConversionUtils.isESBLib(p)) {
							WESBResourceManager.getWESBLibConverter().convert(context);
						} else if (ConversionUtils.isESBModule(p)) {
							WESBResourceManager.getWESBModuleConverter().convert(context);
						}
						context.monitor.worked(1);
					}

				} catch (Throwable e) {
					context.log.addEntry(null, new ErrorEntry(ConversionUtils.getExceptionMessage(e)));
				}
			}
		};

		try {
			op.run(monitor);
		} catch (Exception e) {
			context.log.addEntry(null, new ErrorEntry(ConversionUtils.getExceptionMessage(e)));
		}

		context.log.end();

	}

	private List<IProject> getProjectOrder(HashSet<IProject> projectsInvolved) throws Exception {
		List<IProject> order = new ArrayList<IProject>();
		HashSet<IProject> processed = new HashSet<IProject>();

		HashMap<IProject, Integer> referenceCount = new HashMap<IProject, Integer>();

		populateProjectPair(projectsInvolved, referenceCount);

		do {
			if (processed.size() == projectsInvolved.size()) {
				break;
			}
			int processedSize = processed.size();
			for (IProject p : projectsInvolved) {
				if (processed.contains(p)) {
					continue;
				}
				if (referenceCount.get(p) == null) {
					processed.add(p);
					order.add(0, p);
					try {
						HashSet<IProject> projectsReferenceRemoved = new HashSet<IProject>();
						for (IProject p1 : p.getDescription().getReferencedProjects()) {
							if (!projectsInvolved.contains(p1)) {
								continue;
							}
							projectsReferenceRemoved.add(p1);
							Integer count = referenceCount.get(p1);
							if (count != null) {
								if (count.intValue() > 1) {
									referenceCount.put(p1, new Integer(count.intValue() - 1));
								} else {
									referenceCount.remove(p1);
								}
							}
						}
						for (IProject p1 : ConversionUtils.getClassPathReference(p)) {
							if (!projectsInvolved.contains(p1)) {
								continue;
							}
							if (projectsReferenceRemoved.contains(p1)) {
								continue;
							}
							Integer count = referenceCount.get(p1);
							if (count != null) {
								if (count.intValue() > 1) {
									referenceCount.put(p1, new Integer(count.intValue() - 1));
								} else {
									referenceCount.remove(p1);
								}
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
			if (processed.size() == 0 || processedSize == processed.size()) {
				// nothing has been processed, maybe circular project reference
				throw new ESBConversionException(WESBConversionMessages.errorCircularProjectReferenceDetected);
			}
		} while (true);

		return order;
	}

	private void populateProjectPair(HashSet<IProject> ps, HashMap<IProject, Integer> referenceCount) {
		for (IProject p : ps) {
			try {
				HashSet<IProject> processed = new HashSet<IProject>();
				for (IProject p1 : p.getDescription().getReferencedProjects()) {
					Integer count = referenceCount.get(p1);
					if (count == null) {
						referenceCount.put(p1, new Integer(1));
					} else {
						referenceCount.put(p1, new Integer(count.intValue() + 1));
					}
					processed.add(p1);
				}

				for (IProject p1 : ConversionUtils.getClassPathReference(p)) {
					if (processed.contains(p1) || !(ps.contains(p1))) {
						continue;
					}
					processed.add(p1);
					Integer count = referenceCount.get(p1);
					if (count == null) {
						referenceCount.put(p1, new Integer(1));
					} else {
						referenceCount.put(p1, new Integer(count.intValue() + 1));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
