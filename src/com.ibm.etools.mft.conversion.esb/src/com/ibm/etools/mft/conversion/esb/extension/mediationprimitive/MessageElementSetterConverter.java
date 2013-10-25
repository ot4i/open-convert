/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2013" 
 *  crc="1565004725" > 
 *  IBM Confidential 
 *   
 *  OCO Source Materials 
 *   
 *  5724-E11,5724-E26 
 *   
 *  (C) Copyright IBM Corp. 2013 
 *   
 *  The source code for the program is not published 
 *  or otherwise divested of its trade secrets, 
 *  irrespective of what has been deposited with the 
 *  U.S. Copyright Office. 
 *  </copyright> 
 ************************************************************************/
package com.ibm.etools.mft.conversion.esb.extension.mediationprimitive;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.osgi.util.NLS;

import com.ibm.broker.config.appdev.Terminal;
import com.ibm.broker.config.appdev.nodes.JavaComputeNode;
import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extensionpoint.AbstractMediationPrimitiveConverter;
import com.ibm.etools.mft.conversion.esb.extensionpoint.Nodes;
import com.ibm.etools.mft.conversion.esb.extensionpoint.PrimitiveManager;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;
import com.ibm.etools.mft.conversion.esb.model.mfc.Table;
import com.ibm.etools.mft.uri.protocol.PlatformProtocol;

/**
 * This maps the WESB MessageElementSetter into a Java Compute Node.
 * 
 * It assumes that the MessageElementSetter has been debugged in WESB and does
 * not produce any run-time errors, so there are no checks for run-time errors
 * in this code (errors such as assigning a value of an incompatible type).
 * 
 * We don't have access to the message schema so we can't generate the Java code
 * necessary to perform the MessageElementSetter actions. Instead, we generate a
 * JAXB Java class which simply copies the input message to the output. It
 * contains comments telling the user how to update the class to get it working
 * with generated JAXB classes, and contains comments giving details of each
 * MessageElementSetter action.
 * 
 * @author Zhongming Chen, Chris Kalus
 * 
 */
public class MessageElementSetterConverter extends AbstractMediationPrimitiveConverter {

	final String EOL = "\n"; //$NON-NLS-1$
	final String EOL_TABS = "\n\t\t\t"; //$NON-NLS-1$
	final String QUOTE = "\""; //$NON-NLS-1$
	final String BODY_PATH = "/body/"; //$NON-NLS-1$

	final String COPY_ACTION = "copy"; //$NON-NLS-1$
	final String DELETE_ACTION = "delete"; //$NON-NLS-1$
	final String APPEND_ACTION = "append"; //$NON-NLS-1$

	/**
	 * 
	 */
	public MessageElementSetterConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.etools.mft.conversion.esb.extensionpoint.
	 * IMediationPrimitiveHandler#getType()
	 */
	@Override
	public String getType() {
		return "MessageElementSetter"; //$NON-NLS-1$
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		Nodes nodes = createNodes(converterContext);
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);
		// get the target IIB Project
		IFile targetFile = getTargetFlowFile(converterContext);
		IProject targetProject = targetFile.getProject();
		// create the JavaComputNode Project
		IProject jcnProject = CreateJCNProject.create(targetProject);
		// create the JavaCompute node
		JavaComputeNode node = (JavaComputeNode) PrimitiveManager.createNode(converterContext.targetFlow, nodeName, ROLE_MAIN,
				JavaComputeNode.class, nodes); //$NON-NLS-1$
		String className = converterContext.targetFlow.getName() + "_" + nodeName; //$NON-NLS-1$
		node.setJavaClass(className);
		// create the Java file for the JavaComputNode project
		IFile file = jcnProject.getFile(new Path(className + ".java")); //$NON-NLS-1$
		String content = ConversionUtils.loadTemplate("MessageElementSetter.java.template"); //$NON-NLS-1$
		String moduleName = file.getFullPath().removeFileExtension().lastSegment();
		URI uri = converterContext.component.eResource().getURI();
		IFile sourceFile = PlatformProtocol.getWorkspaceFile(uri);
		if (sourceFile != null) {
			addSourceToTargetResource(sourceFile, file);
		}

		// get the table of actions from the MessageElementSetter
		Table p = (Table) getPropertyOfSourcePrimitive(converterContext, "messageElements"); //$NON-NLS-1$
		StringBuffer statements = new StringBuffer();
		if (p != null) {
			List<Row> rows = p.getRow();
			if (rows.size() > 0) {
				// Issue a ToDo message for converting the MessageElementSetter
				// actions
				String primitiveName = converterContext.sourcePrimitive.getName();
				createToDoTask(file,
						NLS.bind(WESBConversionMessages.todoConvertMessageElementActions, new Object[] { primitiveName, nodeName }));

				// iterate through the table entries, creating Java content for
				// each
				// one
				for (Row r : rows) {
					Property targetProp = getColumnValueInARow(r, "target"); //$NON-NLS-1$
					String target = (targetProp != null) ? targetProp.getValue() : ""; //$NON-NLS-1$

					Property typeProp = getColumnValueInARow(r, "type"); //$NON-NLS-1$
					String type = (typeProp != null) ? typeProp.getValue() : ""; //$NON-NLS-1$

					Property valueProp = getColumnValueInARow(r, "value"); //$NON-NLS-1$
					String value = (valueProp != null) ? valueProp.getValue() : ""; //$NON-NLS-1$

					// remove "/body/" from the target path
					target = removeBodyFromPath(target, file, primitiveName, nodeName);

					if (COPY_ACTION.equals(type)) {
						// remove "/body/" from the value path
						value = removeBodyFromPath(value, file, primitiveName, nodeName);

						statements.append(EOL_TABS);
						statements.append("// COPY " + value + " to " + target + EOL); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (DELETE_ACTION.equals(type)) {
						statements.append(EOL_TABS);
						statements.append("// DELETE (assign a null to) " + target + EOL); //$NON-NLS-1$
					} else if (APPEND_ACTION.equals(type)) {
						// remove "/body/" from the value path
						value = removeBodyFromPath(value, file, primitiveName, nodeName);

						statements.append(EOL_TABS);
						statements.append("// APPEND " + value + " to list " + target + EOL); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						// Set
						statements.append(EOL_TABS);
						statements.append("// SET " + target + " to " + QUOTE + value + QUOTE + EOL); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}

		content = NLS.bind(content, new Object[] { moduleName, statements.toString() });
		ConversionUtils.writeToFile(file, content);

		node.setProperty("javaClass", className); //$NON-NLS-1$

		return nodes;
	}

	/*
	 * Remove the leading "/body/" from the path
	 */
	String removeBodyFromPath(String path, IFile file, String primitiveName, String nodeName) {
		if (path != null && path.startsWith(BODY_PATH)) {
			path = path.substring(BODY_PATH.length());
		} else {
			// Unsupported path, issue a ToDo
			createToDoTask(file, NLS.bind(WESBConversionMessages.todoUnsupportedMessageElementPath, new Object[] { primitiveName,
					path, nodeName }));
		}
		return path;
	}

	@Override
	public Terminal getInputTerminal(String sourceName, Nodes nodes) {
		return ((JavaComputeNode) nodes.getNode(ROLE_MAIN)).INPUT_TERMINAL_IN; //$NON-NLS-1$
	}

	@Override
	public Terminal getOutputTerminal(String sourceName, Nodes nodes) {
		if ("Out".equals(sourceName)) { //$NON-NLS-1$
			return ((JavaComputeNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_OUT; //$NON-NLS-1$
		} else {
			return ((JavaComputeNode) nodes.getNode(ROLE_MAIN)).OUTPUT_TERMINAL_FAILURE; //$NON-NLS-1$
		}
	}

	@Override
	public String getConvertedTo() {
		return "JavaCompute"; //$NON-NLS-1$
	}
}
