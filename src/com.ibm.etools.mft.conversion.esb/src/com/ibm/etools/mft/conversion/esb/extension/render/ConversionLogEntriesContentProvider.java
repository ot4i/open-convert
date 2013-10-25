/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.DebugEntry;
import com.ibm.etools.mft.conversion.esb.userlog.ErrorEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionLogEntriesContentProvider implements ITreeContentProvider, IContentProvider {

	private boolean showAll = true;
	private IResource resource;
	private List<ConversionLogEntry> entries;

	/**
	 * 
	 */
	public ConversionLogEntriesContentProvider() {
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DefaultLogEntriesRenderer) {
			if (resource != null) {
				if (entries == null) {
					return new Object[] { resource };
				}
				List<Object> list = new ArrayList<Object>();
				if (showAll && hasUserLogEntry())
					list.add(ConversionLogEntry.class);
				if (hasTodoEntry())
					list.add(TodoEntry.class);
				if (hasProblems())
					list.add(resource);
				return list.toArray(new Object[0]);
			} else {
				if (entries.size() > 0) {
					if (entries.get(0) instanceof ErrorEntry) {
						return new Object[] { ErrorEntry.class };
					} else if (entries.get(0) instanceof DebugEntry) {
						return new Object[] { DebugEntry.class };
					} else if (entries.get(0) instanceof TodoEntry) {
						return new Object[] { TodoEntry.class };
					}
				}
			}
		} else if (inputElement == ConversionLogEntry.class) {
			List<ConversionLogEntry> ls = new ArrayList<ConversionLogEntry>();
			for (ConversionLogEntry e : entries) {
				if (!(e instanceof TodoEntry)) {
					ls.add(e);
				}
			}
			return ls.toArray();
		} else if (inputElement == ErrorEntry.class) {
			return entries.toArray();
		} else if (inputElement == DebugEntry.class) {
			return entries.toArray();
		} else if (inputElement == TodoEntry.class) {
			List<ConversionLogEntry> ls = new ArrayList<ConversionLogEntry>();
			for (ConversionLogEntry e : entries) {
				if ((e instanceof TodoEntry)) {
					if (showAll) {
						ls.add(e);
					} else if (!((TodoEntry) e).isCompleted()) {
						ls.add(e);
					}
				}
			}
			return ls.toArray();
		} else if (inputElement instanceof IResource) {
			// workspace error markers
			try {
				IMarker[] problems = ((IResource) inputElement).findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				List<IMarker> markers = new ArrayList<IMarker>();
				for (IMarker p : problems) {
					if (p.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR
							|| p.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
						markers.add(p);
					}
				}
				return markers.toArray();
			} catch (CoreException e) {
				// do nothing
			}
		}
		return new Object[0];
	}

	private boolean hasProblems() {
		try {
			IMarker[] problems = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			for (IMarker p : problems) {
				if (p.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR
						|| p.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
					return true;
				}
			}
		} catch (CoreException e) {
			// Do nothing
		}
		return false;
	}

	private boolean hasTodoEntry() {
		for (ConversionLogEntry e : entries) {
			if ((e instanceof TodoEntry)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasUserLogEntry() {
		for (ConversionLogEntry e : entries) {
			if (!(e instanceof TodoEntry)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

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
		return getChildren(element).length > 0;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public void setUserLogEntries(List<ConversionLogEntry> entries) {
		this.entries = entries;
	}

	public IResource getResource() {
		return resource;
	}
}
