/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1280083948" > 
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.Definition;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.util.NLS;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSchemaDirective;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.WSDLUtils;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.ErrorEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.uri.protocol.PlatformProtocol;
import com.ibm.etools.msg.coremodel.utilities.WorkbenchUtil;
import com.ibm.etools.msg.coremodel.utilities.report.IMSGReport;
import com.ibm.etools.msg.importer.wsdl.WsdlProvider;
import com.ibm.etools.msg.importer.wsdl.operation.NewMSDFromWSDLOperation;
import com.ibm.etools.msg.importer.wsdl.operation.RemoveUnusedXSDImportsFromWSDL;
import com.ibm.etools.msg.importer.wsdl.pages.WSDLImportOptions;
import com.ibm.etools.msg.msgmodel.utilities.importhelpers.XGenSchemaFile;
import com.ibm.etools.msg.msgmodel.utilities.importhelpers.XGenSchemaFileList;
import com.ibm.etools.msg.msgmodel.utilities.wsdlhelpers.DeployableWSDLHelper;
import com.ibm.etools.msg.msgmodel.utilities.xsdhelpers.XSDHelper;
import com.ibm.etools.msg.wsdl.helpers.WSDLBindingsHelper;
import com.ibm.etools.msg.wsdl.helpers.WSDLHelper;

/**
 * @author Zhongming Chen
 *
 */
public class XSDAndWSDLConverterHelper {

	private HashMap<String, IFile> fileNameToLocation = new HashMap<String, IFile>();
	private IProject sourceProject;
	private IProject targetProject;
	private ConversionContext context;

	public XSDAndWSDLConverterHelper(final ConversionContext context, IProject source, IProject target) {
		super();
		this.sourceProject = source;
		this.targetProject = target;
		this.context = context;

		fileNameToLocation.clear();
		for (IProject p : context.projects) {
			if (!p.isAccessible()) {
				continue;
			}
			try {
				p.accept(new IResourceVisitor() {

					@Override
					public boolean visit(IResource resource) throws CoreException {
						if (ConversionUtils.isXSDOrWSDL(resource)) {
							context.monitor.subTask(WESBConversionMessages.progressConverting + resource.getFullPath().toString());
							String namespace = ""; //$NON-NLS-1$
							if (ConversionUtils.isXSD(resource)) {
								XSDSchema schema = XSDHelper.loadXSD((IFile) resource, context.resourceSet);
								if (schema != null) {
									namespace = schema.getTargetNamespace();
								}
							} else {
								// WSDL
								Definition def = WSDLUtils.getDefinition((IFile) resource, context.resourceSet);
								if (def != null) {
									namespace = def.getTargetNamespace();
								}
							}
							fileNameToLocation.put(encodeFileLocationKey(namespace, resource.getName()), (IFile) resource);
						}
						return true;
					}
				});
			} catch (CoreException e) {
				context.log.addEntry(p, new ErrorEntry(ConversionUtils.getExceptionMessage(e)));
			}
		}
	}

	public static String encodeFileLocationKey(String namespace, String name) {
		return namespace + "^^" + name; //$NON-NLS-1$
	}

	public void copyAllXSDandWSDLFiles() throws CoreException {
		sourceProject.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFolder) {
					if (!targetProject.getFolder(resource.getProjectRelativePath()).exists()) {
						targetProject.getFolder(resource.getProjectRelativePath()).create(false, true, new NullProgressMonitor());
					}
				} else if (ConversionUtils.isXSDOrWSDL(resource)) {
					ConversionUtils.copy((IFile) resource, targetProject.getFile(resource.getProjectRelativePath()));
				}
				return true;
			}
		});
	}

	public void convert() throws Exception {
		sourceProject.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (ConversionUtils.isXSD(resource)) {
					convertXSD((IFile) resource, targetProject.getFile(resource.getProjectRelativePath()));
				} else if (ConversionUtils.isWSDL(resource)) {
					convertWSDL((IFile) resource, targetProject.getFile(resource.getProjectRelativePath()));
				}
				return true;
			}
		});

		context.resourceSet.getResources().clear();
	}

	protected void convertWSDL(IFile source, IFile file) {
		try {
			WSDLHelper wsdlHelper = WSDLHelper.getInstance();
			Definition sourceDef = wsdlHelper.loadWSDLFile(source);
			Map bindings = sourceDef.getBindings();
			for (Object b : bindings.values()) {
				Binding binding = (Binding) b;
				String style = DeployableWSDLHelper.getStyle((Binding) binding);
				boolean encodedUse = DeployableWSDLHelper.hasEncodedUse(binding);
				if (!style.equals(DeployableWSDLHelper.DOCUMENT_STYLE) || encodedUse) {
					// WSDL style not supported. Remove output from target
					// project.
					context.log.addEntry(
							file.getProject(),
							new TodoEntry(NLS.bind(WESBConversionMessages.todoNonDCWWSDLDetected, file.getProjectRelativePath()
									.toString())));
					file.delete(true, new NullProgressMonitor());
					return;
				}
			}

			IProject targetProject = file.getProject();
			IFile targetFile = file;
			HashSet<String> existingResources = new HashSet<String>();
			for (Resource r : context.resourceSet.getResources()) {
				existingResources.add(r.getURI().toString());
			}
			Definition def = WSDLUtils.getDefinition(targetFile, context.resourceSet);

			WSDLUtils.updateWSDLImportLocation(targetFile.getParent(), (org.eclipse.wst.wsdl.Definition) def, context.helper,
					fileNameToLocation);
			WSDLUtils.updateWSDLInlineSchemaImportLocation(targetFile.getParent(), (org.eclipse.wst.wsdl.Definition) def,
					context.helper, fileNameToLocation);

			((org.eclipse.wst.wsdl.Definition) def).eResource().save(Collections.EMPTY_MAP);

			WSDLImportOptions opt = setupOptions(targetProject, targetFile, def, true);
			WsdlProvider wizardShell = new WsdlProvider();
			wizardShell.setWSDLImportOptions(opt);
			wizardShell.setIsCreatingMessageModel(true);
			IMSGReport report = wizardShell.initializeReport();
			importWSDL(report, opt);
			context.log.addEntry(targetFile,
					new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoWsdlHandler_import, targetFile.getName()), null));

			for (Resource r : context.resourceSet.getResources()) {
				if (!existingResources.contains(r.getURI().toString())) {
					IFile addedResource = PlatformProtocol.getWorkspaceFile(r.getURI());
					if (addedResource != null && addedResource.getFullPath().toString().indexOf("InlineSchema") > 0) { //$NON-NLS-1$
						context.log.addSourceToTargetResource(source, addedResource);
					}
				}
			}
			context.log.addSourceToTargetResource(source, targetFile);
		} catch (Exception e) {
			context.log.addEntry(file, new ErrorEntry(ConversionUtils.getExceptionMessage(e)));
		}
	}

	private void importWSDL(IMSGReport msgReport, WSDLImportOptions opt) {
		opt.setToImportResource(false);
		IProgressMonitor monitor = new NullProgressMonitor();

		try {
			WSDLHelper.getInstance().setKeepOriginalFolderStructure(true);
			NewMSDFromWSDLOperation genOp = new NewMSDFromWSDLOperation(msgReport, ((WSDLImportOptions) opt)) {
				@Override
				protected void generate(IProgressMonitor monitor) {
					// super.generate(monitor);
					XGenSchemaFile xSchema;
					for (Iterator<XGenSchemaFile> it = fSchemaList.getAll().iterator(); it.hasNext();) {
						xSchema = (XGenSchemaFile) it.next();
						String file = xSchema.getSerializedFileName();
						// only serialize generated inline schema for the
						// current WSDL.
						if (xSchema.isEmittable()
								&& file.startsWith(fImportOptions.getSourceFile().getFullPath().removeFileExtension().lastSegment()
										+ "_InlineSchema")) { //$NON-NLS-1$
							context.log.addEntry(
									xSchema.getSerializedFile(),
									new ConversionLogEntry(NLS.bind(
											WESBConversionMessages.infoWsdlHandler_inline_schema_externalized, fImportOptions
													.getSourceFile().getName(), xSchema.getSerializedFile().getName())));
							serialize(xSchema, xSchema.getSerializedFile());
							monitor.worked(1);
						}
					}
				}
			};

			try {
				genOp.run(monitor);
			} catch (Throwable ex) {
			}

			RemoveUnusedXSDImportsFromWSDL removeOp = new RemoveUnusedXSDImportsFromWSDL(msgReport, ((WSDLImportOptions) opt));
			removeOp.run(monitor);

			WSDLUtils.updateImportedWSDL(opt, context.helper, fileNameToLocation);

		} catch (Exception e) {
			context.log.addEntry(null, new ErrorEntry(ConversionUtils.getExceptionMessage(e)));
		} finally {
			WSDLHelper.getInstance().setKeepOriginalFolderStructure(false);
		}

	}

	protected void convertXSD(IFile source, IFile file) {
		XSDSchema xsdSchema = XSDHelper.loadXSD(file, context.resourceSet);

		updateImportAndInclude(xsdSchema, file);

		try {
			xsdSchema.eResource().save(Collections.EMPTY_MAP);
			context.log.addSourceToTargetResource(source, file);
		} catch (IOException e) {
			context.log.addEntry(
					file,
					new ErrorEntry(NLS.bind(WESBConversionMessages.errorOnSave, file.getFullPath().toString(),
							e.getLocalizedMessage())));
		}
	}

	private void updateImportAndInclude(XSDSchema xsdSchema, IFile file) {
		for (XSDSchemaContent content : xsdSchema.getContents()) {
			if (content instanceof XSDSchemaDirective) {
				XSDSchemaDirective e = (XSDSchemaDirective) content;
				String location = e.getSchemaLocation();
				if (location == null || WSDLUtils.isRemoteSchema(location)) {
					continue;
				}
				IPath path = new Path(location);
				String fileName = path.lastSegment();
				String ns = ""; //$NON-NLS-1$
				if (e.getResolvedSchema() != null) {
					ns = e.getResolvedSchema().getTargetNamespace();
				} else {
					ns = e.getElement().getAttribute("namespace"); //$NON-NLS-1$
				}
				IFile actualLocation = fileNameToLocation.get(encodeFileLocationKey(ns, fileName));
				if (actualLocation != null) {
					if (actualLocation.getProject() != file.getProject()) {
						// cross project reference
						actualLocation = ConversionUtils.getProject(
								context.helper.getConvertedProjectName(actualLocation.getProject())).getFile(
								actualLocation.getProjectRelativePath());
						location = actualLocation.getFullPath().makeRelativeTo(file.getParent().getFullPath()).toString();
						if (location != null && !location.equals(e.getSchemaLocation())) {
							e.setSchemaLocation(location);
						}
					}
				}
			}
		}
	}

	private WSDLImportOptions setupOptions(IProject targetProject, IFile wsdlFile, Definition wsdlDef, boolean isReimpport)
			throws InvocationTargetException {
		try {
			WSDLHelper.getInstance().setKeepOriginalFolderStructure(true);

			WSDLImportOptions realOptions = new WSDLImportOptions();
			// This flag is for showing the drag and drop tip after importing
			// WSDL
			// file from WSDL importer wizard.
			// We don't need to show the tip, so set this flag to be true.
			realOptions.setExecutingTestCase(true);
			realOptions.setMsdFileName(wsdlFile.getName());
			realOptions.setToUseExternalResource(false);
			realOptions.setToUseRemoteResource(false);
			realOptions.setWSDLDefinition(wsdlDef);
			// Never save backup
			realOptions.setToImportResource(false);
			realOptions.setMessageBrokerProject(true);
			// Options specific for App/Lib
			realOptions.setSelectedProject(targetProject);
			realOptions.setSourceFile(wsdlFile);
			IContainer parent = wsdlFile.getParent();
			if (parent instanceof IFolder)
				realOptions.setSelectedFolder((IFolder) parent);

			// Analyze schemas (top level only. i.e. import statements ignored)
			XGenSchemaFileList schemaList = WSDLHelper.getInstance().getXGenSchemaFileList(realOptions.getWSDLDefinition(), null,
					targetProject, realOptions.getSelectedFolder(), false, isReimpport);
			// // Analyze schemas (top level only. i.e. import statements
			// ignored)
			// XGenSchemaFileList schemaList =
			// WSDLHelper.getInstance().traverse(realOptions.getWSDLDefinition(),
			// null, targetProject,
			// realOptions.getSelectedFolder(), false, isReimpport);
			// Update parsed files
			// System.out.println("------" + wsdlFile.getFullPath());
			// for (XGenSchemaFile f : schemaList.getAll()) {
			// System.out.println(f.getSerializedFileName());
			// }
			String fileName = WorkbenchUtil.getWSDLNameFromRemoteURL(wsdlDef.getDocumentBaseURI());

			IPath path = new Path(fileName);
			XGenSchemaFile fWsdlSchema = schemaList.getItem(path.lastSegment(), realOptions.getWSDLDefinition()
					.getTargetNamespace());
			realOptions.setWSDLDefinitionSchema(fWsdlSchema);
			realOptions.setSchemaList(schemaList);
			// Inject folder
			// realOptions.updateFolderForMessageModel();
			realOptions.updateResourceCopyMap();

			// Set bindings to import
			Vector<javax.wsdl.Binding> allBindings = WSDLBindingsHelper.getInstance().getAllAvailableBindings(
					realOptions.getWSDLDefinition());
			if (!allBindings.isEmpty()) {
				List<String> allBindingsLocaNames = new ArrayList<String>();
				for (javax.wsdl.Binding aBinding : allBindings) {
					allBindingsLocaNames.add(aBinding.getQName().getLocalPart());
				}
				realOptions.setSelectedBindings(allBindingsLocaNames);
			} else {
				realOptions.setIgnoreNoBinding(true);
			}

			return realOptions;
		} finally {
			WSDLHelper.getInstance().setKeepOriginalFolderStructure(false);
		}
	}

}
