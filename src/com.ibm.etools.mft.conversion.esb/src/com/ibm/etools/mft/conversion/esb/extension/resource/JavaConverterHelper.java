/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1882696864" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2010, 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extension.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.osgi.util.NLS;

import com.ibm.etools.mft.builder.NatureConfiguration;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.logicalmodelhelpers.WorkspaceHelper;

/**
 * @author Zhongming Chen
 *
 */
public class JavaConverterHelper {

	private IProject sourceProject;
	private IProject targetProject;
	private ConversionContext context;
	private IProject targetJavaProject;

	public JavaConverterHelper(ConversionContext context, IProject source, IProject target) {
		super();
		this.sourceProject = source;
		this.targetProject = target;
		this.targetJavaProject = ConversionUtils.getProject(target.getName() + "_WESBJava"); //$NON-NLS-1$
		this.context = context;
	}

	public void convert() throws Exception {
		NullProgressMonitor monitor = new NullProgressMonitor();

		if (!targetJavaProject.exists()) {
			targetJavaProject.create(monitor);
			targetJavaProject.open(monitor);
		}

		WorkspaceHelper.addProjectNature(monitor, "org.eclipse.jdt.core.javanature", targetJavaProject); //$NON-NLS-1$
		NatureConfiguration.configureProject(targetJavaProject);

		ConversionUtils.copy(sourceProject.getFile(new Path(".classpath")), targetJavaProject.getFile(new Path(".classpath"))); //$NON-NLS-1$ //$NON-NLS-2$
		WorkspaceHelper.addProjectReference(targetProject, targetJavaProject);

		context.log.addSourceToTargetResource(sourceProject, targetJavaProject);

		sourceProject.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (ConversionUtils.isJava(resource) || ConversionUtils.isJAR(resource)) {
					ConversionUtils.copy((IFile) resource, targetJavaProject.getFile(resource.getProjectRelativePath()));
					context.log.addSourceToTargetResource(resource, targetJavaProject.getFile(resource.getProjectRelativePath()));
				}
				return true;
			}
		});

		configureClassPath();

	}

	protected void configureClassPath() throws Exception {
		IJavaProject targetJavaJP = JavaCore.create(targetJavaProject);
		IClasspathEntry[] cps = targetJavaJP.getRawClasspath();
		List<IClasspathEntry> classpaths = new ArrayList<IClasspathEntry>();

		for (IClasspathEntry cp : cps) {
			IPath newPath = cp.getPath();
			ClasspathEntry cpe = (ClasspathEntry) cp;
			if (cp.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IPath path = cp.getPath();
				if (path.segment(0).equals("org.eclipse.jdt.launching.JRE_CONTAINER")) { //$NON-NLS-1$
					// JRE, ignore remaining segments;
					newPath = new Path(path.segment(0));
					cpe.path = newPath;
				} else if (path.lastSegment().indexOf("wps.") >= 0) { //$NON-NLS-1$
					// WPS JRE, ignore
					continue;
				}
			} else if (cp.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				// if (path.toString().indexOf("/gen/src") >= 0) {
				// // gen/src, ignore
				// continue;
				// } else {
				// if (path.segment(0).equals(sourceProject.getName())) {
				// newPath = new Path(targetJavaProject.getName());
				// newPath = path.removeFirstSegments(1);
				// }
				// }
				// newPath = newPath.removeFirstSegments(1);
				// cp = new ClasspathEntry(cp.getContentKind(),
				// cp.getEntryKind(), newPath, cp.getInclusionPatterns(),
				// cp.getExclusionPatterns(), cp.getSourceAttachmentPath(),
				// cp.getSourceAttachmentRootPath(), cpe.specificOutputLocation,
				// cp.isExported(), cp.getAccessRules(),
				// cpe.combineAccessRules(), cp.getExtraAttributes());
			} else if (cp.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				IPath path = cp.getPath();
				IResource r = ConversionUtils.getResource(path);
				if ((r instanceof IProject) && (context.projects.contains(r))) {
					continue;
				}
			}
			classpaths.add(cp);
		}

		targetJavaJP.setRawClasspath(classpaths.toArray(new IClasspathEntry[0]), new NullProgressMonitor());
		context.log.addEntry(
				targetJavaProject,
				new TodoEntry(NLS.bind(WESBConversionMessages.todoFixJavaProjectProblems, sourceProject.getName(),
						targetJavaProject.getName())));
	}

}
