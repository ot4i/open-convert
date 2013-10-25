package com.ibm.etools.mft.conversion.esb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.wst.wsdl.WSDLFactory;
import org.eclipse.wst.wsdl.XSDSchemaExtensibilityElement;
import org.eclipse.wst.wsdl.binding.soap.SOAPBinding;
import org.eclipse.wst.wsdl.internal.impl.DefinitionImpl;
import org.eclipse.wst.wsdl.util.WSDLResourceImpl;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDInclude;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaDirective;
import org.eclipse.xsd.impl.XSDSchemaImpl;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Node;

import com.ibm.etools.mft.conversion.esb.extension.resource.XSDAndWSDLConverterHelper;
import com.ibm.etools.mft.util.WMQIConstants;
import com.ibm.etools.msg.importer.wsdl.pages.WSDLImportOptions;
import com.ibm.etools.msg.msgmodel.utilities.importhelpers.XGenSchemaFile;
import com.ibm.etools.msg.msgmodel.utilities.wsdlhelpers.DeployableWSDLAnnotationsHelper;
import com.ibm.etools.msg.msgmodel.utilities.wsdlhelpers.DeployableWSDLHelper;
import com.ibm.xwt.wsdl.binding.soap12.SOAP12Address;
import com.ibm.xwt.wsdl.binding.soap12.SOAP12Binding;

/**
 * @author Zhongming Chen
 * 
 */
public class WSDLUtils {

	public static boolean isRemoteSchema(String schemaLocation) {
		return schemaLocation != null && schemaLocation.toLowerCase().startsWith("http:"); //$NON-NLS-1$
	}

	public static boolean hasBinding(Definition def) {
		return (def.getBindings().keySet().size() > 0);
	}

	public static Definition getDefinition(IFile f, ResourceSet rs) {
		Resource r = rs.getResource(URI.createPlatformResourceURI(f.getFullPath().toString()), true);
		return r.getContents().size() == 1 && r.getContents().get(0) instanceof Definition ? (Definition) r.getContents().get(0)
				: null;
	}

	public static void updateImportedWSDL(WSDLImportOptions opt, ConversionHelper helper, HashMap<String, IFile> fileNameToLocation)
			throws IOException {
		org.eclipse.wst.wsdl.Definition definition = (org.eclipse.wst.wsdl.Definition) opt.getWSDLDefinition();
		WSDLResourceImpl r = (WSDLResourceImpl) definition.eResource();

		r.unload();
		r.getResourceSet().getResource(r.getURI(), true);
		opt.setWSDLDefinition(r.getDefinition());
		definition = r.getDefinition();

		// remove inlined schema if there is any
		if (definition.getTypes() != null && definition.getTypes().getExtensibilityElements().size() > 0) {
			List<XSDSchemaExtensibilityElement> toRemove = new ArrayList<XSDSchemaExtensibilityElement>();
			for (Object o : definition.getTypes().getExtensibilityElements()) {
				if (o instanceof XSDSchemaExtensibilityElement) {
					XSDSchemaExtensibilityElement xsdee = (XSDSchemaExtensibilityElement) o;
					if (xsdee.getSchema() == null) {
						XSDSchema schema = XSDSchemaImpl.createSchema(xsdee.getElement());
						if (WSDLUtils.isInlineSchema(schema)) {
							toRemove.add(xsdee);
						} else {
							xsdee.setSchema(schema);
						}
					} else {
						if (WSDLUtils.isInlineSchema(xsdee.getSchema())) {
							toRemove.add(xsdee);
						}
					}
				}
			}
			definition.getTypes().getExtensibilityElements().removeAll(toRemove);
		}

		// add schema include
		List<IPath> generatedXSDs = new ArrayList<IPath>();
		for (XGenSchemaFile f : opt.getSchemaList().getEmittableFiles()) {
			IFile file = f.getMSDFile();
			if (file.getName().startsWith(opt.getSourceFile().getFullPath().removeFileExtension().lastSegment() + "_InlineSchema") //$NON-NLS-1$
					&& file.getFileExtension().equals(WMQIConstants.WSDL_FILE_EXTENSION_NO_DOT)) {
				XSDSchemaExtensibilityElement schema = WSDLFactory.eINSTANCE.createXSDSchemaExtensibilityElement();
				schema.setEnclosingDefinition(definition);
				XSDSchema s = XSDFactory.eINSTANCE.createXSDSchema();
				s.setTargetNamespace(f.getSchema().getTargetNamespace());
				Map qNamePrefixToNamespaceMap = s.getQNamePrefixToNamespaceMap();
				s.setSchemaForSchemaQNamePrefix("xsd"); //$NON-NLS-1$
				qNamePrefixToNamespaceMap.put(s.getSchemaForSchemaQNamePrefix(), XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
				qNamePrefixToNamespaceMap.put("tns", f.getSchema().getTargetNamespace()); //$NON-NLS-1$
				XSDInclude include = XSDFactory.eINSTANCE.createXSDInclude();
				String location = file.getFullPath().removeFileExtension()
						.addFileExtension(WMQIConstants.XSD_FILE_EXTENSION_NO_DOT).lastSegment();
				generatedXSDs.add(new Path(location));
				include.setSchemaLocation(location);
				s.getContents().add(include);
				s.updateElement();
				schema.setSchema(s);
				if (definition.getTypes() == null) {
					definition.setTypes(WSDLFactory.eINSTANCE.createTypes());
				}
				definition.getTypes().addExtensibilityElement(schema);
			}
		}

		updateImportedSchemaLocation(opt.getSourceFile().getParent(), definition, helper, fileNameToLocation);

		// prepare WSDL for update
		((DefinitionImpl) definition).updateDocument();
		definition.setElement(null);
		definition.updateElement(true);

		// add annotation
		DeployableWSDLAnnotationsHelper annotationsHelper = new DeployableWSDLAnnotationsHelper();
		DeployableWSDLAnnotationsHelper.removeWSDLAnnotations(definition);
		annotationsHelper.addIsImportedAnnotation(definition);
		for (IPath p : generatedXSDs) {
			annotationsHelper.addGeneratedXSDAnnotation(definition, p);
		}
		Map bindings = definition.getBindings();
		for (Iterator<Binding> i = bindings.values().iterator(); i.hasNext();) {
			Binding thisBinding = i.next();
			String style = DeployableWSDLHelper.getStyle(thisBinding);
			if (style != null) {
				annotationsHelper.addOriginalBindingAnnotation(definition, (Node) ((org.eclipse.wst.wsdl.Binding) thisBinding)
						.getElement().getParentNode(), thisBinding.getQName().getLocalPart(), style, DeployableWSDLHelper
						.hasEncodedUse(thisBinding), true);
			}
		}
		annotationsHelper.insertAnnotations(definition);

		// save WSDL
		definition.eResource().save(Collections.EMPTY_MAP);
	}

	public static void updateWSDLImportLocation(IContainer currentDir, org.eclipse.wst.wsdl.Definition definition,
			ConversionHelper helper, HashMap<String, IFile> fileNameToLocation) {
		for (Object o : definition.getImports().values()) {
			if (o instanceof Import) {
				Import imp = (Import) o;
				String location = imp.getLocationURI();
				String ns = imp.getNamespaceURI();
				if (ns == null) {
					ns = ""; //$NON-NLS-1$
				}
				location = updateLocation(currentDir, ns, location, helper, fileNameToLocation);
				imp.setLocationURI(location);
			} else if (o instanceof List) {
				for (Object o1 : (List) o) {
					if (o1 instanceof Import) {
						Import imp = (Import) o1;
						String location = imp.getLocationURI();
						String ns = imp.getNamespaceURI();
						if (ns == null) {
							ns = ""; //$NON-NLS-1$
						}
						location = updateLocation(currentDir, ns, location, helper, fileNameToLocation);
						imp.setLocationURI(location);
					}
				}
			}
		}
	}

	public static void updateWSDLInlineSchemaImportLocation(IContainer currentDir, org.eclipse.wst.wsdl.Definition definition,
			ConversionHelper helper, HashMap<String, IFile> fileNameToLocation) {
		if (definition.getTypes() != null) {
			for (Object o : definition.getTypes().getExtensibilityElements()) {
				if (o instanceof XSDSchemaExtensibilityElement) {
					XSDSchemaExtensibilityElement xsdee = (XSDSchemaExtensibilityElement) o;
					if (xsdee.getSchema() != null) {
						for (Object o1 : xsdee.getSchema().getContents()) {
							if (o1 instanceof XSDImport) {
								String ns = ((XSDImport) o1).getNamespace();
								if (ns == null) {
									ns = ""; //$NON-NLS-1$
								}
								String location = updateLocation(currentDir, ns, ((XSDImport) o1).getSchemaLocation(), helper,
										fileNameToLocation);
								((XSDImport) o1).setSchemaLocation(location);
							} else if (o1 instanceof XSDInclude) {
								String location = updateLocation(currentDir,
										"", ((XSDInclude) o1).getSchemaLocation(), helper, fileNameToLocation); //$NON-NLS-1$
								((XSDInclude) o1).setSchemaLocation(location);
							}
						}
					}
				}
			}
		}
	}

	private static void updateImportedSchemaLocation(IContainer currentDir, org.eclipse.wst.wsdl.Definition definition,
			ConversionHelper helper, HashMap<String, IFile> fileNameToLocation) {
		if (definition.getTypes() != null) {
			for (Object o : definition.getTypes().getExtensibilityElements()) {
				if (o instanceof XSDSchemaExtensibilityElement) {
					XSDSchema schema = ((XSDSchemaExtensibilityElement) o).getSchema();
					for (Object o1 : schema.getContents()) {
						if (o1 instanceof XSDSchemaDirective) {
							XSDSchemaDirective xsdsd = ((XSDSchemaDirective) o1);
							String location = xsdsd.getSchemaLocation();
							String ns = ""; //$NON-NLS-1$
							if (xsdsd.getResolvedSchema() != null) {
								ns = xsdsd.getResolvedSchema().getTargetNamespace();
							}
							location = updateLocation(currentDir, ns, location, helper, fileNameToLocation);
							xsdsd.setSchemaLocation(location);
						}
					}
				}
			}
		}
	}

	private static String updateLocation(IContainer currentDir, String ns, String location, ConversionHelper helper,
			HashMap<String, IFile> fileNameToLocation) {
		if (location == null) {
			return null;
		}
		IPath path = new Path(location);
		String fileName = path.lastSegment();
		IFile sourceFile = fileNameToLocation.get(XSDAndWSDLConverterHelper.encodeFileLocationKey(ns, fileName));
		if (sourceFile == null) {
			// maybe inline schema, ignore
			return location;
		}
		IFile actualFile = ConversionUtils.getProject(helper.getConvertedProjectName(sourceFile.getProject())).getFile(
				sourceFile.getProjectRelativePath());
		if (sourceFile.getProject() != currentDir.getProject()) {
			// cross project reference
			return actualFile.getFullPath().makeRelativeTo(currentDir.getFullPath()).toString();
		}
		return actualFile.getProjectRelativePath().toString();
	}

	public static boolean isInlineSchema(XSDSchema schema) {
		// TODO: May need to refine the logic.
		if (schema.getContents() == null) {
			return true;
		}
		for (Object o : schema.getContents()) {
			if (!(o instanceof XSDSchemaDirective)) {
				return true;
			}
		}
		return false;
	}

	public static SOAPBinding getSOAPBinding(Binding binding) {
		for (Object o : binding.getExtensibilityElements()) {
			if (o instanceof SOAPBinding) {
				return (SOAPBinding) o;
			}
		}
		return null;
	}

	public static SOAP12Binding getSOAP12Binding(Binding binding) {
		for (Object o : binding.getExtensibilityElements()) {
			if (o instanceof SOAP12Binding) {
				return (SOAP12Binding) o;
			}
		}
		return null;
	}

	public static boolean isJMSBinding(String transportURI) {
		return "http://schemas.xmlsoap.org/soap/jms".equals(transportURI); //$NON-NLS-1$
	}

	public static boolean isJMSBinding(Binding binding) {
		SOAPBinding soapBinding = WSDLUtils.getSOAPBinding(binding);
		boolean isJMS = false;
		if (soapBinding != null) {
			isJMS = WSDLUtils.isJMSBinding(soapBinding.getTransportURI());
		} else {
			SOAP12Binding soap12Binding = WSDLUtils.getSOAP12Binding(binding);
			if (soap12Binding != null) {
				isJMS = WSDLUtils.isJMSBinding(soap12Binding.getTransportURI());
			}
		}

		return isJMS;
	}

	public static String getSOAPAddressLocation(Port port) {
		for (Object o : port.getExtensibilityElements()) {
			if (o instanceof SOAPAddress) {
				return ((SOAPAddress) o).getLocationURI();
			} else if (o instanceof SOAP12Address) {
				return ((SOAP12Address) o).getLocationURI();
			}
		}
		return null;
	}
}
