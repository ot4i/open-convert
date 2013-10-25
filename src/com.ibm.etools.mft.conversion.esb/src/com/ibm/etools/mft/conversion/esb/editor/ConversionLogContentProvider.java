/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.parameter.WESBProjectComparator;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionLogContentProvider implements IContentProvider, ITreeContentProvider {

	private ConversionLog model;
	private HashMap<IProject, List<IFile>> targetProjects = new HashMap<IProject, List<IFile>>();
	private boolean isShowAll = true;

	/**
	 * 
	 */
	public ConversionLogContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IProject) {
			if (model.getAllTargetProjects().contains(inputElement)) {
				// it is target project.
				if (!((IProject) inputElement).isAccessible()) {
					return new Object[0];
				}
				if (targetProjects.get(inputElement) != null) {
					return targetProjects.get(inputElement).toArray();
				}
			} else {
				// it is source project
				List<IProject> ps = model.getSourceToTargetProject().get(inputElement);
				Collections.sort(ps, new WESBProjectComparator());
				return ps.toArray();
			}
		} else if (inputElement instanceof IFile) {
			return new Object[0];
		} else if (inputElement instanceof ConversionLog) {
			if (model.getErrors().size() > 0) {
				List<Object> ps = new ArrayList<Object>();
				ps.add(WESBConversionMessages.errorInformation);
				if (model.getDebugMessages().size() > 0) {
					ps.add(WESBConversionMessages.debugMessages);
				}
				return ps.toArray();
			} else {
				List<Object> ps = new ArrayList<Object>();
				ps.add(WESBConversionMessages.summaryInformation);
				if (model.getDebugMessages().size() > 0 || model.getErrors().size() > 0) {
					ps.add(WESBConversionMessages.traceInformation);
				}
				ps.add(WESBConversionMessages.detailResultInformation);
				return ps.toArray();
			}
		} else if (inputElement.toString().equals(WESBConversionMessages.summaryInformation)) {
			return new Object[] { TodoEntry.class, IMarker.class };
		} else if (inputElement.toString().equals(WESBConversionMessages.traceInformation)) {
			List<Object> ps = new ArrayList<Object>();
			if (model.getDebugMessages().size() > 0) {
				ps.add(WESBConversionMessages.debugMessages);
			}
			if (model.getErrors().size() > 0) {
				ps.add(WESBConversionMessages.errors);
			}
			return ps.toArray();
		} else if (inputElement.toString().equals(WESBConversionMessages.detailResultInformation)) {
			List<Object> ps = new ArrayList<Object>(model.getSourceToTargetProject().keySet());
			Collections.sort(ps, new WESBProjectComparator());
			return ps.toArray();
		}
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		model = (ConversionLog) newInput;
		process();
	}

	public void process() {
		targetProjects.clear();

		if (model == null) {
			return;
		}

		for (String s : model.getLog().keySet()) {
			IResource r = ConversionUtils.getResource(new Path(s));
			if (r instanceof IProject) {
				if (!targetProjects.containsKey(r)) {
					targetProjects.put((IProject) r, new ArrayList<IFile>());
				}
			} else if (r instanceof IFile) {
				if (!isShowAll && !hasOutstandingTasks(r)) {
					continue;
				}
				List<IFile> ps = targetProjects.get(r.getProject());
				if (ps == null) {
					targetProjects.put(r.getProject(), ps = new ArrayList<IFile>());
				}
				if (!isShowAll) {
					if (hasOutstandingTasks(r)) {
						ps.add((IFile) r);
					}
				} else {
					ps.add((IFile) r);
				}
				Collections.sort(ps, new FileComparator());
			}
		}

		if (!isShowAll) {
			List<IProject> toRemove = new ArrayList<IProject>();
			for (IProject p : targetProjects.keySet()) {
				if (targetProjects.get(p).size() == 0 && !hasOutstandingTasks(p)) {
					toRemove.add(p);
				}
			}

			for (IProject p : toRemove) {
				targetProjects.remove(p);
			}
		}
	}

	/**
	 * @return True if there are to-do items.
	 */
	public boolean hasOutstandingTasks() {
		if (model != null) {
			for (String s : model.getLog().keySet()) {
				IResource r = ConversionUtils.getResource(new Path(s));
				if (hasOutstandingTasks(r)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasOutstandingTasks(IResource r) {
		List<ConversionLogEntry> es = model.getLog().get(r.getFullPath().toString());
		for (ConversionLogEntry e : es) {
			if ((e instanceof TodoEntry) && !((TodoEntry) e).isCompleted()) {
				return true;
			}
		}
		return false;
	}

	public void setShowAll(boolean isShowAll) {
		this.isShowAll = isShowAll;
		process();
	}
}
