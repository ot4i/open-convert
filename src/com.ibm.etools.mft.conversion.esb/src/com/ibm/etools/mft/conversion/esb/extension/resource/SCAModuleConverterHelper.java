/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3890155984" > 
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.wsdl.Port;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.broker.config.appdev.IntegrationService;
import com.ibm.broker.config.appdev.IntegrationServiceFlow;
import com.ibm.broker.config.appdev.IntegrationServiceOperation;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.SubFlowNode;
import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.InputNode;
import com.ibm.broker.config.appdev.nodes.LabelNode;
import com.ibm.broker.config.appdev.nodes.OutputNode;
import com.ibm.broker.config.appdev.nodes.SOAPExtractNode;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.ESBConversionException;
import com.ibm.etools.mft.conversion.esb.FlowResource;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.mediationprimitive.XSLTransformationConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.BindingManager;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.DefaultMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IPrimitiveConverter.ConverterContext;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.BindingConverter;
import com.ibm.etools.mft.conversion.esb.model.ExportResource;
import com.ibm.etools.mft.conversion.esb.model.ImportResource;
import com.ibm.etools.mft.conversion.esb.model.MFCComponentResource;
import com.ibm.etools.mft.conversion.esb.model.PrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.WESBResource;
import com.ibm.etools.mft.conversion.esb.model.mfc.ErrorFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Flow;
import com.ibm.etools.mft.conversion.esb.model.mfc.InputTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Interface;
import com.ibm.etools.mft.conversion.esb.model.mfc.MediationFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.ObjectFactory;
import com.ibm.etools.mft.conversion.esb.model.mfc.Operation;
import com.ibm.etools.mft.conversion.esb.model.mfc.OperationFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.RequestFlow;
import com.ibm.etools.mft.conversion.esb.model.mfc.ResponseFlow;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLogEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.logicalmodelhelpers.WorkspaceHelper;
import com.ibm.etools.mft.uri.protocol.PlatformProtocol;
import com.ibm.etools.mft.util.WMQIConstants;
import com.ibm.wbit.model.utils.wsdl.WSDLUtils;
import com.ibm.websphere.sca.scdl.Reference;
import com.ibm.ws.ffdc.FFDCFilter;
import com.ibm.ws.sibx.scax.mediation.model.ComponentFlows;
import com.ibm.ws.sibx.scax.mediation.model.loader.EFlowSimplifier;
import com.ibm.ws.sibx.scax.mediation.model.loader.FlowModelLoader;
import com.ibm.ws.sibx.scax.mediation.model.xml.loader.XMLMedflowModelLoaderUtil;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.Component;
import com.ibm.wsspi.sca.scdl.Displayable;
import com.ibm.wsspi.sca.scdl.Export;
import com.ibm.wsspi.sca.scdl.ExportBinding;
import com.ibm.wsspi.sca.scdl.Import;
import com.ibm.wsspi.sca.scdl.Part;
import com.ibm.wsspi.sca.scdl.ReferenceSet;
import com.ibm.wsspi.sca.scdl.SCDLFactory;
import com.ibm.wsspi.sca.scdl.Wire;
import com.ibm.wsspi.sca.scdl.impl.NativeExportBindingImpl;
import com.ibm.wsspi.sca.scdl.impl.ReferenceImpl;
import com.ibm.wsspi.sca.scdl.jaxws.JaxWsExportBinding;
import com.ibm.wsspi.sca.scdl.mfc.MediationFlowFactory;
import com.ibm.wsspi.sca.scdl.mfc.MediationFlowImplementation;
import com.ibm.wsspi.sca.scdl.webservice.WebServiceExportBinding;
import com.ibm.wsspi.sca.scdl.wsdl.WSDLPortType;

/**
 * @author Zhongming Chen
 *
 */
public class SCAModuleConverterHelper implements WESBConversionConstants {

	public static XMLMedflowModelLoaderUtil medflowUtil = new XMLMedflowModelLoaderUtil();

	public class NodeObject {
		public Element owner;
		public URI href;
		public String type;
		public String xsiType;
		public String fragment;
		public Object root;
		public Object implementation;
		public IFile owningFile;
		public IFile implFile;
		public Object implObject;

		public NodeObject(Element e, IResource source, ResourceSet rs, boolean copyToTarget) throws Exception {
			this.owner = e;
			List<Element> es = ConversionUtils.getImmediateChild(e, "", "extendedObject"); //$NON-NLS-1$ //$NON-NLS-2$
			if (es.size() > 0) {
				URI u = URI.createURI(es.get(0).getAttribute("href")); //$NON-NLS-1$
				IFile file = source.getProject().getFile(u.path());
				this.owningFile = file;
				fragment = u.fragment();
				href = URI.createPlatformResourceURI(owningFile.getFullPath().toString());
			}
			es = ConversionUtils.getImmediateChild(e, "", "extensionObject"); //$NON-NLS-1$ //$NON-NLS-2$
			if (es.size() > 0) {
				type = es.get(0).getAttribute("type"); //$NON-NLS-1$
				xsiType = ConversionUtils.getXSIType(es.get(0));
				if (!ConversionUtils.hasValue(xsiType)) {
					// try xmi:type
					xsiType = ConversionUtils.getXMIType(es.get(0));
				}
			}
			if (isExport() || isImport()) {
				try {
					XMIResource r = (XMIResource) rs.getResource(href, true);
					root = r.getEObject(fragment);
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			} else if (isComponent()) {
				XMIResource r = (XMIResource) rs.getResource(href, true);
				root = r.getEObject(fragment);
				if (root instanceof Component) {
					implementation = ((Component) root).getImplementation();
					if (implementation instanceof MediationFlowImplementation) {
						IFile sourceFile = source.getProject().getFile(((MediationFlowImplementation) implementation).getMfcFile());
						implFile = sourceFile;
						String filePath = sourceFile.getLocation().toString();
						if (medflowUtil.isReadableFormat(filePath)) {
							implObject = ConversionUtils.loadModel(ConversionUtils.getContent(implFile),
									ConversionUtils.PACKAGE_MFC);
							combineMediationFlowsIfTheyAreSplitted(implFile, implObject);
						} else {
							try {
								FFDCFilter.missingMedNodes.clear();
								Resource res = FlowModelLoader.loadEflowModel(URI
										.createFileURI(sourceFile.getLocation().toString()).toString(), context.log.getModelFile()
										.getParent());
								ComponentFlows compFlows = EFlowSimplifier.getComponentFlows("M1", ((Component) root).getName(), //$NON-NLS-1$
										res.getContents());
								implObject = ConversionUtils.convertFromOldModelToNewModel(compFlows);
							} finally {
								Resource.Factory msgflowFactory = Resource.Factory.Registry.INSTANCE.getFactory(URI
										.createURI("a.msgflow")); //$NON-NLS-1$
								Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("subflow", msgflowFactory); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}

		public NodeObject() {
		}

		public boolean isExport() {
			return "com.ibm.wbit.wiring.ui.ext.model:ExportExtension".equals(xsiType); //$NON-NLS-1$
		}

		public boolean isImport() {
			return "com.ibm.wbit.wiring.ui.ext.model:NodeExtension".equals(xsiType) && href.path().endsWith(".import"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		public boolean isComponent() {
			return "com.ibm.wbit.wiring.ui.ext.model:NodeExtension".equals(xsiType) && !href.path().endsWith(".import"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		public boolean isMFCComponent() {
			return implementation != null && (implementation instanceof MediationFlowImplementation);
		}

		public void combineMediationFlowsIfTheyAreSplitted(IFile mfcFile, Object implObject) throws Exception {
			if (!(implObject instanceof MediationFlow)) {
				return;
			}
			MediationFlow controlFlow = (MediationFlow) implObject;
			if (!controlFlow.isMultipleFiles()) {
				return;
			}
			for (com.ibm.etools.mft.conversion.esb.model.mfc.Import imp : controlFlow.getImport()) {
				String loc = imp.getLocation();
				if (loc.endsWith(".mfcflow")) { //$NON-NLS-1$
					boolean found = false;
					IFile mfcFlowFile = mfcFile.getProject().getFile(loc);
					StringTokenizer st = new StringTokenizer(mfcFlowFile.getName(), "_"); //$NON-NLS-1$
					st.nextToken();
					String interfaceName = st.nextToken();
					String opName = st.nextToken();
					if (opName.endsWith(".mfcflow")) { //$NON-NLS-1$
						opName = opName.substring(0, opName.length() - ".mfcflow".length()); //$NON-NLS-1$
					}
					MediationFlow mfcFlow = (MediationFlow) ConversionUtils.loadModel(ConversionUtils.getContent(mfcFlowFile),
							ConversionUtils.PACKAGE_MFC);
					for (Interface in : controlFlow.getInterface()) {
						if (interfaceName.equals(in.getPortType().getLocalPart())) {
							for (Operation op : in.getOperation()) {
								if (op.getName().equals(opName)) {
									for (JAXBElement<? extends Flow> f : mfcFlow.getFlow()) {
										if (f.getValue() instanceof RequestFlow) {
											op.setRequestFlow((RequestFlow) f.getValue());
										} else if (f.getValue() instanceof ResponseFlow) {
											op.setResponseFlow((ResponseFlow) f.getValue());
										} else if (f.getValue() instanceof ErrorFlow) {
											op.setErrorFlow((ErrorFlow) f.getValue());
										}
										found = true;
									}
									break;
								}
							}
							break;
						}
					}
					if (!found) {
						found = found;
					}
				}
			}
		}

	}

	private List<NodeObject> exports = new ArrayList<NodeObject>();
	private List<NodeObject> imports = new ArrayList<NodeObject>();
	private HashMap<Binding, Nodes> bindingToNodes = new HashMap<Binding, Nodes>();
	private HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> primitiveToNodes = new HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes>();
	private List<NodeObject> components = new ArrayList<NodeObject>();
	private FlowResourceManager flowManager = new FlowResourceManager();
	private ConversionContext context;
	private boolean landingOnService;
	private IProject targetProject;
	private IProject sourceProject;
	private IntegrationService brokerService;

	public SCAModuleConverterHelper(ConversionContext context) {
		this.context = context;
		WESBProject esbp = ConversionUtils.getESBProject(context);
		landingOnService = WESBModuleConverter.IIB_SERVICE.equals(esbp.getLandingPoint());
		sourceProject = context.resource.getProject();
		targetProject = ConversionUtils.getProject(context.helper.getConvertedProjectName(context.resource.getProject()));
	}

	public WESBResource preview(WESBConversionDataType model, WESBResource resource, HashMap<String, List<String>> mapUsages) {
		IFile moduleFile = context.resource.getProject().getFile(new Path("sca.modulex")); //$NON-NLS-1$
		if (resource == null) {
			resource = new SCAModule();
		}
		SCAModule module = (SCAModule) resource;

		try {
			loadModule(moduleFile, context.resourceSet, false);
		} catch (Throwable e) {
			return module;
		}

		String msg = checkSupported();
		if (msg != null) {
			// unsupported
			module.setErrorMessage(msg);
			return module;
		}

		HashMap<String, List<String>> bindingUsages = new HashMap<String, List<String>>();

		HashSet<Object> toRemove = new HashSet<Object>(module.getChildren());
		for (NodeObject o : exports) {
			ExportResource exportResource = (ExportResource) ConversionUtils.getObjectForClassAndName(module.getChildren(),
					ExportResource.class, ((Export) o.root).getDisplayName(), "name"); //$NON-NLS-1$
			if (exportResource == null) {
				exportResource = new ExportResource();
				exportResource.setName(((Export) o.root).getDisplayName());
				module.getChildren().add(exportResource);
			}
			if (((Export) o.root).getBinding() != null) {
				ConversionUtils
						.addUsage(bindingUsages, o.owningFile.getName(), ((Export) o.root).getBinding().getClass().getName());
			} else {
				// SCA binding
				ConversionUtils.addUsage(bindingUsages, o.owningFile.getName(), NativeExportBindingImpl.class.getName());
			}
			toRemove.remove(exportResource);
		}

		ConversionUtils.addUsages(bindingUsages, model.getGlobalConfiguration().getBindingConverters(), BindingConverter.class,
				"type", true); //$NON-NLS-1$

		HashMap<String, List<String>> mpUsages = new HashMap<String, List<String>>();
		for (NodeObject o : components) {
			if (o.isMFCComponent()) {
				MFCComponentResource r = (MFCComponentResource) ConversionUtils.getObjectForClassAndName(module.getChildren(),
						MFCComponentResource.class, ((Component) o.root).getDisplayName(), "name"); //$NON-NLS-1$
				if (r == null) {
					r = new MFCComponentResource();
					r.setName(((Component) o.root).getDisplayName());
					module.getChildren().add(r);
				}
				toRemove.remove(r);
				previewMediationFlow(mpUsages, mapUsages, o.owningFile.getName(), o.implObject);
			}
		}

		ConversionUtils.addUsages(mpUsages, model.getGlobalConfiguration().getPrimitiveConverters(), PrimitiveConverter.class,
				"type", true); //$NON-NLS-1$

		bindingUsages.clear();

		for (NodeObject o : imports) {
			if (o.root == null) {
				continue;
			}
			ImportResource r = (ImportResource) ConversionUtils.getObjectForClassAndName(module.getChildren(),
					ImportResource.class, ((Import) o.root).getDisplayName(), "name"); //$NON-NLS-1$
			if (r == null) {
				r = new ImportResource();
				r.setName(((Import) o.root).getDisplayName());
				module.getChildren().add(r);
			}
			toRemove.remove(r);

			if (((Import) o.root).getBinding() != null) {
				ConversionUtils
						.addUsage(bindingUsages, o.owningFile.getName(), ((Import) o.root).getBinding().getClass().getName());
			}
		}

		ConversionUtils.addUsages(bindingUsages, model.getGlobalConfiguration().getBindingConverters(), BindingConverter.class,
				"type", true); //$NON-NLS-1$

		module.getChildren().removeAll(toRemove);

		return module;
	}

	protected void previewMediationFlow(HashMap<String, List<String>> mpUsage, HashMap<String, List<String>> mapUsages,
			String mfcName, Object mfcModel) {
		MediationFlow flow = (MediationFlow) mfcModel;
		for (Interface interfaze : flow.getInterface()) {
			for (Operation op : interfaze.getOperation()) {
				previewOperationFlow(mpUsage, mapUsages, mfcName, op.getRequestFlow());
				previewOperationFlow(mpUsage, mapUsages, mfcName, op.getErrorFlow());
				previewOperationFlow(mpUsage, mapUsages, mfcName, op.getResponseFlow());
			}
		}
	}

	protected void previewOperationFlow(HashMap<String, List<String>> mpUsage, HashMap<String, List<String>> mapUsages,
			String mfcName, OperationFlow flow) {
		if (flow == null) {
			return;
		}
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node n : flow.getNode()) {
			String type = n.getType();
			if (XSLTransformationConverter.TYPE.equals(type)) {
				Property p = (Property) PrimitiveManager.getProperty(n, "XMXMap"); //$NON-NLS-1$
				if (p != null) {
					String mapFile = p.getValue();
					ConversionUtils.addUsage(mapUsages, mfcName, mapFile);
				}
			}
			ConversionUtils.addUsage(mpUsage, mfcName, type);
		}
	}

	protected String checkSupported() {
		StringBuffer sb = new StringBuffer();
		if (exports.size() == 0) {
			sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorNoExport, sourceProject.getName())));
		}
		if (exports.size() > 1) {
			sb.append(ConversionUtils.addHTMLBullet(WESBConversionMessages.errorMoreThanOneExport));
		}
		if (components.size() > 1) {
			sb.append(ConversionUtils.addHTMLBullet(WESBConversionMessages.errorMoreThanOneComponent));
		}
		Export ex = null;
		Component com = null;
		if (exports.size() == 1) {
			NodeObject e = exports.get(0);
			ex = (Export) e.root;
			if (ex == null) {
				sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorNoExport, sourceProject.getName())));
			} else {
				if (ex.getInterfaceSet() != null && ex.getInterfaceSet().getInterfaces().size() > 1) {
					sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorMoreThanOneInterfaceInExport,
							ex.getDisplayName())));
				}
				if (!isSupportedBinding(ex.getBinding())) {
					sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorUnsupportedExportBinding,
							ex.getDisplayName(), ex.getBinding() != null ? ex.getBinding().getClass().getName() : "SCA Binding"))); //$NON-NLS-1$
				}
			}
		}
		if (components.size() == 1) {
			NodeObject c = components.get(0);
			com = (Component) c.root;
			if (!c.isMFCComponent()) {
				sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorNonMFCComponent, com.getDisplayName())));
			}
			if (com.getInterfaceSet() != null && com.getInterfaceSet().getInterfaces().size() > 1) {
				sb.append(ConversionUtils.addHTMLBullet(NLS.bind(
						WESBConversionMessages.errorMoreThanOneInterfaceInComponentInterfaces, com.getDisplayName())));
			}
			for (Object o : com.getReferences()) {
				if (o instanceof Reference) {
					Reference r = (Reference) o;
					if ("0..n".equals(r.getMultiplicity())) { //$NON-NLS-1$
						sb.append(ConversionUtils.addHTMLBullet(NLS.bind(
								WESBConversionMessages.errorComponentReferenceMultiplicity, com.getDisplayName(), r.getName())));
					}
				}
			}
		}
		if (ex != null && com != null && ex.getTargetName() != null && !ex.getTargetName().equals(getTargetComponentName(com))) {
			sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorComponentIsNotWiredToExport,
					ex.getDisplayName(), com.getDisplayName())));
		}
		for (NodeObject no : imports) {
			Import im = (Import) no.root;
			if (im == null) {
				continue;
			}
			if (im.getInterfaceSet() != null && im.getInterfaceSet().getInterfaces().size() > 1) {
				sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorMoreThanOneInterfaceInImport,
						im.getDisplayName())));
			}
			if (!isSupportedBinding(im.getBinding())) {
				sb.append(ConversionUtils.addHTMLBullet(NLS.bind(WESBConversionMessages.errorUnsupportedImportBinding,
						im.getDisplayName(), im.getBinding() != null ? im.getBinding().getClass().getName() : "SCA Binding"))); //$NON-NLS-1$
			}
		}

		if (sb.length() > 0) {
			return "<ul>" + sb.toString() + "</ul>"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return null;
		}
	}

	public String getTargetComponentName(Component com) {
		String location = null;
		try {
			location = PlatformProtocol.getWorkspaceFile(com.eResource().getURI()).getParent().getProjectRelativePath().toString();
		} catch (Throwable e){
			// do nothing
		}
		return (ConversionUtils.hasValue(location))? (location + "/" + com.getDisplayName()) : com.getDisplayName();
	}

	protected boolean isSupportedBinding(Binding binding) {
		// TODO: check against registry.
		return true;
		// return binding != null &&
		// BindingManager.getBindingConverter(binding.getClass().getName()) !=
		// null;
	}

	public void convert() throws Exception {
		context.monitor.setTaskName(WESBConversionMessages.progressConvertingSCAModule);

		IFile moduleFile = context.resource.getProject().getFile(new Path("sca.modulex")); //$NON-NLS-1$
		loadModule(moduleFile, context.resourceSet, true);

		IProject targetProject = context.helper.getTargetFile(context.resource.getProject().getFile("dummy")).getProject(); //$NON-NLS-1$

		context.index(targetProject);

		String msg = checkSupported();
		if (msg != null) {
			// unsupported
			context.log.addEntry(targetProject, new TodoEntry(NLS.bind(WESBConversionMessages.todoUnsupportedScaModule, msg)));
			WorkspaceHelper.removeProjectNature(new NullProgressMonitor(), WMQIConstants.SERVICE_APPLICATION_NATURE_ID,
					targetProject);
			return;
		}

		if (landingOnService) {
			createServiceDescriptor(targetProject);
		}

		createMainFlow();

		if (brokerService != null) {
			IFile serviceDescriptor = targetProject.getFile("service.descriptor"); //$NON-NLS-1$
			brokerService.writeServiceDescriptor(serviceDescriptor.getLocation().toString());
			serviceDescriptor.refreshLocal(1, new NullProgressMonitor());
		}
	}

	protected void createServiceDescriptor(IProject project) throws Exception {
		brokerService = new IntegrationService(project.getName());
		// IFile serviceDescriptor =
		// context.helper.getTargetFile(project.getFile("service.descriptor"));
		// URI uri =
		// URI.createPlatformResourceURI(serviceDescriptor.getFullPath().toString());
		// Resource resource = null;
		// if (serviceDescriptor.exists()) {
		// resource = context.resourceSet.getResource(uri, true);
		// try {
		// services = (com.ibm.etools.mft.service.model.Services)
		// resource.getContents().get(0);
		// } catch (Throwable e) {
		// services = ServiceFactory.eINSTANCE.createServices();
		// resource.getContents().clear();
		// resource.getContents().add(services);
		// }
		// } else {
		// resource = context.resourceSet.createResource(uri);
		// services = ServiceFactory.eINSTANCE.createServices();
		// resource.getContents().add(services);
		// }
		// resource.save(Collections.EMPTY_MAP);
	}

	protected void loadModule(IFile file, ResourceSet rs, boolean copyToTarget) throws Exception {
		exports.clear();
		imports.clear();
		components.clear();

		Document dom = ConversionUtils.loadXML(file.getContents());

		List<Element> maps = ConversionUtils.getImmediateChild(dom.getDocumentElement(), "http:///extensionmodel.ecore", //$NON-NLS-1$
				"ExtensionMap"); //$NON-NLS-1$

		for (Element e : maps) {
			if ("http://www.ibm.com/xmlns/prod/websphere/scdl/6.0.0".equals(e.getAttribute("namespace"))) { //$NON-NLS-1$ //$NON-NLS-2$
				List<Element> extensions = ConversionUtils.getImmediateChild(e, "", "extensions"); //$NON-NLS-1$ //$NON-NLS-2$
				for (Element e1 : extensions) {
					List<Element> extensionObjects = ConversionUtils.getImmediateChild(e1, "", "extensionObject"); //$NON-NLS-1$ //$NON-NLS-2$
					if (extensionObjects.size() > 0) {
						try {
							NodeObject o = new NodeObject(e1, file, rs, copyToTarget);
							if (o.isExport()) {
								exports.add(o);
							} else if (o.isImport()) {
								imports.add(o);
							} else if (o.isComponent()) {
								components.add(o);
							}
						} catch (Throwable ex) {
							// ignore this node.
							ex.printStackTrace();
							continue;
						}
					}
				}
			}
		}
	}

	protected void createMainFlow() throws Exception {
		context.log.addSourceToTargetResource(sourceProject.getFile("sca.module"), //$NON-NLS-1$
				targetProject.getFile(new Path(targetProject.getName() + WMQIConstants.MESSAGE_FLOW_EXTENSION)));

		MessageFlow mainFlow = PrimitiveManager.getOrCreateMessageFlow(context, flowManager, null, targetProject.getName(),
				WMQIConstants.MESSAGE_FLOW_EXTENSION);

		convertExports(mainFlow);

		convertWiresInAssemblyDiagram(mainFlow);

		context.monitor.setTaskName("Commiting message flows ..."); //$NON-NLS-1$
		flowManager.commit(context);
	}

	protected void convertWiresInAssemblyDiagram(MessageFlow mainFlow) throws Exception {
		for (NodeObject o : exports) {
			Export export = (Export) o.root;

			convertWireForExport(mainFlow, export);
		}
	}

	protected void convertWireForExport(MessageFlow mainFlow, Export export) throws Exception {
		String targetNodeName = export.getTargetName();

		if (export.getBinding() == null) {
			return;
		}

		IBindingConverter converter = BindingManager.getConverter(export.getBinding().getClass().getName(), context, context.model);

		if (converter == null) {
			return;
		}

		NodeObject targetNode = getComponentsOrImports(targetNodeName);
		if (targetNode == null) {
			return;
		}
		if (targetNode.root instanceof Component) {
			convertComponent(export, mainFlow, targetNode);
		} else if (targetNode.root instanceof Import) {
			// create dummy pass through component
			NodeObject no = createPassThroughComponent(export, targetNode);
			convertComponent(export, mainFlow, no);
			targetNode = no;
			targetNodeName = ((Component) no.root).getDisplayName();
		} else {
			return;
		}

		// Don't wire target
		// SubFlowNode target = (SubFlowNode)
		// mainFlow.getNodeByName(targetNodeName);
		// if (target == null) {
		// target = new SubFlowNode();
		// MessageFlow subFlow =
		// flowManager.getFlow(PrimitiveManager.getFullyQualifiedFlowName(null,
		// targetNodeName));
		// target.setSubFlow(subFlow);
		// target.setNodeName(targetNodeName);
		// mainFlow.addNode(target);
		// }
		// List<Object> upstreamNodes = new ArrayList<Object>();
		// upstreamNodes.add(export);
		// configureDownstreamNode(upstreamNodes, targetNode);
		//
		// // wire from export to component
		// mainFlow.connect((OutputTerminal) sourceTerminal,
		// target.getInputTerminals()[0]);
		//
		// // wire from component back to export
		// OutputTerminal inputResponseTerminal =
		// target.getOutputTerminal(target.getNodeName() + REPLY_NODE_SUFFIX);
		//
		// mainFlow.connect(inputResponseTerminal,
		// (com.ibm.broker.config.appdev.InputTerminal) replyTerminal);
	}

	private NodeObject createPassThroughComponent(Export export, NodeObject targetNode) {
		Import im = (Import) targetNode.root;

		// create component
		Component c = SCDLFactory.eINSTANCE.createComponent();
		c.setDisplayName(im.getDisplayName() + "_Passthrough"); //$NON-NLS-1$
		NodeObject no = new NodeObject();
		no.root = c;
		no.owningFile = targetNode.owningFile;
		no.implFile = targetNode.owningFile;
		no.implementation = MediationFlowFactory.eINSTANCE.createMediationFlowImplementation();
		MediationFlow mf = new MediationFlow();
		mf.setName(im.getDisplayName());
		no.implObject = mf;

		// create interface
		c.setInterfaceSet(im.getInterfaceSet());

		// create reference
		ReferenceSet refSet = SCDLFactory.eINSTANCE.createReferenceSet();
		c.setReferenceSet(refSet);
		ReferenceImpl ref = (ReferenceImpl) SCDLFactory.eINSTANCE.createReference();
		refSet.getReferences().add(ref);
		ref.setName("out"); //$NON-NLS-1$

		// wire to import
		Wire wire = SCDLFactory.eINSTANCE.createWire();
		wire.setTargetName(export.getTargetName());
		ref.getWires().add(wire);

		// create mediation flow
		for (Object o : c.getInterfaceSet().getInterfaces()) {
			if (o instanceof WSDLPortType) {
				WSDLPortType portType = (WSDLPortType) o;
				Interface theInterface = new Interface();
				mf.getInterface().add(theInterface);
				ObjectFactory factory = new ObjectFactory();
				if (portType.getPortType() instanceof QName) {
					QName qName = (QName) portType.getPortType();
					PortType realPortType = context.indexer.wsdlPortTypes.get(ConversionUtils.getQName(qName.getNamespaceURI(),
							qName.getLocalPart()));
					for (Object o1 : realPortType.getOperations()) {
						if (o1 instanceof javax.wsdl.Operation) {
							javax.wsdl.Operation sourceOp = (javax.wsdl.Operation) o1;
							Operation targetOp = new Operation();
							targetOp.setName(sourceOp.getName());
							theInterface.getOperation().add(targetOp);

							// create request flow
							RequestFlow requestFlow = new RequestFlow();
							targetOp.setRequestFlow(requestFlow);

							// create input node
							com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode = new com.ibm.etools.mft.conversion.esb.model.mfc.Node();
							inputNode.setType("Input"); //$NON-NLS-1$
							inputNode.setName("input"); //$NON-NLS-1$
							requestFlow.getNode().add(inputNode);

							// callout node
							com.ibm.etools.mft.conversion.esb.model.mfc.Node calloutNode = new com.ibm.etools.mft.conversion.esb.model.mfc.Node();
							calloutNode.setType("Callout"); //$NON-NLS-1$
							calloutNode.setName(targetOp.getName() + "_Callout"); //$NON-NLS-1$
							Property property = new Property();
							property.setName("operationName"); //$NON-NLS-1$
							property.setValue(sourceOp.getName());
							calloutNode.getAbstractProperty().add(factory.createAbstractProperty(property));
							property = new Property();
							property.setName("referenceName"); //$NON-NLS-1$
							property.setValue("out"); //$NON-NLS-1$
							calloutNode.getAbstractProperty().add(factory.createAbstractProperty(property));
							requestFlow.getNode().add(calloutNode);

							// wire input to callout
							InputTerminal in = new InputTerminal();
							in.setName("in"); //$NON-NLS-1$
							calloutNode.getInputTerminal().add(in);

							com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal out = new com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal();
							inputNode.getOutputTerminal().add(out);
							out.setName("out"); //$NON-NLS-1$

							com.ibm.etools.mft.conversion.esb.model.mfc.Wire w = new com.ibm.etools.mft.conversion.esb.model.mfc.Wire();
							w.setTargetNode(calloutNode.getName());
							w.setTargetTerminal("in"); //$NON-NLS-1$
							out.getWire().add(w);

							if (WSDLUtils.getOutputs((org.eclipse.wst.wsdl.Operation) sourceOp).size() > 0) {
								// 2 way op, create response flow
								ResponseFlow responseFlow = new ResponseFlow();
								targetOp.setResponseFlow(responseFlow);

								// input response node
								com.ibm.etools.mft.conversion.esb.model.mfc.Node inputResponseNode = new com.ibm.etools.mft.conversion.esb.model.mfc.Node();
								inputResponseNode.setType("InputResponse"); //$NON-NLS-1$
								inputResponseNode.setName("inputResponse"); //$NON-NLS-1$
								responseFlow.getNode().add(inputResponseNode);

								// call out response
								com.ibm.etools.mft.conversion.esb.model.mfc.Node calloutResponseNode = new com.ibm.etools.mft.conversion.esb.model.mfc.Node();
								calloutResponseNode.setType("CalloutResponse"); //$NON-NLS-1$
								calloutResponseNode.setName(targetOp.getName() + "_CalloutResponse"); //$NON-NLS-1$
								responseFlow.getNode().add(calloutResponseNode);

								// wire callout response to input response
								in = new InputTerminal();
								in.setName("in"); //$NON-NLS-1$
								inputResponseNode.getInputTerminal().add(in);

								out = new com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal();
								calloutResponseNode.getOutputTerminal().add(out);
								out.setName("out"); //$NON-NLS-1$

								w = new com.ibm.etools.mft.conversion.esb.model.mfc.Wire();
								w.setTargetNode(inputResponseNode.getName());
								w.setTargetTerminal("in"); //$NON-NLS-1$
								out.getWire().add(w);
							}
						}
					}
				}
			}
		}
		return no;
	}

	protected NodeObject getComponentsOrImports(String target) {
		if (target == null) {
			return null;
		}
		for (NodeObject o : components) {
			Component c = (Component) o.root;
			if (target.equals(c.getDisplayName())) {
				return o;
			}
		}
		for (NodeObject o : imports) {
			Import c = (Import) o.root;
			if (target.equals(c.getDisplayName())) {
				return o;
			}
		}
		return null;
	}

	protected NodeObject getNode(String targetNodeName) {
		for (NodeObject o : components) {
			if (o.root instanceof Component) {
				if (((Component) o.root).getDisplayName().equals(targetNodeName)) {
					return o;
				}
			}
		}
		return null;
	}

	protected void convertDownstreamNode(List<Object> upstreamNodes, NodeObject node) {
		if (node.root instanceof Component) {
			if (((Component) node.root).getImplementation() instanceof MediationFlowImplementation) {
				convertDownstreamMFC(upstreamNodes, node);
			}
		}
	}

	protected void convertDownstreamMFC(List<Object> upstreamNodes, NodeObject node) {
		Component component = (Component) node.root;
		MessageFlow componentFlow = flowManager
				.getFlow(ConversionUtils.getFullyQualifiedFlowName(null, component.getDisplayName()));

		SOAPExtractNode soapExtractNode = (SOAPExtractNode) componentFlow.getNodeByName(component.getDisplayName()
				+ ROUTE_TO_LABEL_SUFFIX);
		if (soapExtractNode != null) {
			Export export = (Export) upstreamNodes.get(0);
			if (export.getBinding() instanceof WebServiceExportBinding) {
				WebServiceExportBinding binding = (WebServiceExportBinding) export.getBinding();
				String qName = binding.getService().toString();
				Service service = context.indexer.wsdlServices.get(qName);
				javax.wsdl.Port port = service.getPort(ConversionUtils.getLocalPart(binding.getPort().toString()));
				soapExtractNode.setEnvelopeDestination("$LocalEnvironment/SOAP/Envelope/InRequest/" //$NON-NLS-1$
						+ port.getBinding().getQName().getLocalPart());
			}
		}

	}

	protected void convertComponent(Part upstreamedPart, MessageFlow mainFlow, NodeObject o) throws Exception {
		// don't create component flow
		// MessageFlow componentFlow =
		// PrimitiveManager.getOrCreateMessageFlow(context, flowManager, null,
		// ((Component) o.root).getDisplayName(),
		// WMQIConstants.MESSAGE_SUBFLOW_EXTENSION);
		//
		// context.userLog.addSourceToTargetResource(o.owningFile,
		// flowManager.getFlowResource(componentFlow));

		context.monitor.setTaskName(WESBConversionMessages.progressConvertingComponent);
		convertComponentFlow(upstreamedPart, mainFlow, o);
	}

	protected void convertComponentFlow(Part upstreamedPart, MessageFlow componentFlow, NodeObject nodeObject) throws Exception {
		int index = 0;
		Component c = (Component) nodeObject.root;

		flowManager.log(
				flowManager.getFlowResource(componentFlow),
				new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoMfcFlowConverted, c.getDisplayName(),
						componentFlow.getName())));

		boolean hasMultipleInterfaces = c.getInterfaceSet().getInterfaces().size() > 1;
		for (Object o : c.getInterfaceSet().getInterfaces()) {
			if (o instanceof WSDLPortType) {
				// don't create component flow
				// String nodeName = hasMultipleInterfaces ? (c.getDisplayName()
				// + INPUT_NODE_SUFFIX + "_" + (index++)) : (c
				// .getDisplayName() + INPUT_NODE_SUFFIX);
				// InputNode input = (InputNode)
				// PrimitiveManager.createNode(componentFlow, nodeName,
				// InputNode.class);
				//
				// nodeName = hasMultipleInterfaces ? (c.getDisplayName() +
				// INPUT_NODE_SUFFIX + "_" + (index++))
				// : (c.getDisplayName() + REPLY_NODE_SUFFIX);
				// OutputNode inputResponseNode = (OutputNode)
				// PrimitiveManager.createNode(componentFlow, nodeName,
				// OutputNode.class);

				if (nodeObject.isMFCComponent()) {
					MediationFlow mfc = (MediationFlow) nodeObject.implObject;
					for (Interface theInterface : mfc.getInterface()) {
						QName qname = theInterface.getPortType();
						String qnameStr = (qname != null) ? qname.toString() : null;
						PortType pt = context.indexer.wsdlPortTypes.get(qnameStr);
						// qname can be null for 6.2 and 7.0 flows, so pt can be
						// null
						if (pt == null) {
							// If there is only one PortType, use it
							Collection portTypes = context.indexer.wsdlPortTypes.values();
							if (portTypes.size() == 1) {
								pt = (PortType) (portTypes.toArray())[0];
							}
						}
						for (Operation op : theInterface.getOperation()) {

							flowManager.beginSnapshot();

							// create label node in main flow
							String opName = op.getName();
							String nodeName = mfc.getName() + "_" + opName; //$NON-NLS-1$
							LabelNode labelNode = (LabelNode) PrimitiveManager.createNode(componentFlow, nodeName, null,
									LabelNode.class, null); //$NON-NLS-1$
							labelNode.setLabelName(opName);

							// determine operation type, use the existence of a
							// response flow as the default
							boolean twoWay = (op.getResponseFlow() != null);
							// PortType is more accurate, so use it if possible
							if (pt != null) {
								javax.wsdl.Operation wsdlOp = pt.getOperation(opName, null, null);
								if (wsdlOp != null) {
									javax.wsdl.OperationType opType = wsdlOp.getStyle();
									twoWay = (wsdlOp.getStyle() == javax.wsdl.OperationType.REQUEST_RESPONSE || wsdlOp.getStyle() == javax.wsdl.OperationType.SOLICIT_RESPONSE);
								}
							}

							nodeName = nodeName + (twoWay ? REQUEST_RESPONSE_FLOW_SUFFIX : REQUEST_FLOW_SUFFIX);

							com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode = getInputNode(op.getRequestFlow().getNode());

							MessageFlow requestFlow = convertRequestFlow(upstreamedPart, c, op.getName(), inputNode, nodeName,
									op.getRequestFlow());

							// A two way flow may not have a WESB response flow,
							// WESB can send a response from the request flow
							if (twoWay && (op.getResponseFlow() != null)) {
								convertResponseFlow(upstreamedPart, c, op.getName(), inputNode, requestFlow, nodeName,
										op.getResponseFlow(), op.getRequestFlow());
							}

							for (FlowResource fr : flowManager.getDelta()) {
								context.log.addSourceToTargetResource(nodeObject.implFile, fr.file);
							}

							context.log.addSourceToTargetResource(nodeObject.implFile, flowManager.getFlowResource(requestFlow));

							SubFlowNode subFlowNode = (SubFlowNode) PrimitiveManager.createNode(componentFlow, nodeName, null,
									SubFlowNode.class, null);
							subFlowNode.setSubFlow(requestFlow);

							// connect label to request subflow
							if (subFlowNode.getInputTerminals().length > 0) {
								componentFlow.connect(labelNode.OUTPUT_TERMINAL_OUT, subFlowNode.getInputTerminals()[0]);
							}

							// if (subFlowNode.getInputTerminals().length > 0) {
							// // FIXME
							// componentFlow.connect(labelNode.OUTPUT_TERMINAL_OUT,
							// subFlowNode.getInputTerminals()[0]);
							// }
							// InputNode inputNodeInRequestFlow = null;
							// for (Node n : requestFlow.getNodes()) {
							// if (n instanceof InputNode) {
							// inputNodeInRequestFlow = (InputNode) n;
							// break;
							// }
							// }
							// if (inputNodeInRequestFlow != null &&
							// subFlowNode.getOutputTerminals().length > 0) {
							// componentFlow.connect(
							// subFlowNode.getOutputTerminal(inputNodeInRequestFlow.getNodeName()
							// + "Response"),
							// inputResponseNode.INPUT_TERMINAL_IN);
							// }

							// connect operation subflow to reply node
							// TODO: Currently assuming the upstreamed part is
							// an Export.
							IBindingConverter bindingConverter = BindingManager.getConverter(((Export) upstreamedPart).getBinding()
									.getClass().getName(), context, context.model);
							Nodes targetNodes = bindingToNodes.get(((Export) upstreamedPart).getBinding());
							Terminal targetTerminal = bindingConverter.getInputTerminal("", targetNodes); //$NON-NLS-1$

							if (subFlowNode.getOutputTerminals().length > 0) {
								// 2 way operation
								componentFlow.connect(subFlowNode.getOutputTerminals()[0],
										(com.ibm.broker.config.appdev.InputTerminal) targetTerminal);
							}

							if (brokerService != null) {
								IntegrationServiceOperation serviceOp = getServiceOperation(op.getName());
								if (serviceOp == null) {
									throw new ESBConversionException(NLS.bind(
											WESBConversionMessages.errorMultipleWSDLXSDWithSameNSAndLocalName, op.getName(),
											c.getName()));
								}
								serviceOp.getServiceFlows().clear();
								IntegrationServiceFlow flow = new IntegrationServiceFlow(flowManager.getFlowResource(requestFlow)
										.getProjectRelativePath().toString(),
										com.ibm.broker.config.appdev.IntegrationServiceFlow.FlowType.REQUEST_RESPONSE_TYPE);
								serviceOp.getServiceFlows().add(flow);
							}
						}
					}
				}
			}
		}
	}

	protected void convertResponseFlow(Part upstreamedPart, Component component, String operationName,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, MessageFlow flow, String nodeName,
			ResponseFlow responseFlow, RequestFlow requestFlow) throws Exception {
		flowManager.log(flowManager.getFlowResource(flow),
				new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoResponseFlowConverted, operationName, flow.getName())));

		convertMediationPrimitives(upstreamedPart, component, operationName, flow, inputNode, responseFlow.getNode(),
				WESBConversionConstants.SUFFIX_OF_MEDIATION_PRIMITIVE_IN_RESPONSE_FLOW, requestFlow, responseFlow);

		convertWires(flow, responseFlow, inputNode, responseFlow.getNode());
	}

	protected IntegrationServiceOperation getServiceOperation(String name) {
		for (IntegrationServiceOperation op : brokerService.getServiceOperations()) {
			if (op.getName().equals(name)) {
				return op;
			}
		}
		return null;
	}

	protected MessageFlow convertRequestFlow(Part upstreamedPart, Component component, String operationName,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, String nodeName, OperationFlow requestFlow)
			throws Exception {
		MessageFlow flow = PrimitiveManager.getOrCreateMessageFlow(context, flowManager, "gen", nodeName, //$NON-NLS-1$
				WMQIConstants.MESSAGE_SUBFLOW_EXTENSION);

		flowManager.log(flowManager.getFlowResource(flow),
				new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoRequestFlowConverted, operationName, flow.getName())));

		primitiveToNodes.clear();

		convertMediationPrimitives(upstreamedPart, component, operationName, flow, inputNode, requestFlow.getNode(), null, null,
				requestFlow);

		convertWires(flow, requestFlow, inputNode, requestFlow.getNode());

		return flow;
	}

	protected com.ibm.etools.mft.conversion.esb.model.mfc.Node getInputNode(
			List<com.ibm.etools.mft.conversion.esb.model.mfc.Node> nodes) {
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node n : nodes) {
			if (n.getType().equals("Input")) { //$NON-NLS-1$
				return n;
			}
		}
		return null;
	}

	protected void convertWires(MessageFlow flow, com.ibm.etools.mft.conversion.esb.model.mfc.Flow mediationFlow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, List<com.ibm.etools.mft.conversion.esb.model.mfc.Node> nodes)
			throws Exception {
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive : nodes) {
			convertWire(flow, mediationFlow, inputNode, primitive);
		}
	}

	protected void convertWire(MessageFlow flow, com.ibm.etools.mft.conversion.esb.model.mfc.Flow mediationFlow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive)
			throws Exception {
		IPrimitiveConverter handler = context.getPrimitiveConverter(primitive.getType());
		handler.convertWire(flow, mediationFlow, inputNode, primitive, primitiveToNodes);
	}

	protected void convertMediationPrimitives(Part upstreamedPart, Component component, String operationName, MessageFlow flow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode,
			List<com.ibm.etools.mft.conversion.esb.model.mfc.Node> nodes, String primitiveSuffix, RequestFlow requestFlow,
			OperationFlow sourceFlow) throws Exception {
		for (com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive : nodes) {
			convertMediationPrimitive(upstreamedPart, component, operationName, flow, inputNode, primitive, primitiveSuffix,
					requestFlow, sourceFlow);
		}
	}

	protected void convertMediationPrimitive(Part upstreamedPart, Component component, String operationName, MessageFlow flow,
			com.ibm.etools.mft.conversion.esb.model.mfc.Node inputNode, com.ibm.etools.mft.conversion.esb.model.mfc.Node primitive,
			String primitiveSuffix, RequestFlow requestFlow, OperationFlow sourceFlow) throws Exception {
		IPrimitiveConverter handler = context.getPrimitiveConverter(primitive.getType());
		String nodeName = PrimitiveManager.getNodeName(primitive, primitiveToNodes, primitiveSuffix);
		ConverterContext converterContext = new ConverterContext();
		converterContext.moduleConverter = this;
		converterContext.flowManager = flowManager;
		converterContext.upstreamedPart = upstreamedPart;
		converterContext.operationName = operationName;
		converterContext.targetFlow = flow;
		converterContext.sourceFlow = sourceFlow;
		converterContext.inputNodeInSourceFlow = inputNode;
		converterContext.sourcePrimitive = primitive;
		converterContext.component = component;
		Nodes nodes = handler.convert(converterContext);
		if (nodes != null) {
			StringBuffer sb = new StringBuffer();
			boolean isFirstTime = true;
			for (Node n : nodes.getAllNodes()) {
				if (!isFirstTime) {
					sb.append(","); //$NON-NLS-1$
				}
				sb.append(getConvertedNodeName(n.getClass().getName().substring(n.getClass().getPackage().getName().length() + 1)));
			}
			if (handler instanceof DefaultMediationPrimitiveConverter) {
				flowManager.log(flowManager.getFlowResource(flow),
						new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoUnsupportedPrimitiveConverted, nodeName)));
			} else {
				flowManager.log(flowManager.getFlowResource(flow),
						new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoPrimitiveConverted, nodeName, sb.toString())));
			}
		}
	}

	private String getConvertedNodeName(String s) {
		if (s.endsWith("Node")) { //$NON-NLS-1$
			s = s.substring(0, s.length() - 4);
		}
		if (s.equalsIgnoreCase("subflow")) { //$NON-NLS-1$
			return s;
		} else {
			if (s.equals("MappingMSL")) { //$NON-NLS-1$
				s = "Mapping"; //$NON-NLS-1$
			}
			return "'" + s + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void convertExports(MessageFlow mainFlow) throws Exception {
		for (NodeObject o : exports) {
			context.monitor.setTaskName(WESBConversionMessages.progressConvertingExport);
			convertExport(mainFlow, o);
		}
	}

	protected void convertExport(MessageFlow mainFlow, NodeObject nodeObject) throws Exception {
		Export export = (Export) nodeObject.root;
		context.log.addSourceToTargetResource(nodeObject.owningFile,
				targetProject.getFile(new Path(targetProject.getName() + WMQIConstants.MESSAGE_FLOW_EXTENSION)));
		for (Object o : export.getInterfaceSet().getInterfaces()) {
			if (o instanceof WSDLPortType) {
				convertWSDLPortTypeForExport(mainFlow, (WSDLPortType) o, export);
			}
		}
	}

	protected void convertWSDLPortTypeForExport(MessageFlow mainFlow, WSDLPortType portType, Export export) throws Exception {

		if (landingOnService) {
			// check whether it is SOAP/JMS binding. If yes, convert to App.
			Binding binding = export.getBinding();
			String qName = null;
			String portName = null;
			if (binding instanceof WebServiceExportBinding) {
				qName = ((WebServiceExportBinding) binding).getService().toString();
				portName = ((WebServiceExportBinding) binding).getPort().toString();
			} else if (binding instanceof JaxWsExportBinding) {
				qName = ((JaxWsExportBinding) binding).getService().toString();
				portName = ((JaxWsExportBinding) binding).getPort().toString();
			}
			if (qName != null && portName != null) {
				Service s = context.indexer.wsdlServices.get(qName);
				if (s == null) {
					throw new ESBConversionException(NLS.bind(WESBConversionMessages.errorUnresolvedService, qName));
				}
				javax.wsdl.Port port = s.getPort(ConversionUtils.getLocalPart(portName));
				boolean isJMS = com.ibm.etools.mft.conversion.esb.WSDLUtils.isJMSBinding(port.getBinding());
				if (isJMS) {
					// convert it back to application
					WorkspaceHelper
							.removeProjectNature(context.monitor, WMQIConstants.SERVICE_APPLICATION_NATURE_ID, targetProject);
					brokerService = null;
					landingOnService = false;
				}
			}
		}

		IBindingConverter bindingConverter = null;
		if (export.getBinding() != null) {
			bindingConverter = BindingManager.getConverter(export.getBinding().getClass().getName(), context, context.model);
		}
		if (bindingConverter == null) {
			bindingConverter = DefaultBindingConverter.instance;
		}

		ExportBinding sourceBinding = export.getBinding();
		if (sourceBinding == null) {
			sourceBinding = SCDLFactory.eINSTANCE.createNativeExportBinding();
			export.setBinding(sourceBinding);
		}
		com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter.ConverterContext bindingConverterContext = new com.ibm.etools.mft.conversion.esb.extensionpoint.IBindingConverter.ConverterContext();
		bindingConverterContext.moduleConverter = this;
		bindingConverterContext.sourceBinding = sourceBinding;
		bindingConverterContext.targetFlow = mainFlow;
		bindingConverterContext.portType = portType;
		bindingConverterContext.flowFile = flowManager.getFlowResource(mainFlow);
		bindingConverterContext.operationName = null;
		bindingConverterContext.flowManager = flowManager;
		bindingConverter.convert(bindingConverterContext);

		flowManager.log(
				flowManager.getFlowResource(mainFlow),
				new ConversionLogEntry(NLS.bind(WESBConversionMessages.infoExportConverted, export.getDisplayName(),
						bindingConverter.getConvertedTo()))); //$NON-NLS-1$
	}

	public void createService(String selectedPortType, IFile wsdlFile, Port port) throws Exception {
		brokerService.setWSDL(wsdlFile.getFullPath().makeRelativeTo(targetProject.getFullPath()).toString());
		brokerService.setMainFlowName(targetProject.getName() + WMQIConstants.MESSAGE_FLOW_EXTENSION);
		brokerService.setPortTypeName(selectedPortType);

		createServiceErrorHandlers();

		if (port.getBinding() == null) {
			throw new ESBConversionException(NLS.bind(WESBConversionMessages.errorUnresolvedBindingForPort, port.getName()));
		}
		if (port.getBinding().getPortType() == null) {
			throw new ESBConversionException(NLS.bind(WESBConversionMessages.errorUnresolvedPortTypeForBinding, port.getBinding()
					.getQName().toString()));
		}
		for (Object o : port.getBinding().getPortType().getOperations()) {
			if (o instanceof org.eclipse.wst.wsdl.Operation) {
				// FIXME: one way or R/R
				IntegrationServiceOperation op = new IntegrationServiceOperation(((org.eclipse.wst.wsdl.Operation) o).getName(),
						com.ibm.broker.config.appdev.IntegrationServiceOperation.OperationType.REQUEST_RESPONSE_TYPE);
				brokerService.getServiceOperations().add(op);
			}
		}
	}

	protected void createServiceErrorHandlers() throws Exception {
		brokerService.getErrorFlows()
			.add(createServiceFlow("gen/" + brokerService.getServiceName() + "InputCatchHandler", //$NON-NLS-1$ //$NON-NLS-2$
				com.ibm.broker.config.appdev.IntegrationServiceFlow.FlowType.CATCH_TYPE));
		brokerService.getErrorFlows()
			.add(createServiceFlow("gen/" + brokerService.getServiceName() + "InputFailureHandler", //$NON-NLS-1$ //$NON-NLS-2$
				com.ibm.broker.config.appdev.IntegrationServiceFlow.FlowType.FAILURE_TYPE));
		brokerService.getErrorFlows()
			.add(createServiceFlow("gen/" + brokerService.getServiceName() + "InputHTTPTimeoutHandler", //$NON-NLS-1$ //$NON-NLS-2$
				com.ibm.broker.config.appdev.IntegrationServiceFlow.FlowType.TIMEOUT_TYPE));
	}

	protected IntegrationServiceFlow createServiceFlow(String location, com.ibm.broker.config.appdev.IntegrationServiceFlow.FlowType type)
			throws Exception {
		IntegrationServiceFlow flow = new IntegrationServiceFlow(location + WMQIConstants.MESSAGE_SUBFLOW_EXTENSION, type);

		IFile msgFlowFile = targetProject.getFile(new Path(location + WMQIConstants.MESSAGE_SUBFLOW_EXTENSION));
		MessageFlow msgFlow = PrimitiveManager.getOrCreateMessageFlow(context, flowManager, msgFlowFile.getParent()
				.getProjectRelativePath().toString(), msgFlowFile.getFullPath().removeFileExtension().lastSegment(),
				WMQIConstants.MESSAGE_SUBFLOW_EXTENSION);

		PrimitiveManager.createNode(msgFlow, "Input", null, InputNode.class, null); //$NON-NLS-1$
		PrimitiveManager.createNode(msgFlow, "Output", null, OutputNode.class, null); //$NON-NLS-1$

		return flow;
	}

	public NodeObject getPart(String name) {
		for (NodeObject o : imports) {
			if (o.root == null) {
				continue;
			}
			if (((Displayable) o.root).getDisplayName().equals(name)) {
				return o;
			}
		}
		for (NodeObject o : components) {
			if (o.root == null) {
				continue;
			}
			if (((Displayable) o.root).getDisplayName().equals(name)) {
				return o;
			}
		}
		for (NodeObject o : exports) {
			if (o.root == null) {
				continue;
			}
			if (((Displayable) o.root).getDisplayName().equals(name)) {
				return o;
			}
		}
		return null;
	}

	public ConversionContext getContext() {
		return context;
	}

	public HashMap<Binding, Nodes> getBindingToNodes() {
		return bindingToNodes;
	}

	public IntegrationService getBrokerService() {
		return brokerService;
	}

	public IProject getTargetProject() {
		return targetProject;
	}

	public Collection<? extends String> getApplicableLandingPoints() {
		List<String> landingPoints = new ArrayList<String>();
		boolean canBeService = false;
		if (exports.size() == 1 && components.size() == 1 && components.get(0).isMFCComponent()) {
			ExportBinding binding = ((Export) exports.get(0).root).getBinding();
			if ((binding instanceof WebServiceExportBinding) || (binding instanceof JaxWsExportBinding)) {
				// JMS?
				canBeService = true;
			}
		}
		if (canBeService) {
			landingPoints.add(IIB_SERVICE);
		}
		landingPoints.add(IIB_APPLICATION);
		return landingPoints;
	}

	public HashMap<com.ibm.etools.mft.conversion.esb.model.mfc.Node, Nodes> getPrimitiveToNodes() {
		return primitiveToNodes;
	}
}
