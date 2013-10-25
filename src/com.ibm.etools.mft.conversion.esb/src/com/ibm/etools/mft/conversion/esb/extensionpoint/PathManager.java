/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1321882349" > 
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
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibm.etools.mft.conversion.esb.model.GlobalConfigurationType;
import com.ibm.etools.mft.conversion.esb.model.PathMapping;

/**
 * @author Zhongming Chen
 *
 */
public class PathManager {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	private static List<PathMapping> builtInPathMappings = new ArrayList<PathMapping>();
	private static HashMap<String, String> builtInPathMappingsMap = new HashMap<String, String>();

	static {
		// builtInPathMappings.add(createPathMapping("/context/correlation",
		// "LocalEnvironment/Variables/context/correlation"));
	}

	private static PathMapping createPathMapping(String s, String t) {
		PathMapping mapping = new PathMapping();
		mapping.setSource(s);
		mapping.setTarget(t);
		builtInPathMappingsMap.put(s, t);
		return mapping;
	}

	public static List<PathMapping> getBuiltInPathMappings() {
		return builtInPathMappings;
	}

	public static boolean isUserDefinedPathMapping(PathMapping m) {
		return !builtInPathMappingsMap.containsKey(m.getSource());
	}

	public static HashMap<String, String> getBuiltInPathMappingsMap() {
		return builtInPathMappingsMap;
	}

	public static void populatePathMappings(HashMap<String, String> pathMappings, GlobalConfigurationType model) {
		for (PathMapping m : getBuiltInPathMappings()) {
			pathMappings.put(m.getSource(), m.getTarget());
		}
		for (PathMapping m : model.getPathMappings()) {
			pathMappings.put(m.getSource(), m.getTarget());
		}
	}

}
