/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="2598497136" > 
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
package com.ibm.etools.mft.conversion.esb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBProjectInterchangeImportUtil {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	// Natures to be removed
	private static final List<String> naturesToRemove = Arrays.asList(new String[] {
			"org.eclipse.wst.common.modulecore.ModuleCoreNature", //$NON-NLS-1$
			"org.eclipse.wst.common.project.facet.core.nature", //$NON-NLS-1$
			"org.eclipse.jdt.core.javanature", //$NON-NLS-1$
			"org.eclipse.jem.workbench.JavaEMFNature", //$NON-NLS-1$
			"com.ibm.ws.sca.rapiddeploy.style.SCAProjectNature" //$NON-NLS-1$
	});

	// Builders to be removed
	private static final List<String> buildersToRemove = Arrays.asList(new String[] {
			"com.ibm.wbit.project.wbimodulebuilder_prejdt", //$NON-NLS-1$
			"com.ibm.wbit.project.wbimodulebuilder_postjdt", //$NON-NLS-1$
			"org.eclipse.jdt.core.javabuilder" //$NON-NLS-1$
	});

	// Bundle to verify WID shell shared
	private static final String WID_MEDIATION_BUNDLE = "com.ibm.wbit.sib.mediation.model"; //$NON-NLS-1$

	/*
	 * @return True if shell sharing with WID
	 */
	public static boolean widShellShared() {
		Bundle widBundle = Platform.getBundle(WID_MEDIATION_BUNDLE);
		if (widBundle != null) {
			return true;
		}
		return false;
	}

	/*
	 * Update build spec to avoid build errors
	 * 
	 * @param builderIds Original builder ids from the source project
	 * 
	 * @return Update set of builders
	 */
	public static ICommand[] updateBuildSpec(ICommand[] builderIds) {
		HashSet<ICommand> updatedBuilderSet = new HashSet<ICommand>();
		for (int j = 0; j < builderIds.length; j++) {
			if (buildersToRemove.contains(builderIds[j].getBuilderName())) {
				// Remove builders that cause workspace build errors
				continue;
			}
			updatedBuilderSet.add(builderIds[j]);
		}
		// remove all build commands.
		return new ICommand[0];
	}

	/*
	 * Update project natures to avoid triggering the migration wizard
	 * 
	 * @param natureIds Original nature ids from the source project
	 * 
	 * @return Update set of natures
	 */

	public static String[] updateNatures(String[] natureIds) {
		HashSet<String> updatedNatureSet = new HashSet<String>();
		for (int j = 0; j < natureIds.length; j++) {
			if (naturesToRemove.contains(natureIds[j])) {
				// Remove natures that trigger the migration wizard
				continue;
			}
			updatedNatureSet.add(natureIds[j]);
		}
		return updatedNatureSet.toArray(new String[0]);
	}
}
