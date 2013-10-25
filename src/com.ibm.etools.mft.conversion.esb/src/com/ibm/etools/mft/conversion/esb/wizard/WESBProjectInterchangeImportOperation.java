/*
 * Licensed Material - Property of IBM
 * (C) Copyright IBM Corp. 2001, 2005 - All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 */
/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ibm.etools.mft.conversion.esb.wizard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeImportDataModel;
import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangePlugin;
import com.ibm.bpm.common.ui.project.interchange.ProjectInterchangeResourceHandler;
import com.ibm.etools.common.frameworks.internal.datamodel.WTPOperation;
import com.ibm.etools.common.frameworks.internal.datamodel.WTPOperationDataModel;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;

public class WESBProjectInterchangeImportOperation extends WTPOperation {
	private static final int LARGE_IMPORT = 25;
	protected ZipFile zipFile;
	private int zipWork = 30; // amount of work for unzipping a zip file
	// Keys = names, values = descriptions
	protected Map projectDescriptions;
	protected IPath rootLocation;
	protected IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	protected IProgressMonitor monitor;

	/**
	 * @param model
	 */
	public WESBProjectInterchangeImportOperation(WTPOperationDataModel model) {
		super((ProjectInterchangeImportDataModel) model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.wst.common.frameworks.internal.operations.WTPOperation#execute
	 * (org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		this.monitor = monitor;
		try {
			initialize();
			monitor.beginTask(ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_2_UI_, projectDescriptions.size()
					* 4 + (zipFile.size() * zipWork));
			deleteExistingProjectsAndFolders(monitor); // accounts for
														// projectDescriptions.size()*2
														// work
			expandZip(monitor); // accounts for zipFile.size() * zipWork work
			createProjects(monitor); // accounts for
										// projectDescriptions.size()*2 work
		} finally {
			monitor.done();
		}
	}

	/**
	 * 
	 */
	private void initialize() {
		// set datamodel local variables
		ProjectInterchangeImportDataModel dataModel = (ProjectInterchangeImportDataModel) operationDataModel;
		rootLocation = new Path(dataModel.getStringProperty(ProjectInterchangeImportDataModel.FILE_UNZIP_LOCATION));
		zipFile = (ZipFile) dataModel.getProperty(ProjectInterchangeImportDataModel.FILE);
		projectDescriptions = (Map) dataModel.getProperty(ProjectInterchangeImportDataModel.SELECTED_PROJECT_MAP);
	}

	/**
	 * @param monitor
	 */
	private void createProjects(IProgressMonitor monitor) throws CoreException {
		if (isDefaultLocation())
			createDefaultLocationProjects(monitor);
		else
			createNonDefaultLocationProjects(monitor);
	}

	/**
	 * @param monitor
	 */
	private void createDefaultLocationProjects(IProgressMonitor monitor) throws CoreException {
		Iterator it = projectDescriptions.values().iterator();
		IProjectDescription description;
		while (it.hasNext()) {
			description = (IProjectDescription) it.next();
			description.setLocation(null);
			doCreateExistingProject(description, monitor, (projectDescriptions.size() > LARGE_IMPORT));
		}
	}

	/**
	 * @param monitor
	 */
	private void createNonDefaultLocationProjects(IProgressMonitor monitor) throws CoreException {
		Iterator it = projectDescriptions.entrySet().iterator();
		Map.Entry entry;
		IProjectDescription description;
		while (it.hasNext()) {
			entry = (Map.Entry) it.next();
			description = (IProjectDescription) entry.getValue();
			description.setLocation(computeLocation((String) entry.getKey()));
			doCreateExistingProject(description, monitor, (projectDescriptions.size() > LARGE_IMPORT));
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private IPath computeLocation(String name) {
		return rootLocation.append(name);
	}

	/**
	 * @param description
	 * @param monitor
	 * @param largeImport
	 */
	private void doCreateExistingProject(IProjectDescription description, IProgressMonitor monitor, boolean largeImport)
			throws CoreException {
		IProject project = getProject(description);
		project.create(description, new SubProgressMonitor(monitor, 1));
		ConversionUtils.setSingleNatureAndRemoveBuilders(project);
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		if (largeImport)
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1));
		else
			project.open(new SubProgressMonitor(monitor, 1));
	}

	/**
	 * @param description
	 * @return
	 */
	private IProject getProject(IProjectDescription description) {
		return getProject(description.getName());
	}

	/**
	 * @param description
	 * @return
	 */
	private IProject getProject(String name) {
		return workspaceRoot.getProject(name);
	}

	/**
	 * Return whether or not the specifed location is a prefix of the root.
	 */
	private boolean isDefaultLocation() {
		return Platform.getLocation().equals(rootLocation);
	}

	/**
	 * @param monitor
	 */
	private void expandZip(IProgressMonitor monitor) throws CoreException, InterruptedException {
		SubProgressMonitor subMon = null;
		try {
			subMon = new SubProgressMonitor(monitor, zipFile.size() * zipWork);
			subMon.beginTask(ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_3_UI_, zipFile.size() * zipWork);
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				checkCancelled();
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!shouldImport(entry)) {
					subMon.worked(zipWork);
					continue;
				}
				subMon.subTask(entry.getName());
				File aFile = computeLocation(entry.getName()).toFile();
				File parentFile = null;
				try {
					if (entry.isDirectory()) {
						aFile.mkdirs();
					} else {
						parentFile = aFile.getParentFile();
						if (!parentFile.exists())
							parentFile.mkdirs();
						if (!aFile.exists())
							aFile.createNewFile();
						InputStream in = zipFile.getInputStream(entry);
						if (in != null) {
							copy(in, new FileOutputStream(aFile));
							if (entry.getTime() > 0)
								aFile.setLastModified(entry.getTime());
						} else {
							// PI Import fails if directores contain "funny"
							// characters
							// bug in JRE zip implementations can cause this,
							// can't do much but log it
							ProjectInterchangePlugin
									.getInstance()
									.getLog()
									.log(new Status(IStatus.ERROR, ProjectInterchangeResourceHandler.PluginConstants_0_UI_, 1027,
											NLS.bind(ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_7_UI_,
													new Object[] { entry.getName() }), null));
						}
					}
				} catch (IOException e) {
					throw new CoreException(newErrorStatus(
							ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_4_UI_ + entry.getName(), e));
				}
				subMon.worked(zipWork);
			}
		} finally {
			if (subMon != null) {
				subMon.done();
			}
		}

	}

	/**
	 * If the entry does not belong to one of the selected projects, we need to
	 * skip it
	 */
	private boolean shouldImport(ZipEntry entry) {
		IPath aPath = new Path(entry.getName());
		if (aPath.segmentCount() == 0)
			return false;
		String projName = aPath.segment(0);
		return projectDescriptions.containsKey(projName);
	}

	/**
	 * @param monitor
	 */
	private void deleteExistingProjectsAndFolders(IProgressMonitor monitor) throws CoreException, InterruptedException {
		int timesToTry = 10;
		while (true) {
			if (timesToTry < 0) {
				break;
			}
			timesToTry--;
			Iterator iter = projectDescriptions.keySet().iterator();
			String name;
			try {
				while (iter.hasNext()) {
					name = (String) iter.next();
					IProject p = getProject(name);
					if (p.exists()) {
						p.delete(true, true, new SubProgressMonitor(monitor, 1));
					} else {
						monitor.worked(1);
					}
					checkCancelled();
					File locationFile = computeLocation(name).toFile();
					if (locationFile.exists()) {
						delete(locationFile);
					}
					monitor.worked(1);
					checkCancelled();
				}
			} catch (CoreException ex) {
				if (timesToTry < 0) {
					throw ex;
				}
			}
		}
	}

	private void checkCancelled() throws InterruptedException {
		if (monitor.isCanceled())
			throw new InterruptedException();
	}

	/**
	 * deletes a file from the file system; for directories, recurse the
	 * subdirectories and delete them as well
	 */
	public boolean delete(File aFile) throws CoreException, InterruptedException {
		if (aFile == null)
			return true;
		if (aFile.isDirectory()) {
			File[] files = aFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!delete(files[i]))
					return false;
			}
		}
		monitor.subTask(ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_5_UI_ + aFile.getAbsolutePath());
		checkCancelled();
		if (aFile.delete())
			return true;

		IStatus status = newErrorStatus(
				ProjectInterchangeResourceHandler.ProjectInterchangeImportOperation_6_UI_ + aFile.getAbsolutePath(), null);
		throw new CoreException(status);

	}

	/**
	 * Copy all the data from the input stream to the output stream up until the
	 * first end of file character, and close the two streams
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		try {
			int n = in.read(buffer);
			while (n > 0) {
				out.write(buffer, 0, n);
				n = in.read(buffer);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	private IStatus newErrorStatus(String message, Exception e) {
		return new Status(IStatus.ERROR, ProjectInterchangeResourceHandler.PluginConstants_0_UI_, 1027, message, e);
	}
}
