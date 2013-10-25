/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3575711463" > 
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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.WESBResource;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.logicalmodelhelpers.WorkspaceHelper;
import com.ibm.etools.mft.util.WMQIConstants;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBModuleConverter extends WESBLibConverter implements WESBConversionConstants {

	private SCAModuleConverterHelper helper;

	@Override
	public WESBResource preview(ConversionContext context) {
		WESBProject project = (WESBProject) super.preview(context);
		SCAModule moduleNode = (SCAModule) ConversionUtils.getObjectForClass(project.getChildren(), SCAModule.class);
		helper = new SCAModuleConverterHelper(context);
		HashMap<String, List<String>> mapUsages = new HashMap<String, List<String>>();
		project.setModule((SCAModule) helper.preview(context.model, moduleNode, mapUsages));

		if (project.getMaps() != null) {
			ConversionUtils.addUsages(mapUsages, project.getMaps().getAllMaps(), WESBMap.class, "name", false); //$NON-NLS-1$
		}

		selectMapBasedOnUsage(project.getMaps());

		project.getApplicableLandingPoints().clear();
		project.getApplicableLandingPoints().addAll(helper.getApplicableLandingPoints());
		if (project.getApplicableLandingPoints().size() > 0) {
			project.setLandingPoint(project.getApplicableLandingPoints().get(0));
		}

		return project;
	}

	protected void addMapUsage(HashMap<String, List<String>> mapUsages, List<WESBMap> allMaps) {

	}

	protected List<String> getApplicableLandingPoints() {
		List<String> landingPoints = new ArrayList<String>();
		landingPoints.add(IIB_SERVICE);
		landingPoints.add(IIB_APPLICATION);
		return landingPoints;
	}

	@Override
	protected void calculateMapUsage(WESBMaps maps) {
		super.calculateMapUsage(maps);
	}

	@Override
	protected void doConvert(IProject targetP) throws Exception {
		super.doConvert(targetP);

		helper = new SCAModuleConverterHelper(context);
		helper.convert();
	}

	@Override
	protected void addLogEntry(IResource resource, IProject targetP, ConversionLog userLog) throws CoreException {
		if (targetP.hasNature(WMQIConstants.SERVICE_APPLICATION_NATURE_ID)) {
			userLog.addEntry(
					targetP,
					new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoConversionMessage_MediationModuleToService,
							resource.getName(), targetP.getName())));
		} else {
			userLog.addEntry(
					targetP,
					new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoConversionMessage_MediationModuleToApplication,
							resource.getName(), targetP.getName())));
		}
	}

	@Override
	protected void configureNature(IProject targetP, IProgressMonitor monitor) throws CoreException {
		if (IIB_SERVICE.equals(ConversionUtils.getESBProject(context).getLandingPoint())) {
			WorkspaceHelper.addProjectNature(monitor, WMQIConstants.SERVICE_APPLICATION_NATURE_ID, targetP);
		}
		WorkspaceHelper.addProjectNature(monitor, WMQIConstants.APPLICATION_NATURE_ID, targetP);
		WorkspaceHelper.addProjectNature(monitor, WMQIConstants.MESSAGE_BROKER_PROJECT_NATURE_ID, targetP);
	}

}
