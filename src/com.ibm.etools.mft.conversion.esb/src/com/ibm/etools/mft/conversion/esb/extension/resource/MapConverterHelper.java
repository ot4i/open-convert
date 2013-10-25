/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="1898847983" > 
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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.userlog.ConversionLog;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;
import com.ibm.msl.mapping.MappingRoot;
import com.ibm.msl.mapping.util.ModelUtils;
import com.ibm.msl.mapping.xml.util.XMLUtils;

/**
 * @author Zhongming Chen
 *
 */
public class MapConverterHelper {

	public class SMOStructure {
		public String correlationContext;
		public String message;
		public String sharedContext;
		public String transientContext;
		public String xpath;
		public String smo;
		public Element owner;
	}

	public class Variable {
		public Variable(Element e, SMOStructure smo) {
			declraingElement = e;
			this.smo = smo;
		}

		public SMOStructure smo;
		public Element declraingElement;
	}

	public class MappingObject {
		public MappingObject(Element parent) {
			NodeList nl = parent.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n instanceof Element) {
					if ("input".equals(n.getNodeName())) { //$NON-NLS-1$
						inputs.add((Element) n);
					} else if ("output".equals(n.getNodeName())) { //$NON-NLS-1$
						outputs.add((Element) n);
					} else {
						others.add((Element) n);
					}
				}
			}
		}

		public List<Element> inputs = new ArrayList<Element>();
		public List<Element> outputs = new ArrayList<Element>();
		public List<Element> others = new ArrayList<Element>();
	}

	public static final String SMO_MAP = "SMO_MAP"; //$NON-NLS-1$
	public static final String NON_SMO_MAP = "NON_SMO_MAP"; //$NON-NLS-1$

	private ConversionLog userLog;
	private HashMap<IFile, String> variablesForInput = new HashMap<IFile, String>();
	private HashMap<IFile, String> variablesForOutput = new HashMap<IFile, String>();
	private int idCount = 0;
	private HashMap<String, String> pathMappings;
	private IFile targetFile;
	private boolean successfulConversion = false;
	private HashMap<String, SMOStructure> outputSMOs = new HashMap<String, MapConverterHelper.SMOStructure>();
	private HashMap<String, SMOStructure> inputSMOs = new HashMap<String, MapConverterHelper.SMOStructure>();
	private HashSet<String> todoTasksAdded = new HashSet<String>();
	private ConversionContext context;
	private boolean oldStyleMap;

	public MapConverterHelper(ConversionContext context) {
		this.context = context;
	}

	public String preview(IFile mapFile, HashMap<String, List<String>> mapUsages) {
		Document dom;
		try {
			dom = ConversionUtils.loadXML(mapFile.getContents());
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		NodeList mappingRoots = dom.getElementsByTagName("mappingRoot"); //$NON-NLS-1$
		if (mappingRoots.getLength() == 0) {
			return null;
		}

		Element mr = (Element) mappingRoots.item(0);
		Element imports = ConversionUtils.getFirstElement(mr, null, "imports"); //$NON-NLS-1$
		if (imports != null) {
			for (Element e : ConversionUtils.getImmediateChild(imports, null, "import")) { //$NON-NLS-1$
				String kind = e.getAttribute("kind"); //$NON-NLS-1$
				if (!"map".equals(kind)) { //$NON-NLS-1$
					continue;
				}
				String location = e.getAttribute("location"); //$NON-NLS-1$
				if (location.startsWith("/")) { //$NON-NLS-1$
					location = location.substring(1);
				}
				ConversionUtils.addUsage(mapUsages, mapFile.getProjectRelativePath().toString(), location);
			}
		}

		for (Element e : getInputs(mr)) {
			if (isSMO(e)) {
				return SMO_MAP;
			}
		}

		for (Element e : getOutputs(mr)) {
			if (isSMO(e)) {
				return SMO_MAP;
			}
		}

		return NON_SMO_MAP;
	}

	public void convert() throws Exception {
		successfulConversion = true;
		oldStyleMap = false;
		if (!context.resource.exists()) {
			return;
		}
		this.targetFile = context.helper.getTargetFile((IFile) context.resource);
		ConversionUtils.copy((IFile) context.resource, targetFile);

		this.userLog = context.log;
		this.pathMappings = context.pathMappings;
		variablesForInput.clear();
		variablesForOutput.clear();
		idCount = 0;
		inputSMOs.clear();
		outputSMOs.clear();
		todoTasksAdded.clear();

		context.index(targetFile.getProject());
		context.monitor.subTask(WESBConversionMessages.progressConverting + context.resource.getFullPath().toString());

		Document dom = ConversionUtils.loadXML(targetFile.getContents());

		NodeList mappingRoots = dom.getElementsByTagNameNS("http://www.ibm.com/2008/ccl/Mapping", "mappingRoot"); //$NON-NLS-1$ //$NON-NLS-2$
		if (mappingRoots.getLength() > 1) {
			successfulConversion = false;
			userLog.addEntry(targetFile, new TodoEntry(WESBConversionMessages.todoMultipleMappingRoots));
		}

		if (mappingRoots.getLength() > 0) {
			Node mr = mappingRoots.item(0);
			convertMappingRoot((Element) mr);
		}

		String content = ConversionUtils.saveXML(dom);
		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8")); //$NON-NLS-1$
		targetFile.setContents(in, true, false, new NullProgressMonitor());

		if (oldStyleMap) {
			URI mapFileURI = URI.createPlatformResourceURI(targetFile.getFullPath().toString());
			Resource r = ModelUtils.getMappingResourceManager(mapFileURI).createResourceSet().getResource(mapFileURI, true);
			if (r.getContents().size() > 0 && (r.getContents().get(0) instanceof MappingRoot)) {
				MappingRoot mr = (MappingRoot) r.getContents().get(0);
				XMLUtils.setTargetEngine(mr, "xquery");
				r.save(Collections.EMPTY_MAP);
			}
		}

		if (successfulConversion) {
			userLog.addEntry(targetFile,
					new TodoEntry(NLS.bind(WESBConversionMessages.todoPotentialErrorsOnMap, targetFile.getFullPath().toString())));
		}
		userLog.addSourceToTargetResource(context.resource, targetFile);
	}

	protected void convertMappingRoot(Element mr) throws UnsupportedEncodingException {
		String version = mr.getAttribute("version"); //$NON-NLS-1$
		oldStyleMap = (version == null || version.equals("")) ? true : false; //$NON-NLS-1$

		mr.setAttribute("domainIDExtension", "mb"); //$NON-NLS-1$ //$NON-NLS-2$
		mr.setAttribute("mainMap", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		Element generation = ConversionUtils.getFirstElement(mr, "http://www.ibm.com/2008/ccl/Mapping", "generation"); //$NON-NLS-1$ //$NON-NLS-2$
		if (generation == null) {
			if (!oldStyleMap) {
				generation = mr.getOwnerDocument().createElementNS("http://www.ibm.com/2008/ccl/Mapping", "generation"); //$NON-NLS-1$ //$NON-NLS-2$
				mr.appendChild(generation);
				userLog.addEntry(targetFile,
						new TodoEntry(NLS.bind(WESBConversionMessages.todoXPath10DetectedInMap, targetFile.getName())));
			}
		} else {
			String engine = generation.getAttribute("engine"); //$NON-NLS-1$
			if (engine == null || "".equals(engine) || "xslt".equals(engine)) { //$NON-NLS-1$ //$NON-NLS-2$
				userLog.addEntry(targetFile,
						new TodoEntry(NLS.bind(WESBConversionMessages.todoXPath10DetectedInMap, targetFile.getName())));
			}
		}
		if (generation != null) {
			generation.setAttribute("engine", "xquery"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (Element e : getInputs(mr)) {
			populateSMOs(e, inputSMOs);
		}

		for (Element e : getOutputs(mr)) {
			populateSMOs(e, outputSMOs);
		}

		List<Element> nl = ConversionUtils.getImmediateChild(mr, null, "mappingDeclaration"); //$NON-NLS-1$
		if (nl.size() > 1) {
			successfulConversion = false;
			userLog.addEntry(targetFile, new TodoEntry(WESBConversionMessages.todoMultipleMappingDeclarations));
		} else if (nl.size() == 1) {
			convertMappingDeclaration(nl.get(0), oldStyleMap);
		}

	}

	protected void populateSMOs(Element e, HashMap<String, SMOStructure> SMOs) throws UnsupportedEncodingException {
		if (isSMO(e)) {
			SMOStructure smoStructure = new SMOStructure();
			String var = e.getAttribute("var"); //$NON-NLS-1$
			if (var == null) {
				var = ""; //$NON-NLS-1$
			}
			SMOs.put(var, smoStructure);

			populateSmoStrucutre(smoStructure, e);
		} else {
			updatePathForSchema(e);
		}
	}

	protected void updatePathForSchema(Element e) {
	}

	protected boolean isSMO(Element e) {
		if ("smo".equals(e.getAttribute("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		String path = e.getAttribute("path"); //$NON-NLS-1$
		if (path != null && path.startsWith("smo:/")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	protected List<Element> getOutputs(Element e) {
		return ConversionUtils.getImmediateChild(e, null, "output"); //$NON-NLS-1$
	}

	protected List<Element> getInputs(Element e) {
		return ConversionUtils.getImmediateChild(e, null, "input"); //$NON-NLS-1$
	}

	protected void populateSmoStrucutre(SMOStructure smo, Element e) throws UnsupportedEncodingException {
		smo.correlationContext = e.getAttribute("correlationContext"); //$NON-NLS-1$
		smo.message = e.getAttribute("message"); //$NON-NLS-1$
		smo.sharedContext = e.getAttribute("sharedContext"); //$NON-NLS-1$
		smo.transientContext = e.getAttribute("transientContext"); //$NON-NLS-1$
		smo.xpath = e.getAttribute("xpath"); //$NON-NLS-1$
		smo.smo = e.getAttribute("smo"); //$NON-NLS-1$
		smo.owner = e;
		String path = e.getAttribute("path"); //$NON-NLS-1$
		if (path.startsWith("smo:/")) { //$NON-NLS-1$
			// smo schema path
			String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
			String value = getValue(decodedPath, "/message="); //$NON-NLS-1$
			if (value != null) {
				smo.message = value;
			}
			value = getValue(decodedPath, "/xpath="); //$NON-NLS-1$
			if (value != null) {
				smo.xpath = value;
			}
			value = getValue(decodedPath, "/correlationContext="); //$NON-NLS-1$
			if (value != null) {
				smo.correlationContext = value;
			}
		}

		e.removeAttribute("message"); //$NON-NLS-1$
		e.removeAttribute("sharedContext"); //$NON-NLS-1$
		e.removeAttribute("correlationContext"); //$NON-NLS-1$
		e.removeAttribute("type"); //$NON-NLS-1$
		e.removeAttribute("smoName"); //$NON-NLS-1$
		e.removeAttribute("types"); //$NON-NLS-1$
		e.removeAttribute("transientContext"); //$NON-NLS-1$
		e.removeAttribute("smo"); //$NON-NLS-1$
		e.removeAttribute("xpath"); //$NON-NLS-1$

		if (!ConversionUtils.hasValue(smo.message)) {
			// remove input
			e.getParentNode().removeChild(e);
			return;
		}

		List<String> parts = getWSDLMessageParts(smo.message);
		if (parts == null) {
			return;
		}

		updateInputOutputOnMappingRoot(e, parts.get(0));

		// if (ConversionUtils.hasValue(smo.correlationContext)) {
		// Element e1 = e.getOwnerDocument().createElement("input");
		// e.getParentNode().insertBefore(e1, e.getNextSibling());
		// updateInputOutputOnMappingRoot(e1, smo.correlationContext);
		// }
	}

	protected String getValue(String path, String key) throws UnsupportedEncodingException {
		int start = path.indexOf(key);
		int end = 0;
		if (start > 0) {
			start += key.length();
			end = path.indexOf("/", start); //$NON-NLS-1$
			if (end > 0) {
				return URLDecoder.decode(path.substring(start, end), "UTF-8"); //$NON-NLS-1$
			}
		}
		return null;
	}

	protected void updateInputOutputOnMappingRoot(Element e, String type) {
		IFile xsdFile = findXSDFile(type);
		if (xsdFile == null) {
			return;
		}
		// String xsdLocation =
		// xsdFile.getFullPath().makeRelativeTo(targetFile.getParent().getFullPath()).toString();
		String xsdLocation = xsdFile.getProjectRelativePath().toString();
		if (!xsdLocation.startsWith("/")) { //$NON-NLS-1$
			xsdLocation = "/" + xsdLocation; //$NON-NLS-1$
		}

		String var = "wesbconversionvar" + (idCount++); //$NON-NLS-1$
		if (e.getNodeName().equals("input")) { //$NON-NLS-1$
			variablesForInput.put(xsdFile, var);
		} else {
			variablesForOutput.put(xsdFile, var);
		}
		e.setAttribute("path", xsdLocation); //$NON-NLS-1$
		e.setAttribute("var", var); //$NON-NLS-1$
	}

	protected void convertMappingDeclaration(Element md, boolean oldStyleMap) {
		MappingObject mo = new MappingObject(md);
		HashMap<String, Variable> variablesInScope = new HashMap<String, Variable>();
		boolean emptySMOInput = true;

		for (Element input : mo.inputs) {
			emptySMOInput &= !convertMappingRootObject(input, inputSMOs, variablesForInput, variablesInScope);
		}
		for (Element output : mo.outputs) {
			convertMappingRootObject(output, outputSMOs, variablesForOutput, variablesInScope);
		}
		for (Element e : mo.others) {
			convertMappingObjectUnderMappingDeclaration(e, variablesInScope);
		}

		if (mo.inputs.size() > 0 && !emptySMOInput) {
			String propertiesElement = oldStyleMap ? "mapping" : "move"; //$NON-NLS-1$ //$NON-NLS-2$
			Element propertiesMove = md.getOwnerDocument().createElement(propertiesElement);
			Element input = md.getOwnerDocument().createElement("input"); //$NON-NLS-1$
			input.setAttribute("path", "Properties"); //$NON-NLS-1$ //$NON-NLS-2$
			Element output = md.getOwnerDocument().createElement("output"); //$NON-NLS-1$
			output.setAttribute("path", "Properties"); //$NON-NLS-1$ //$NON-NLS-2$
			propertiesMove.appendChild(input);
			propertiesMove.appendChild(output);
			md.appendChild(propertiesMove);
		}
	}

	protected void convertMappingObjectUnderMappingDeclaration(Element e, HashMap<String, Variable> variablesFromParent) {
		MappingObject mo = new MappingObject(e);
		HashMap<String, Variable> variablesInScope = new HashMap<String, Variable>();

		addToVariables(e, null, variablesInScope);

		for (Element input : mo.inputs) {
			convertSecondLevelMappingObjectUnderMappingDeclaration(input, variablesFromParent);
		}
		for (Element output : mo.outputs) {
			convertSecondLevelMappingObjectUnderMappingDeclaration(output, variablesFromParent);
		}
	}

	protected boolean convertMappingRootObject(Element e, HashMap<String, SMOStructure> smos,
			HashMap<IFile, String> variablesForIO, HashMap<String, Variable> variablesInScope) {
		String path = e.getAttribute("path"); //$NON-NLS-1$
		SMOStructure smo = getSMO(smos, path);
		if (smo == null) {
			return false;
		}
		if (isEmpty(smo)) {
			// remove the element.
			e.getParentNode().removeChild(e);
			return false;
		}

		addToVariables(e, smo, variablesInScope);

		// set up correlation context
		// if (ConversionUtils.hasValue(smo.correlationContext)) {
		// String type = smo.correlationContext;
		// type = findXSDType(type);
		// if (type == null) {
		// return false;
		// }
		// Element e1 = e.getOwnerDocument().createElement("cast");
		// e.appendChild(e1);
		// e1.setAttribute("path",
		// "LocalEnvironment/Variables/context/correlation/any");
		// e1.setAttribute("qualifier", type);
		// }
		//
		if (smo.xpath.equals("/body")) { //$NON-NLS-1$
			convertBody(e, smo, variablesForIO);
		} else if (smo.xpath.equals("/")) { //$NON-NLS-1$
			convertBody(e, smo, variablesForIO);
		} else if (smo.xpath.equals("smo")) { //$NON-NLS-1$
			convertBody(e, smo, variablesForIO);
		} else {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoUnsupportedXPathMappingObject, smo.xpath));
			return false;
		}

		return true;
	}

	private boolean isEmpty(SMOStructure smo) {
		return !ConversionUtils.hasValue(smo.message);
	}

	protected String findXSDType(String type) {
		if (context.indexer.xsdElements.get(type) != null) {
			return type;
		}
		userLog.addEntry(targetFile, new TodoEntry(NLS.bind(WESBConversionMessages.todoUnresolvedElement, type)));
		return null;
	}

	protected void addToVariables(Element e, SMOStructure smo, HashMap<String, Variable> variablesInScope) {
		String var = e.getAttribute("var"); //$NON-NLS-1$
		if (var == null) {
			var = ""; //$NON-NLS-1$
		}
		variablesInScope.put(var, new Variable(e, smo));
	}

	protected SMOStructure getSMO(HashMap<String, SMOStructure> smos, String path) {
		String var = ""; //$NON-NLS-1$
		SMOStructure smo = null;
		if (path.startsWith("$")) { //$NON-NLS-1$
			int index = path.indexOf("/"); //$NON-NLS-1$
			var = path.substring(1, index);
			smo = smos.get(var);
		} else {
			smo = smos.get(""); //$NON-NLS-1$
			var = path;
		}
		if (smo == null) {
			successfulConversion = false;
			userLog.addEntry(targetFile, new TodoEntry(NLS.bind(WESBConversionMessages.todoUnresolvedSMO, var)));
			return null;
		}
		return smo;
	}

	protected void convertSecondLevelMappingObjectUnderMappingDeclaration(Element e, HashMap<String, Variable> variables) {
		String path = e.getAttribute("path"); //$NON-NLS-1$
		if (path == null) {
			path = ""; //$NON-NLS-1$
		}

		// retrieve corresponding SMO and parent IO element.
		String var = ""; //$NON-NLS-1$
		SMOStructure smo = null;
		Variable v = null;
		if (path.startsWith("$")) { //$NON-NLS-1$
			int index = path.indexOf("/"); //$NON-NLS-1$
			var = path.substring(1, index - 1);
			v = variables.get(var);
		} else {
			v = variables.get(""); //$NON-NLS-1$
			var = e.getNodeName();
		}
		if (v == null || v.smo == null) {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoUnresolvedSMO, var));
			return;
		}
		smo = v.smo;

		// process path
		if (path != null && path.startsWith("body/")) { //$NON-NLS-1$
			// starts with body
			e.setAttribute("path", path.substring("body/".length())); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (path != null && path.equals("body")) { //$NON-NLS-1$
			e.setAttribute("path", "."); //$NON-NLS-1$ //$NON-NLS-2$
			// } else if (path != null &&
			// path.startsWith("context/correlation/")) {
			// if (ConversionUtils.hasValue(smo.correlationContext)) {
			// String newPath = pathMappings.get("/context/correlation");
			// String type = smo.correlationContext;
			// IFile xsdFile = context.indexer.xsdElements.get(type);
			// newPath += "/" + ConversionUtils.getLocalPart(type) +
			// path.substring("context/correlation".length());
			// e.setAttribute("path", newPath);
			// }
		} else if (path != null && path.startsWith("context/")) { //$NON-NLS-1$
			// non-supported context
			convertToDoTaskInMap(e,
					NLS.bind(WESBConversionMessages.mapTodoMapContextRelativePath, path, e.getParentNode().getNodeName()), path,
					"Properties"); //$NON-NLS-1$
			successfulConversion = false;
		} else if (path != null && path.startsWith("header/")) { //$NON-NLS-1$
			// headers
			convertToDoTaskInMap(e, NLS.bind(WESBConversionMessages.mapTodoMapHeader, e.getParentNode().getNodeName()),
					"headers", "Properties"); //$NON-NLS-1$ //$NON-NLS-2$
			successfulConversion = false;
		} else if (path != null && path.equals("context")) { //$NON-NLS-1$
			// add a todo
			convertToDoTaskInMap(e, NLS.bind(WESBConversionMessages.mapTodoMapContext, e.getParentNode().getNodeName()),
					"context", "Properties"); //$NON-NLS-1$ //$NON-NLS-2$
			successfulConversion = false;
		} else if (path != null && path.equals("headers")) { //$NON-NLS-1$
			// headers
			convertToDoTaskInMap(e, NLS.bind(WESBConversionMessages.mapTodoMapHeader, e.getParentNode().getNodeName()),
					"headers", "Properties"); //$NON-NLS-1$ //$NON-NLS-2$
			successfulConversion = false;
		} else if (path != null && path.equals("attachments")) { //$NON-NLS-1$
			successfulConversion = false;
			addTaskIfNecessary(WESBConversionMessages.todoAttachmentsNotSupported);
		} else {
			return;
		}

	}

	protected void addTaskIfNecessary(String message) {
		if (todoTasksAdded.contains(message)) {
			return;
		}
		userLog.addEntry(targetFile, new TodoEntry(message));
		todoTasksAdded.add(message);
	}

	protected void convertToDoTaskInMap(Element e, String taskDocumentation, String replacingPath, String pathToReplace) {
		Document doc = e.getOwnerDocument();
		Element task = doc.createElement("task"); //$NON-NLS-1$
		Element parent = (Element) e.getParentNode();
		Element grandParent = (Element) parent.getParentNode();
		task.setAttribute("type", "todo"); //$NON-NLS-1$ //$NON-NLS-2$
		Element documentation = doc.createElement("documentation"); //$NON-NLS-1$
		Text text = doc.createTextNode(taskDocumentation);
		documentation.appendChild(text);
		task.appendChild(documentation);
		grandParent.removeChild(parent);
		grandParent.appendChild(task);
		MappingObject mo = new MappingObject(parent);
		for (Element o : mo.inputs) {
			task.appendChild(o);
			String path = o.getAttribute("path"); //$NON-NLS-1$
			if (replacingPath.equals(path)) {
				o.setAttribute("path", pathToReplace); //$NON-NLS-1$
			}
		}
		for (Element o : mo.outputs) {
			task.appendChild(o);
			String path = o.getAttribute("path"); //$NON-NLS-1$
			if (replacingPath.equals(path)) {
				o.setAttribute("path", pathToReplace); //$NON-NLS-1$
			}
		}
		addTaskIfNecessary(taskDocumentation);
	}

	protected void convertBody(Element e, SMOStructure smo, HashMap<IFile, String> variablesForIO) {
		// boolean hasCorrelationContext =
		// ConversionUtils.hasValue(smo.correlationContext);

		List<String> parts = getWSDLMessageParts(smo.message);
		if (parts == null) {
			return;
		}

		String body = parts.get(0);
		IFile xsdFile = getXSDFile(body);
		if (xsdFile == null) {
			return;
		}

		String var = variablesForIO.get(xsdFile);
		if (var == null) {
			successfulConversion = false;
			userLog.addEntry(targetFile, new TodoEntry(NLS.bind(WESBConversionMessages.todoUnresolvedVarForElement, body)));
			return;
		}
		e.setAttribute("namespace", ConversionUtils.getNamespace(body)); //$NON-NLS-1$
		// e.setAttribute("path", "$" + var + "/mb:msg(" +
		// ConversionUtils.getLocalPart(body) + ",assembly,XMLNSC,"
		// + (hasCorrelationContext ? "LocalEnvironment," : "") +
		// "Properties)");
		e.setAttribute("path", "$" + var + "/mb:msg(" + ConversionUtils.getLocalPart(body) + ",assembly,XMLNSC," + "Properties)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	protected IFile getXSDFile(String type) {
		IFile xsdFile = context.indexer.xsdElements.get(type);
		if (xsdFile == null) {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoUnresolvedElement, type));
			return null;
		}
		return xsdFile;
	}

	protected IFile findXSDFile(String type) {
		IFile file = context.indexer.xsdElements.get(type);
		if (file == null) {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoUnresolvedElement, type));
		}
		return file;
	}

	protected List<String> getWSDLMessageParts(String message) {
		List<String> parts = context.indexer.wsdlMessages.get(message);
		if (parts == null) {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoUnresolvedWSDLMessage, message));
			return null;
		}
		if (parts.size() > 1) {
			successfulConversion = false;
			addTaskIfNecessary(NLS.bind(WESBConversionMessages.todoMultiPartWSDLMessage, message));
			return null;
		}
		return parts;
	}

	public static List<WESBMap> getMapsToConvert(WESBMaps maps) {
		List<WESBMap> result = new ArrayList<WESBMap>();
		for (WESBMap map : maps.getAllMaps()) {
			if (map.isTobeConverted()) {
				result.add(map);
			}
		}
		return result;
	}

}
