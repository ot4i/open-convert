/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.Service;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;

import com.ibm.etools.mft.conversion.esb.ConversionHelper;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBResource;
import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.Node;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.etools.mft.flow.xpath.MFTXPathBuilderFactory;
import com.ibm.etools.mft.util.WMQIConstants;
import com.ibm.msl.xml.xpath.IXPathModel;
import com.ibm.msl.xml.xpath.XPathCompositeNode;
import com.ibm.msl.xml.xpath.XPathNode;
import com.ibm.msl.xml.xpath.XPathTokenNode;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionContext {

	public static int seed = 0;

	public class MappedPath {
		public HashMap<String, String> namespaceToPrefix = new HashMap<String, String>();
		public String mappedPath = null;
	}

	public class Indexer {
		public HashMap<String, IFile> xsdElements = new HashMap<String, IFile>();
		public HashMap<String, IFile> xsdTypes = new HashMap<String, IFile>();
		public HashMap<String, List<String>> xsdLocalNameToNamespaces = new HashMap<String, List<String>>();
		public HashMap<String, List<String>> wsdlMessages = new HashMap<String, List<String>>();
		public HashMap<String, IFile> wsdlPortTypesToFile = new HashMap<String, IFile>();
		public HashMap<String, PortType> wsdlPortTypes = new HashMap<String, PortType>();
		public HashMap<String, IFile> wsdlServicesToFile = new HashMap<String, IFile>();
		public HashMap<String, List<String>> wsdlLocalNameToNamespaces = new HashMap<String, List<String>>();
		public HashMap<String, Service> wsdlServices = new HashMap<String, Service>();
		public HashSet<IProject> projectsProcessed = new HashSet<IProject>();

		public void index(IProject project) throws CoreException {
			if (indexer.projectsProcessed.contains(project)) {
				return;
			}

			if (!project.exists() || !project.isAccessible()) {
				return;
			}
			project.accept(new IResourceVisitor() {

				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (!(resource instanceof IFile)) {
						return true;
					}
					if (resource.getFileExtension().equals(WMQIConstants.XSD_FILE_EXTENSION_NO_DOT)) {
						Resource r = resourceSet.getResource(URI.createPlatformResourceURI(resource.getFullPath().toString()), true);
						for (Object o : r.getContents()) {
							if (o instanceof XSDSchema) {
								for (Object o1 : ((XSDSchema) o).getContents()) {
									if (o1 instanceof XSDElementDeclaration) {
										xsdElements.put(ConversionUtils.getQName(((XSDElementDeclaration) o1).getTargetNamespace(),
												((XSDElementDeclaration) o1).getName()), (IFile) resource);
									} else if (o1 instanceof XSDTypeDefinition) {
										xsdTypes.put((ConversionUtils.getQName(((XSDTypeDefinition) o1).getTargetNamespace(),
												((XSDTypeDefinition) o1).getName())), (IFile) resource);
										addLocalName(xsdLocalNameToNamespaces, ((XSDTypeDefinition) o1).getTargetNamespace(),
												((XSDTypeDefinition) o1).getName());
									}
								}
							}
						}
					} else if (resource.getFileExtension().equals(WMQIConstants.WSDL_FILE_EXTENSION_NO_DOT)) {
						Resource r = resourceSet.getResource(URI.createPlatformResourceURI(resource.getFullPath().toString()), true);
						for (Object o : r.getContents()) {
							if (o instanceof Definition) {
								for (Object o1 : ((Definition) o).getMessages().values()) {
									if (o1 instanceof Message) {
										List<String> parts = new ArrayList<String>();
										wsdlMessages.put(ConversionUtils.getQName(((Message) o1).getQName().getNamespaceURI(),
												((Message) o1).getQName().getLocalPart()), parts);
										for (Object partObject : ((Message) o1).getParts().values()) {
											if (partObject instanceof Part) {
												Part part = (Part) partObject;
												if ( part.getElementName() == null ) {
													continue;
												}
												parts.add(ConversionUtils.getQName(part.getElementName().getNamespaceURI(), part
														.getElementName().getLocalPart()));
												addLocalName(wsdlLocalNameToNamespaces, part.getElementName().getNamespaceURI(),
														part.getElementName().getLocalPart());
											}
										}
									}
								}
								for (Object o1 : ((Definition) o).getPortTypes().values()) {
									if (o1 instanceof PortType) {
										wsdlPortTypesToFile.put(ConversionUtils.getQName(((PortType) o1).getQName()
												.getNamespaceURI(), ((PortType) o1).getQName().getLocalPart()), (IFile) resource);
										wsdlPortTypes.put(ConversionUtils.getQName(((PortType) o1).getQName().getNamespaceURI(),
												((PortType) o1).getQName().getLocalPart()), (PortType) o1);
									}
								}
								for (Object o1 : ((Definition) o).getServices().values()) {
									if (o1 instanceof Service) {
										wsdlServicesToFile.put(ConversionUtils.getQName(
												((Service) o1).getQName().getNamespaceURI(), ((Service) o1).getQName()
														.getLocalPart()), (IFile) resource);
										wsdlServices.put(ConversionUtils.getQName(((Service) o1).getQName().getNamespaceURI(),
												((Service) o1).getQName().getLocalPart()), (Service) o1);
									}
								}
							}
						}
					}

					return true;
				}

				private void addLocalName(HashMap<String, List<String>> nsMap, String ns, String name) {
					List<String> nss = nsMap.get(name);
					if (nss == null) {
						nsMap.put(name, nss = new ArrayList<String>());
					}
					if (!nss.contains(ns)) {
						nss.add(ns);
					}
				}
			});

			for (IProject p : project.getDescription().getReferencedProjects()) {
				index(p);
			}
		}
	}

	public IProgressMonitor monitor;
	public IResource resource;
	public WESBConversionDataType model;
	public ConversionHelper helper;
	public ConversionLog log;
	public HashMap<String, Object> option;
	public ResourceSet resourceSet;
	public HashMap<String, String> pathMappings = new HashMap<String, String>();
	public List<IProject> projects;
	public HashMap<String, Object> variables = new HashMap<String, Object>();
	public WESBResource wesbResource;
	public Indexer indexer = new Indexer();
	private ConversionClassLoader classLoader = new ConversionClassLoader(new URL[0]);

	public ConversionContext(WESBConversionDataType model, ConversionLog log) {
		this.log = log;
		resourceSet = new ResourceSetImpl();
		this.model = model;
		helper = new ConversionHelper(model);
		configureClassLoader();
	}

	public void index(IProject project) throws CoreException {
		indexer.index(project);
	}

	protected void configureClassLoader() {
		classLoader = new ConversionClassLoader(new URL[0]);
	}

	public IPrimitiveConverter getPrimitiveConverter(String type) throws Exception {
		return PrimitiveManager.getConverter(type, this, model);
	}

	public MappedPath mapXPath(IFile resource, Node inputNode, String value) {
		MappedPath mappedPath = new MappedPath();
		IXPathModel xpath = MFTXPathBuilderFactory.createXPathModel("mft", value); //$NON-NLS-1$
		XPathCompositeNode location = xpath.getRootLocationPath();
		StringBuffer sb = new StringBuffer();
		convertXPathCompositeNode(location, sb, inputNode, mappedPath, resource);
		mappedPath.mappedPath = sb.toString();
		return mappedPath;
	}

	protected void convertXPathCompositeNode(XPathCompositeNode node, StringBuffer sb, Node inputNode, MappedPath mappedPath,
			IFile resource) {
		for (XPathNode n : node.getChildrenNodes()) {
			if (n instanceof XPathTokenNode) {
				sb.append(n.toString());
			} else if (n instanceof XPathCompositeNode) {
				if (n.getType() == XPathNode.EXPRESSION_TYPE) {
					convertXPathCompositeNode((XPathCompositeNode) n, sb, inputNode, mappedPath, resource);
				} else if (n.getType() == XPathNode.STEP_TYPE) {
					// element
					String value = n.toString();
					if (value.startsWith("/body/")) { //$NON-NLS-1$
						value = resolvePath(resource, inputNode, value, mappedPath);
						sb.append("$Root/XMLNSC" + value); //$NON-NLS-1$ 
					} else {
						sb.append(n.toString());
					}
				} else {
					sb.append(n.toString());
				}
			}
		}
	}

	protected String resolvePath(IFile resource, Node inputNode, String key, MappedPath mappedPath) {
		if ("/context/correlation".equals(key)) { //$NON-NLS-1$
			AbstractProperty p = PrimitiveManager.getProperty(inputNode, "correlationContext"); //$NON-NLS-1$
			if (p != null) {
				String qName = ((Property) p).getValue();
				String nsPrefix = addNSPrefix(mappedPath, ConversionUtils.getNamespace(qName));
				return "/" + (nsPrefix == null ? "" : (nsPrefix + ":")) + ConversionUtils.getLocalPart(qName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else if (key.startsWith("/body/")) { //$NON-NLS-1$
			int index = key.indexOf("/", 6); //$NON-NLS-1$
			if (index > 0) {
				String localName = key.substring(6, index);
				String ns = getNamespaceForWSDLMessage(resource, localName);
				String nsPrefix = addNSPrefix(mappedPath, ns);
				return "/" + (nsPrefix == null ? "" : (nsPrefix + ":")) + key.substring(6); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return key;
	}

	protected String getNamespace(IFile resource, String localName) {
		List<String> nss = indexer.xsdLocalNameToNamespaces.get(localName);
		if (nss != null) {
			if (nss.size() > 1) {
				// more than 1 namespaces.
				log.addEntry(resource,
						new TodoEntry(NLS.bind(WESBConversionMessages.todoCantExactlyLocateXSDBasedOnLocalName, localName)));
			}
		}
		return ""; //$NON-NLS-1$
	}

	protected String getNamespaceForWSDLMessage(IFile resource, String localName) {
		List<String> nss = indexer.wsdlLocalNameToNamespaces.get(localName);
		if (nss != null) {
			if (nss.size() > 1) {
				// more than 1 namespaces.
				StringBuffer sb = new StringBuffer();
				for (String s : nss) {
					sb.append(s);
					sb.append("<br/>");
				}
				log.addEntry(
						resource,
						new TodoEntry(NLS.bind(WESBConversionMessages.todoCantExactlyLocateWSDLMessagePartsBasedOnLocalName,
								new Object[] { localName, sb.toString(), nss.get(0) })));
				return nss.get(0);
			} else if (nss.size() == 1) {
				return nss.get(0);
			}
		}
		return null;
	}

	protected String addNSPrefix(MappedPath mappedPath, String namespace) {
		if (namespace == null) {
			return null;
		}
		String nsPrefix = mappedPath.namespaceToPrefix.get(namespace);
		if (nsPrefix == null) {
			nsPrefix = "ns" + (seed++); //$NON-NLS-1$
			mappedPath.namespaceToPrefix.put(namespace, nsPrefix);
		}
		return nsPrefix;
	}

}
