/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2013" 
 *  crc="2883610894" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;

/**
 * @author Chris Kalus
 *
 */
public class CreateJCNProject {

	final static String JAVA = "Java"; //$NON-NLS-1$
	final static String JCN_HOME = "JCN_HOME"; //$NON-NLS-1$
	final static String JCN_NATURE = "com.ibm.etools.mft.jcn.jcnnature"; //$NON-NLS-1$ 
	final static String JAVA_COMPUTE = "javacompute.jar"; //$NON-NLS-1$
	final static String JPLUGIN2 = "jplugin2.jar"; //$NON-NLS-1$

	/*
	 * Create a JavaComputeNode Project referenced from a base Project
	 */
	public static IProject create(IProject baseProject) throws CoreException {

		// get the base project's name
		String baseName = baseProject.getName();

		// get a handle to the new JCN project
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject jcnProject = root.getProject(baseName + JAVA);

		if (!jcnProject.exists()) {
			// create and open it
			jcnProject.create(null);
			jcnProject.open(null);

			// create a JavaProject
			IJavaProject javaProject = JavaCore.create(jcnProject);

			// Get any existing project Natures
			IProjectDescription description = jcnProject.getDescription();
			String[] natures = description.getNatureIds();
			// copy across to a larger array
			String[] newNatures = new String[natures.length + 2];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			// add Natures for Java and JCN
			newNatures[natures.length] = JavaCore.NATURE_ID;
			newNatures[natures.length + 1] = JCN_NATURE;
			// set back into the project
			description.setNatureIds(newNatures);
			jcnProject.setDescription(description, null);

			// create a classpath entry for the source folder
			IClasspathEntry sourcePath = JavaCore.newSourceEntry(jcnProject.getFullPath());
			// get classpath entries for the JRE
			IClasspathEntry[] defJRELib = PreferenceConstants.getDefaultJRELibrary();
			// create classpath entries for the Broker JCN jars
			IPath path = new Path(JCN_HOME).append(JAVA_COMPUTE);
			IClasspathEntry jcPath = JavaCore.newVariableEntry(path, null, null);
			path = new Path(JCN_HOME).append(JPLUGIN2);
			IClasspathEntry jp2Path = JavaCore.newVariableEntry(path, null, null);
			// create a new array for all the classpath entries
			IClasspathEntry[] newClassPaths = new IClasspathEntry[defJRELib.length + 3];
			// copy the entries into the new array
			newClassPaths[0] = sourcePath;
			System.arraycopy(defJRELib, 0, newClassPaths, 1, defJRELib.length);
			newClassPaths[newClassPaths.length - 2] = jcPath;
			newClassPaths[newClassPaths.length - 1] = jp2Path;
			// set the new classpath entries into the project
			javaProject.setRawClasspath(newClassPaths, null);

			// set the Java output location to the root of the project folder
			// (defaults to "bin")
			IPath outputLoc = new Path("/" + baseName + JAVA); //$NON-NLS-1$
			javaProject.setOutputLocation(outputLoc, null);
		}

		// reference the JCN Project from the base project
		addProjectReference(baseProject, jcnProject);

		return jcnProject;
	}

	/*
	 * Add a project reference
	 */
	public static void addProjectReference(IProject baseProject, IProject referencedProject) throws CoreException {
		// Get any existing referenced projects
		IProjectDescription baseDescription = baseProject.getDescription();
		IProject[] oldRefs = baseDescription.getReferencedProjects();
		// create a new array and copy references across
		IProject[] newRefs = new IProject[oldRefs.length + 1];
		System.arraycopy(oldRefs, 0, newRefs, 0, oldRefs.length);
		// add the new reference
		newRefs[oldRefs.length] = referencedProject;
		// set references back into the base project
		baseDescription.setReferencedProjects(newRefs);
		baseProject.setDescription(baseDescription, null);
	}

}
