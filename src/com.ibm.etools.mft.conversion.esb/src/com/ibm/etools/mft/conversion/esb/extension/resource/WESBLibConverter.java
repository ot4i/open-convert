/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="2030047707" > 
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
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.ibm.etools.mft.builder.NatureConfiguration;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.ESBConversionException;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IWESBResourceHandler;
import com.ibm.etools.mft.conversion.esb.model.WESBJavas;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.WESBResource;
import com.ibm.etools.mft.conversion.esb.model.WESBSchemas;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.logicalmodelhelpers.WorkspaceHelper;
import com.ibm.etools.mft.util.WMQIConstants;

/**
 * @author Zhongming Chen
 *
 */
public class WESBLibConverter implements IWESBResourceHandler, WESBConversionConstants {

	public static final String IIB_LIBRARY = "iibLibrary"; //$NON-NLS-1$
	protected boolean hasJava;
	protected List<String> allMapFiles = new ArrayList<String>();
	protected ConversionContext context;
	private boolean hasXSDOrWSDL;

	@Override
	public WESBResource preview(ConversionContext context) {
		this.context = context;
		WESBResource resource = context.wesbResource;

		if (resource == null || !(resource instanceof WESBProject)) {
			resource = new WESBProject();
		}
		WESBProject wesbProject = (WESBProject) resource;

		wesbProject.getApplicableLandingPoints().clear();
		wesbProject.getApplicableLandingPoints().addAll(getApplicableLandingPoints());

		IProject p = (IProject) context.resource;
		hasJava = false;
		hasXSDOrWSDL = false;
		allMapFiles.clear();
		HashMap<String, List<String>> mapUsages = new HashMap<String, List<String>>();
		processProject(p);
		if (allMapFiles.size() > 0) {
			if (wesbProject.getMaps() == null) {
				wesbProject.setMaps(new WESBMaps());
			}
			previewMaps(wesbProject.getMaps(), p, mapUsages);
			ConversionUtils.addUsages(mapUsages, wesbProject.getMaps().getAllMaps(), WESBMap.class, "name", false); //$NON-NLS-1$
		} else {
			wesbProject.setMaps(null);
		}
		if (hasJava) {
			if (wesbProject.getJavas() == null) {
				wesbProject.setJavas(new WESBJavas());
			}
			previewJavas(wesbProject.getJavas(), p);
		}
		if (hasXSDOrWSDL) {
			if (wesbProject.getSchemas() == null) {
				wesbProject.setSchemas(new WESBSchemas());
			}
		}

		return resource;
	}

	protected void previewJavas(WESBJavas javas, IProject p) {
	}

	protected void previewMaps(WESBMaps maps, IProject p, HashMap<String, List<String>> mapUsages) {
		MapConverterHelper helper = new MapConverterHelper(context);
		HashMap<String, WESBMap> allMaps = new HashMap<String, WESBMap>();
		for (WESBMap m : maps.getAllMaps()) {
			allMaps.put(m.getName(), m);
		}

		for (String s : allMapFiles) {
			IFile f = p.getFile(new Path(s));
			String mapType = helper.preview(f, mapUsages);
			if (mapType == null) {
				// unsupported map format
				continue;
			}
			WESBMap map = allMaps.get(s);
			if (map == null) {
				map = new WESBMap();
				maps.getAllMaps().add(map);
			} else {
				allMaps.remove(s);
			}
			map.setMapType(mapType);
			map.setName(s);
			map.getUsages().clear();
		}

		maps.getAllMaps().removeAll(allMaps.values());

		calculateMapUsage(maps);

		selectMapBasedOnUsage(maps);
	}

	protected void selectMapBasedOnUsage(WESBMaps maps) {
		if (maps == null) {
			return;
		}
		HashMap<String, WESBMap> ms = new HashMap<String, WESBMap>();
		// select all main maps.
		for (WESBMap m : maps.getAllMaps()) {
			ms.put(m.getName(), m);
			if (m.getUsages().size() > 0) {
				for (String u : m.getUsages()) {
					if (!u.toLowerCase().endsWith(".map")) { //$NON-NLS-1$
						// main map reference
						m.setTobeConverted(true);
						break;
					}
				}
			}
		}
		// select referenced submaps
		do {
			boolean nothingProcessed = true;
			for (WESBMap m : maps.getAllMaps()) {
				if (m.isTobeConverted()) {
					continue;
				}
				if (m.getUsages().size() > 0) {
					for (String u : m.getUsages()) {
						if (!u.toLowerCase().endsWith(".map")) { //$NON-NLS-1$
							// referenced by a map.
							if (ms.get(u) != null && ms.get(u).isTobeConverted()) {
								m.setTobeConverted(true);
								nothingProcessed = false;
								break;
							}
						}
					}
				}
			}
			if (nothingProcessed) {
				break;
			}
		} while (true);
	}

	protected void calculateMapUsage(WESBMaps maps) {
	}

	protected void processProject(IProject p) {
		try {
			p.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (ConversionUtils.isJava(resource) || ConversionUtils.isJAR(resource)) {
						hasJava = true;
					} else if (ConversionUtils.isMap(resource)) {
						allMapFiles.add(resource.getProjectRelativePath().toString());
					} else if (ConversionUtils.isXSDOrWSDL(resource)) {
						hasXSDOrWSDL = true;
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
	}

	protected List<String> getApplicableLandingPoints() {
		List<String> landingPoints = new ArrayList<String>();
		landingPoints.add(IIB_LIBRARY);
		return landingPoints;
	}

	@Override
	public void convert(ConversionContext context) throws Exception {
		this.context = context;
		if (!context.resource.getProject().isAccessible()) {
			return;
		}
		String targetName = context.helper.getConvertedProjectName((IProject) context.resource);
		final IProject targetP = ConversionUtils.getProject(targetName);
		doConvert(targetP);
		addLogEntry(context.resource, targetP, context.log);
	}

	protected void doConvert(IProject targetP) throws Exception {
		if (targetP.exists()) {
			ConversionUtils.deleteProject(targetP);
		}

		targetP.create(context.monitor);
		targetP.open(context.monitor);

		context.log.addSourceToTargetResource(context.resource.getProject(), targetP.getProject());

		// configure natures
		configureNature(targetP, context.monitor);
		NatureConfiguration.configureProject(targetP);

		// add project references
		context.monitor.setTaskName(WESBConversionMessages.progressConfiguringProjectReference);
		IProject source = (IProject) context.resource;
		HashSet<IProject> referencedProjects = new HashSet<IProject>();
		for (IProject rp : source.getDescription().getReferencedProjects()) {
			if (WorkspaceHelper.hasReference(rp, source)) {
				throw new ESBConversionException(WESBConversionMessages.errorCircularProjectReferenceDetected);
			}
			if (ConversionUtils.isESBLib(rp)) {
				rp = ConversionUtils.getProject(context.helper.getConvertedProjectName(rp));
				referencedProjects.add(rp);
			} else {
				referencedProjects.add(rp);
			}
		}
		// Java classpath references
		for (IProject rp : ConversionUtils.getClassPathReference(source)) {
			if (rp != null && rp.exists() && rp.isAccessible() && ConversionUtils.isESBProject(rp)) {
				if (WorkspaceHelper.hasReference(rp, source)) {
					throw new ESBConversionException(WESBConversionMessages.errorCircularProjectReferenceDetected);
				}
				if (ConversionUtils.isESBLib(rp)) {
					rp = ConversionUtils.getProject(context.helper.getConvertedProjectName(rp));
					referencedProjects.add(rp);
				} else {
					referencedProjects.add(rp);
				}
			}
		}

		IProjectDescription desc = targetP.getDescription();
		desc.setReferencedProjects(referencedProjects.toArray(new IProject[0]));
		targetP.setDescription(desc, new NullProgressMonitor());

		targetP.getDescription().setReferencedProjects(referencedProjects.toArray(new IProject[0]));

		context.monitor.setTaskName(WESBConversionMessages.progressConvertingSchema);
		XSDAndWSDLConverterHelper schemaHelper = new XSDAndWSDLConverterHelper(context, (IProject) context.resource, targetP);
		schemaHelper.copyAllXSDandWSDLFiles();
		try
		{
			schemaHelper.convert();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			context.log.addEntry(targetP, new TodoEntry (NLS.bind(WESBConversionMessages.todoSchemaProblems, targetP.getName())));
		}
		

		if (((WESBProject) context.wesbResource).getJavas() != null) {
			context.monitor.setTaskName(WESBConversionMessages.progressConvertingJava);
			JavaConverterHelper javaHelper = new JavaConverterHelper(context, (IProject) context.resource, targetP);
			try
			{
				javaHelper.convert();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				context.log.addEntry(targetP, new TodoEntry (NLS.bind(WESBConversionMessages.todoJavaProblems, targetP.getName())));
			}
		}

		if (((WESBProject) context.wesbResource).getMaps() != null) {
			context.monitor.setTaskName(WESBConversionMessages.progressConvertingMap);
			MapConverterHelper mapHelper = new MapConverterHelper(context);
			for (WESBMap map : ((WESBProject) context.wesbResource).getMaps().getAllMaps()) {
				if (map.isTobeConverted()) {
					IProject project = (IProject) context.resource;
					context.resource = project.getFile(new Path(map.getName()));
					mapHelper.convert();
					context.resource = project;
				}
			}
		}

		targetP.refreshLocal(IResource.DEPTH_INFINITE, context.monitor);
		context.resourceSet.getResources().clear();
	}

	protected void addLogEntry(IResource resource, IProject targetP, ConversionLog userLog) throws CoreException {
		userLog.addEntry(
				targetP,
				new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoConversionMessage_ESBLib, resource.getName(),
						targetP.getName())));
	}

	protected void configureNature(IProject targetP, IProgressMonitor monitor) throws CoreException {
		WorkspaceHelper.addProjectNature(monitor, WMQIConstants.LIBRARY_NATURE_ID, targetP);
		WorkspaceHelper.addProjectNature(monitor, WMQIConstants.MESSAGE_BROKER_PROJECT_NATURE_ID, targetP);
	}

}
