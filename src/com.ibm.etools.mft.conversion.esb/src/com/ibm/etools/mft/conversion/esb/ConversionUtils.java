package com.ibm.etools.mft.conversion.esb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.etools.mft.conversion.esb.extensionpoint.ConversionContext;
import com.ibm.etools.mft.conversion.esb.model.GlobalConfigurationType;
import com.ibm.etools.mft.conversion.esb.model.WESBConversionDataType;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.mfc.MediationFlow;
import com.ibm.etools.mft.util.PrivilegedAccessor;
import com.ibm.etools.mft.util.WMQIConstants;
import com.ibm.ws.sibx.scax.mediation.model.ComponentFlows;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionUtils implements WESBConversionConstants {

	public static WESBConversionDataType loadWESBConversionModel(String s) {
		try {
			if (s != null) {
				return (WESBConversionDataType) loadModel(s, PACKAGE_WESB_CONVERSION_TYPE);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return new WESBConversionDataType();
	}

	public static Object loadModel(String s, String packageName) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Unmarshaller u = jc.createUnmarshaller();
		JAXBElement<Object> doc = (JAXBElement<Object>) u.unmarshal(new StringReader(s));
		Object v = doc.getValue();

		return v;
	}

	public static String saveModel(JAXBElement model, String packageName) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(packageName);
			Marshaller m = jc.createMarshaller();
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			m.marshal(model, o);

			return new String(o.toByteArray(), "UTF-8"); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	public static String getESBProjectType(IProject p) {
		return isESBLib(p) ? LIB : MODULE;
	}

	public static IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public static IResource getResource(IPath path) {
		if (path.segmentCount() == 1) {
			return getProject(path.toString());
		} else {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		}
	}

	public static WESBProject getWESBProject(WESBConversionDataType model, IProject project) {
		for (WESBProject p : model.getSourceProjects()) {
			if (p.getName().equals(project.getName())) {
				return p;
			}
		}
		return null;
	}

	public static String readStream(InputStream i, String encoding) {
		try {
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			int c = 0;
			while ((c = i.read()) != -1) {
				o.write(c);
			}
			o.flush();
			if (encoding != null) {
				return new String(o.toByteArray(), encoding); //$NON-NLS-1$
			} else {
				return new String(o.toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String readFile(IFile file) {
		try {
			return readStream(file.getContents(), "UTF-8"); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		return null;
	}

	public static String saveXML(Document dom) throws Exception {
		DOMSource domSource = new DOMSource(dom);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(out);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		serializer.transform(domSource, streamResult);
		return out.toString("UTF-8"); //$NON-NLS-1$
	}

	public static Document loadXML(InputStream contents) throws Exception {
		String s = readStream(contents, "UTF-8"); //$NON-NLS-1$
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document dom = db.parse(new InputSource(new StringReader(s)));
		return dom;
	}

	public static Element getFirstElement(Element e, String namespace, String name) {
		NodeList nl = e.getElementsByTagName(name);
		if (nl.getLength() > 0) {
			return (Element) nl.item(0);
		} else {
			return null;
		}
	}

	public static boolean hasValue(String s) {
		return s != null && s.length() > 0;
	}

	public static String escapeXMLString(String text) {
		if (text != null) {
			// & and < need to be encoded, and > needs to be encoded
			// under certain circumstances so to be safe let's always
			// encode it:
			text = text.replace("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
			text = text.replace("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			text = text.replace(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			// need to encode single-quote and double-quote to allow
			// a mixture of both to be used:
			text = text.replace("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
			text = text.replace("'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
			return text;
		}
		return ""; //$NON-NLS-1$
	}

	public static String getNamespace(String qname) {
		int index = qname.indexOf("}"); //$NON-NLS-1$
		if (index < 0) {
			return ""; //$NON-NLS-1$
		} else {
			return qname.substring(1, index);
		}
	}

	public static String getLocalPart(String qname) {
		int index = qname.indexOf("}"); //$NON-NLS-1$
		if (index < 0) {
			return qname;
		} else {
			return qname.substring(index + 1);
		}
	}

	public static List<Element> getImmediateChild(Element parent, String ns, String n) {
		List<Element> kids = new ArrayList<Element>();
		NodeList nl = null;
		if (ns != null) {
			nl = parent.getElementsByTagNameNS(ns, n);
		} else {
			nl = parent.getElementsByTagName(n);
		}
		for (int i = 0; i < nl.getLength(); i++) {
			Node o = nl.item(i);
			if (o.getParentNode() == parent) {
				kids.add((Element) o);
			}
		}
		return kids;
	}

	public static String getQName(String ns, String n) {
		return "{" + ns + "}" + n; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getXSIType(Element e) {
		return e.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getContent(IFile file) throws Exception {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		int c = 0;
		InputStream in = file.getContents();
		while ((c = in.read()) != -1) {
			o.write(c);
		}
		return o.toString();
	}

	public static boolean isESBProject(IProject p) {
		return isESBLib(p) || isESBModule(p);
	}

	public static boolean isESBLib(IProject p) {
		try {
			return p.isOpen() && (p.hasNature(ESB_LIB_NATURE) || p.hasNature(WESB_LIBRARY_NATURE));
		} catch (CoreException e) {
			return false;
		}
	}

	public static boolean isESBModule(IProject p) {
		try {
			return p.isOpen() && (p.hasNature(ESB_MODULE_NATURE) || p.hasNature(MEDIATION_MODULE_NATURE));
		} catch (CoreException e) {
			return false;
		}
	}

	public static WESBProject getESBProject(ConversionContext context) {
		return ConversionUtils.getWESBProject(context.model, context.resource.getProject());
	}

	public static byte[] readZipContent(ZipInputStream zis, ZipEntry entry) throws Exception {
		String method = "readZipContent"; //$NON-NLS-1$
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int BUFFER = 2048;
		byte[] buffer = new byte[BUFFER];
		int count = 0;
		while ((count = zis.read(buffer, 0, BUFFER)) != -1) {
			out.write(buffer, 0, count);
		}
		return out.toByteArray();
	}

	public static String removeAllHTMLMarkersAndLRCF(String message) {
		if (message == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer sb = new StringBuffer();
		int start = message.indexOf("<"); //$NON-NLS-1$
		int end = 0;
		while (start >= 0) {
			sb.append(message.substring(end, start));
			start = message.indexOf(">", start); //$NON-NLS-1$
			end = start + 1;
			start = message.indexOf("<", end); //$NON-NLS-1$
		}
		sb.append(message.substring(end));
		return sb.toString().replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
	}

	public static void createFolder(File file) {
		file.getParentFile().mkdirs();
	}

	public static void writeToFile(IFile file, String content) throws CoreException {
		createFolder(file.getLocation().toFile());
		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
		if (file.exists()) {
			file.setContents(in, true, false, new NullProgressMonitor());
		} else {
			file.create(in, true, new NullProgressMonitor());
		}
	}

	public static String loadTemplate(String path) {
		URL url = FileLocator.find(WESBConversionPlugin.getDefault().getBundle(), new Path("/templates/" + path), //$NON-NLS-1$
				Collections.EMPTY_MAP);
		if (url != null) {
			try {
				return readStream(url.openStream(), "UTF-8"); //$NON-NLS-1$
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static List<IProject> getClassPathReference(IProject source) throws Exception {
		List<IProject> ps = new ArrayList<IProject>();
		IFile classPath = source.getFile(new Path(".classpath")); //$NON-NLS-1$
		if (classPath.exists()) {
			Document dom = loadXML(classPath.getContents());

			NodeList cpes = dom.getElementsByTagName("classpathentry"); //$NON-NLS-1$

			if (cpes != null) {
				for (int i = 0; i < cpes.getLength(); i++) {
					Element e = (Element) cpes.item(i);
					if ("src".equals(e.getAttribute("kind"))) { //$NON-NLS-1$ //$NON-NLS-2$
						IProject rp = null;
						try {
							rp = ConversionUtils.getResource(new Path(e.getAttribute("path"))).getProject(); //$NON-NLS-1$
							ps.add(rp);
						} catch (Throwable ex) {
							// do nothing
						}
					}
				}
			}
		}

		return ps;
	}

	public static void setSingleNatureAndRemoveBuilders(IProject project) {
		// Project is not opened yet
		try {
			IFile dotProject = project.getFile(new Path(".project")); //$NON-NLS-1$
			if (dotProject.exists()) {
				Document dom = loadXML(dotProject.getContents());

				String natureId = getMedModuleOrLibraryNatureId(dom);
				if (natureId == null) {
					// Do nothing
					return;
				}

				NodeList buildSpecs = dom.getElementsByTagName("buildSpec"); //$NON-NLS-1$
				if (buildSpecs != null) {
					for (int i = 0; i < buildSpecs.getLength(); i++) {
						Element e = (Element) buildSpecs.item(i);
						removeAllChildren(e);
					}
				}

				NodeList naturess = dom.getElementsByTagName("natures"); //$NON-NLS-1$
				if (naturess != null) {
					for (int i = 0; i < naturess.getLength(); i++) {
						Element e = (Element) naturess.item(i);
						removeAllChildren(e);
						// Add new nature
						Element newChild = dom.createElement("nature"); //$NON-NLS-1$
						Text text = dom.createTextNode(natureId);
						newChild.appendChild(text);
						e.appendChild(newChild);
					}
				}
				String content = ConversionUtils.saveXML(dom);
				ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
				dotProject.setContents(in, true, false, new NullProgressMonitor());
			}
		} catch (Exception e) {
			// Do nothing
		}
	}

	private static String getMedModuleOrLibraryNatureId(Document dom) {
		String natureId = null;
		NodeList natures = dom.getElementsByTagName("nature"); //$NON-NLS-1$
		if (natures != null) {
			for (int i = 0; i < natures.getLength(); i++) {
				Node nature = natures.item(i);
				NodeList children = nature.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					if (child instanceof Text) {
						if (child.getNodeValue().equals(ESB_LIB_NATURE) || child.getNodeValue().equals(ESB_MODULE_NATURE)) {
							natureId = child.getNodeValue();
							break;
						}
					}
				}
			}
		}
		return natureId;
	}

	private static void removeAllChildren(Node node) {
		NodeList children = node.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node n = children.item(i);
			if (n.hasChildNodes()) {
				removeAllChildren(n);
			}
			node.removeChild(n);
		}
	}

	public static Object convertFromOldModelToNewModel(ComponentFlows compFlows) throws Exception {
		MediationFlow mfc = new MediationFlow();

		FlowFormatConverter converter = new FlowFormatConverter(compFlows, mfc);
		converter.convert();
		// create interface

		return mfc;
	}

	public static String encodeName(String name) {
		if ( name == null ) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (!(Character.isLetterOrDigit(c) || c == '_')) {
				sb.append("_"); //$NON-NLS-1$
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static void deleteProject(IProject p) {
		try {
			p.delete(true, new NullProgressMonitor());
		} catch (CoreException e) {
			try {
				p.delete(true, new NullProgressMonitor());
			} catch (CoreException e1) {
				try {
					p.delete(true, new NullProgressMonitor());
				} catch (CoreException e2) {
				}
			}
		}
	}

	public static boolean isJava(IResource resource) {
		return "java".equalsIgnoreCase(resource.getFileExtension()); //$NON-NLS-1$
	}

	public static boolean isMap(IResource resource) {
		return "map".equalsIgnoreCase(resource.getFileExtension()); //$NON-NLS-1$
	}

	public static Object getObjectForClass(List list, Class clazz) {
		for (Object k : list) {
			if (k.getClass() == clazz) {
				return k;
			}
		}
		return null;
	}

	public static Object getObjectForClassAndName(List list, Class clazz, String name, String nameAttr) {
		for (Object k : list) {
			try {
				if (k.getClass() == clazz && name != null && name.equals(PrivilegedAccessor.getField(k, nameAttr))) {
					return k;
				}
			} catch (Exception e) {
				continue;
			}
		}
		return null;
	}

	public static String getUsage(List<String> usages) {
		StringBuffer sb = new StringBuffer();
		for (String s : usages) {
			if (sb.length() > 0) {
				sb.append(","); //$NON-NLS-1$
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static void addUsage(HashMap<String, List<String>> usages, String mfcName, String key) {
		List<String> usage = usages.get(key);
		if (usage == null) {
			usages.put(key, usage = new ArrayList<String>());
		}
		usage.add(mfcName);
	}

	public static void addUsages(HashMap<String, List<String>> usages, List list, Class clazz, String key, boolean toCreate) {
		try {
			for (String type : usages.keySet()) {
				Object object = ConversionUtils.getObjectForClassAndName(list, clazz, type, key);
				if (object == null) {
					if (!toCreate) {
						continue;
					}
					object = clazz.newInstance();
					PrivilegedAccessor.setValue(object, key, type);
					list.add(object);
				}
				for (String usage : usages.get(type)) {
					List<String> us = (List<String>) PrivilegedAccessor.getField(object, "usages"); //$NON-NLS-1$
					if (us == null) {
						us = new ArrayList<String>();
						PrivilegedAccessor.setValue(object, "usages", us); //$NON-NLS-1$
					}
					if (!us.contains(usage)) {
						us.add(usage);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getDefaultTargetProjectName(String name) {
		return "IIB_" + name; //$NON-NLS-1$
	}

	public static IFile getLogFile(IFile conversionFile, WESBConversionDataType model) {
		if (model.getGlobalConfiguration().isMergeResult()) {
			return conversionFile.getParent().getFile(
					new Path(conversionFile.getFullPath().removeFileExtension().lastSegment() + ".conversion_results.genres")); //$NON-NLS-1$
		} else {
			return conversionFile.getParent().getFile(
					new Path(conversionFile.getFullPath().removeFileExtension().lastSegment() + ".conversion_results.genres")); //$NON-NLS-1$
		}
	}

	private static String formatTime() {
		Date d = new Date();
		return (Calendar.getInstance().get(Calendar.YEAR))
				+ formatField(Calendar.getInstance().get(Calendar.MONTH) + 1)
				+ formatField(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
				+ "-" //$NON-NLS-1$
				+ formatField(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
				+ formatField(Calendar.getInstance().get(Calendar.MINUTE))
				+ formatField(Calendar.getInstance().get(Calendar.SECOND));
	}

	private static String formatField(int i) {
		if (i > 9) {
			return "" + i; //$NON-NLS-1$
		} else {
			return "0" + i; //$NON-NLS-1$
		}
	}

	public static WESBConversionDataType createNewConversion() {
		WESBConversionDataType data = new WESBConversionDataType();
		data.setGlobalConfiguration(new GlobalConfigurationType());
		data.getGlobalConfiguration().setIncludeReferencedProject(true);
		return data;
	}

	public static List<WESBProject> getAllWESBProjectsToConvert(List<WESBProject> sourceProjects) {
		List<WESBProject> result = new ArrayList<WESBProject>();
		for (WESBProject p : sourceProjects) {
			if (p.isToConvert()) {
				result.add(p);
			}
		}
		return result;
	}

	public static String convertLRToHTML(String message) {
		String s = message;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\n') {
				sb.append("<br/>"); //$NON-NLS-1$
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String addHTMLBullet(String s) {
		return "<li>" + s + "</li>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void createMethodFromTemplate(IType type, String templateName, IProgressMonitor monitor, Object... variables)
			throws JavaModelException {
		String code = String.format(ConversionUtils.loadTemplate(templateName), variables);
		type.createMethod(code, null, false, new SubProgressMonitor(monitor, 2));
	}

	public static void createMethodFromTemplate(IType type, String templateName, IProgressMonitor monitor)
			throws JavaModelException {
		String code = ConversionUtils.loadTemplate(templateName);
		type.createMethod(code, null, false, new SubProgressMonitor(monitor, 2));
	}

	public static boolean isJAR(IResource resource) {
		return "jar".equalsIgnoreCase(resource.getFileExtension()); //$NON-NLS-1$
	}

	public static String getXMIType(Element e) {
		return e.getAttributeNS("http://www.omg.org/XMI", "type"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean canMapBeUnchecked(WESBMap m, WESBMaps maps) {
		if (m.getUsages().size() == 0) {
			return true;
		}
		HashMap<String, WESBMap> ms = new HashMap<String, WESBMap>();
		for (WESBMap m1 : maps.getAllMaps()) {
			ms.put(m1.getName(), m1);
		}
		for (String u : m.getUsages()) {
			if (u.toLowerCase().endsWith(".map")) { //$NON-NLS-1$
				// referenced by a map.
				if (ms.get(u) != null && ms.get(u).isTobeConverted()) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public static List<WESBMap> ensureReferencedMapAreChecked(WESBMap m, WESBMaps maps) {
		List<WESBMap> checked = new ArrayList<WESBMap>();
		for (WESBMap m1 : maps.getAllMaps()) {
			for (String u : m1.getUsages()) {
				if (u.equals(m.getName())) {
					if (!m1.isTobeConverted()) {
						m1.setTobeConverted(true);
						checked.add(m1);
					}
				}
			}
		}
		return checked;
	}

	public static String readRawFile(IFile file) {
		try {
			return readStream(file.getContents(), null);
		} catch (CoreException e) {
		}
		return null;
	}

	public static String getFullyQualifiedFlowName(MessageFlow flow) {
		return getFullyQualifiedFlowName(flow.getBrokerSchema(), flow.getName());
	}

	public static String getFullyQualifiedFlowName(String folderName, String flowName) {
		return (hasValue(folderName) ? folderName + "/" : "") + flowName; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void closeLogFile() {
		if (conversionLog != null) {
			Handler[] handlers = conversionLog.getHandlers();
			for (int i = 0; i < handlers.length; i++)
				handlers[i].close();
		}
	}

	public static void copy(IFile s, IFile t) {
		try {
			InputStream in = s.getContents();
			if (t.exists()) {
				t.setContents(in, true, false, new NullProgressMonitor());
			} else {
				IContainer parent = t.getParent();
				createFolder(parent);
				t.create(in, true, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static void createFolder(IContainer parent) throws CoreException {
		if (parent instanceof IProject) {
			return;
		}
		if (!parent.exists()) {
			IFolder f = (IFolder) parent;
			createFolder(f.getParent());
			f.create(true, true, new NullProgressMonitor());
		}
	}

	public static String getExceptionMessage(Throwable e) {
		String s = null;
		if (e instanceof ESBConversionException) //$NON-NLS-1$
		{
			s = e.getMessage();
			openLogFile("log", "Conversion.log");
			conversionLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			closeLogFile();
		} else {
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(o));
			s = new String(o.toByteArray());
		}
		return s;
	}

	public static boolean isWSDL(IResource resource) {
		return (resource instanceof IFile) && (WMQIConstants.WSDL_FILE_EXTENSION_NO_DOT.equals(resource.getFileExtension()));
	}

	public static boolean isXSD(IResource resource) {
		return (resource instanceof IFile) && (WMQIConstants.XSD_FILE_EXTENSION_NO_DOT.equals(resource.getFileExtension()));
	}

	public static boolean isXSDOrWSDL(IResource resource) {
		return isWSDL(resource) || isXSD(resource);
	}

	public static void openLogFile(String logId, String logFileName) {
		/* Set up the logging - One 1MB file, with timestamps, plain text */
		conversionLog = Logger.getLogger(logId);
		conversionLog.setLevel(Level.INFO);
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = workspaceRoot.getLocation();
			path = path.append(new Path(".metadata/" + logFileName));

			Handler fh = new FileHandler(path.toOSString());
			fh.setEncoding(null);
			fh.setFormatter(new SimpleFormatter());
			conversionLog.addHandler(fh);
		} catch (IOException e) {
			// TODO do nothing
		}
	}

	private static Logger conversionLog = null;

}
