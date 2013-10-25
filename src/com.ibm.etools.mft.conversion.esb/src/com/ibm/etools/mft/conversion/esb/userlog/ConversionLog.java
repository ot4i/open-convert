/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.userlog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.broker.config.common.Base64;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionLog {

	private HashMap<String, List<ConversionLogEntry>> log = new HashMap<String, List<ConversionLogEntry>>();
	private List<ConversionLogEntry> errors = new ArrayList<ConversionLogEntry>();
	private List<ConversionLogEntry> debugMessages = new ArrayList<ConversionLogEntry>();
	private HashSet<String> artifactsHasBeenUpdatedInCurrentSession = new HashSet<String>();
	private HashMap<IProject, List<IProject>> sourceToTargetProject = new HashMap<IProject, List<IProject>>();
	private List<ConversionLogEntry> allTodoes = new ArrayList<ConversionLogEntry>();
	private HashMap<String, HashSet<String>> sourceToTargetResources = new HashMap<String, HashSet<String>>();
	private HashSet<String> sourceInCurrentConversionSession = new HashSet<String>();
	private WESBConversionDataType model;
	private IFile modelFile;
	private HashSet<IProject> allTargetProjects = new HashSet<IProject>();

	/**
	 * @param modelFile
	 * @param patternInstance
	 * 
	 */
	public ConversionLog(WESBConversionDataType model, IFile modelFile) {
		this.model = model;
		this.modelFile = modelFile;
		load();
	}

	public void begin() {
		errors.clear();
		debugMessages.clear();
		if (!model.getGlobalConfiguration().isMergeResult()) {
			log.clear();
			sourceToTargetResources.clear();
		}
	}

	private void commit() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(log);
			out.writeObject(errors);
			out.writeObject(debugMessages);
			out.writeObject(sourceToTargetResources);
			out.flush();
			model.setResult(Base64.encode(bout.toByteArray()));

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void cleanup() {
		List<String> toBeDeleted = new ArrayList<String>();
		for (String k : log.keySet()) {
			IPath p = new Path(k);
			IResource r = null;
			if (p.segmentCount() > 1) {
				// a file
				r = ResourcesPlugin.getWorkspace().getRoot().getFile(p);
			} else {
				// a project
				r = ResourcesPlugin.getWorkspace().getRoot().getProject(p.lastSegment());
			}
			if (!r.exists()) {
				toBeDeleted.add(k);
			}
		}

		for (String k : toBeDeleted) {
			log.remove(k);
		}
	}

	public void end() {
		calculate();
		cleanup();
		commit();
	}

	protected void calculate() {
		allTodoes.clear();
		for (String path : log.keySet()) {
			List<ConversionLogEntry> es = log.get(path);
			for (ConversionLogEntry e : es) {
				if (e instanceof TodoEntry) {
					allTodoes.add(e);
					e.setResource(ConversionUtils.getResource(new Path(path)));
				}
			}
		}
		allTargetProjects.clear();
		for (String s : log.keySet()) {
			IResource r = ConversionUtils.getResource(new Path(s));
			allTargetProjects.add(r.getProject());
		}
		sourceToTargetProject.clear();
		allTargetProjects.clear();
		for (WESBProject p : model.getSourceProjects()) {
			if (ConversionUtils.hasValue(p.getTargetName()) && ConversionUtils.getProject(p.getTargetName()).isAccessible()) {
				List<IProject> targets = new ArrayList<IProject>();
				targets.add(ConversionUtils.getProject(p.getTargetName()));
				allTargetProjects.add(targets.get(0));
			}
		}
		// add from source to target resources map
		for (String source : sourceToTargetResources.keySet()) {
			HashSet<String> targets = sourceToTargetResources.get(source);

			IProject sourceProject = ConversionUtils.getResource(new Path(source)).getProject();
			for (String target : targets) {
				IProject targetProject = ConversionUtils.getResource(new Path(target)).getProject();
				List<IProject> ps = sourceToTargetProject.get(sourceProject);
				if (ps == null) {
					sourceToTargetProject.put(sourceProject, ps = new ArrayList<IProject>());
				}
				if (!ps.contains(targetProject)) {
					ps.add(targetProject);
					allTargetProjects.add(targetProject);
				}
			}
		}
	}

	public HashSet<IProject> getAllTargetProjects() {
		return allTargetProjects;
	}

	private void load() {
		String s = model.getResult();
		if (s == null) {
			return;
		}
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(s)));
			log = (HashMap<String, List<ConversionLogEntry>>) in.readObject();
			errors = (List<ConversionLogEntry>) in.readObject();
			debugMessages = (List<ConversionLogEntry>) in.readObject();
			sourceToTargetResources = (HashMap<String, HashSet<String>>) in.readObject();
			calculate();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, List<ConversionLogEntry>> getLog() {
		return log;
	}

	public boolean isCompleted(String r) {
		List<ConversionLogEntry> entries = log.get(r);
		if (entries != null) {
			for (ConversionLogEntry e : entries) {
				if ((e instanceof TodoEntry) && !((TodoEntry) e).isCompleted()) {
					return false;
				}
			}
		}
		return true;
	}

	public void clear() {
		log.clear();
	}

	public void addEntry(IResource r, ConversionLogEntry entry) {
		if (entry instanceof ErrorEntry) {
			errors.add((ErrorEntry) entry);
			return;
		} else if (entry instanceof DebugEntry) {
			debugMessages.add((DebugEntry) entry);
			return;
		}
		String key = r.getFullPath().toString();
		if (!artifactsHasBeenUpdatedInCurrentSession.contains(key)) {
			artifactsHasBeenUpdatedInCurrentSession.add(key);
			log.remove(key);
		}
		List<ConversionLogEntry> entries = log.get(key);
		if (entries == null) {
			// New file. Proceed to add entry
			log.put(key, entries = new ArrayList<ConversionLogEntry>());
		}
		entries.add(entry);
	}

	public List<ConversionLogEntry> getErrors() {
		return errors;
	}

	public List<ConversionLogEntry> getDebugMessages() {
		return debugMessages;
	}

	public List<ConversionLogEntry> getAllTodoes() {
		return allTodoes;
	}

	public void addSourceToTargetResource(IResource source, IResource target) {
		String sourcePath = source.getFullPath().toString();
		String targetPath = target.getFullPath().toString();
		HashSet<String> targets = sourceToTargetResources.get(sourcePath);
		if (!sourceInCurrentConversionSession.contains(sourcePath) && targets != null) {
			targets.clear();
		}
		sourceInCurrentConversionSession.add(sourcePath);
		if (targets == null) {
			sourceToTargetResources.put(sourcePath, targets = new HashSet<String>());
		}
		targets.add(targetPath);
	}

	public HashMap<String, HashSet<String>> getSourceToTargetResources() {
		return sourceToTargetResources;
	}

	public HashMap<IProject, List<IProject>> getSourceToTargetProject() {
		return sourceToTargetProject;
	}

	public IFile getModelFile() {
		return modelFile;
	}
}
