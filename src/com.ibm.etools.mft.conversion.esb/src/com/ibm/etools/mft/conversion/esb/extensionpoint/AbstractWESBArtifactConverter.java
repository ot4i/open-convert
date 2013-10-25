/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.io.IOException;

import org.eclipse.core.resources.IFile;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.WESBConversionConstants;
import com.ibm.etools.mft.conversion.esb.model.mfc.FailTerminal;
import com.ibm.etools.mft.conversion.esb.model.mfc.Terminal;
import com.ibm.etools.mft.conversion.esb.userlog.DebugEntry;
import com.ibm.etools.mft.conversion.esb.userlog.TodoEntry;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class AbstractWESBArtifactConverter implements WESBConversionConstants, IWESBArtifactConverter {

	public static String ROLE_MAIN = "main";
	public static String ROLE_ENTRY = "entry";
	public static String ROLE_EXIT = "exit";

	protected ConversionContext conversionContext;

	/**
	 * 
	 */
	public AbstractWESBArtifactConverter() {
	}

	protected String getTerminalName(Terminal t) {
		if (t instanceof FailTerminal) {
			return t.getName() != null ? FAIL_TERMINAL + "_" + t.getName() : FAIL_TERMINAL; //$NON-NLS-1$
		} else if (t instanceof com.ibm.etools.mft.conversion.esb.model.mfc.InputTerminal) {
			return t.getName() != null ? t.getName() : IN_TERMINAL;
		} else if (t instanceof com.ibm.etools.mft.conversion.esb.model.mfc.OutputTerminal) {
			return t.getName() != null ? t.getName() : OUT_TERMINAL;
		}
		return t.getName();
	}

	public static com.ibm.broker.config.appdev.Terminal findTerminal(Object[] terminals, String terminalName) {
		for (Object o : terminals) {
			if (o instanceof com.ibm.broker.config.appdev.Terminal) {
				if (terminalName.equals(((com.ibm.broker.config.appdev.Terminal) o).getName())) {
					return (com.ibm.broker.config.appdev.Terminal) o;
				}
			}
		}
		return null;
	}

	protected Node createNode(MessageFlow parent, String nodeName, String roleName, Class nodeClass, Nodes nodes) throws Exception {
		return PrimitiveManager.createNode(parent, nodeName, roleName, nodeClass, nodes);
	}

	public void createStickyNote(MessageFlow flow, Node[] nodes, String msg) {
		// StickyNote sn = new StickyNote(msg);
		// Vector<com.ibm.broker.config.appdev.Node> v = new
		// Vector<com.ibm.broker.config.appdev.Node>(Arrays.asList(nodes));
		// sn.setAssociatedNodes(v);
		// flow.addStickyNote(sn);
	}

	public void setConversionContext(ConversionContext conversionContext) {
		this.conversionContext = conversionContext;
	}

	protected void debug(String message) {
		conversionContext.log.addEntry(null, new DebugEntry(message));
	}

	protected void createToDoTask(IFile targetFile, String message, Object o) {
		conversionContext.log.addEntry(targetFile, new TodoEntry(message, o));
	}

	protected void createToDoTask(IFile targetFile, String message) {
		conversionContext.log.addEntry(targetFile, new TodoEntry(message));
	}

	protected void addSourceToTargetResource(IFile sourceFile, IFile file) {
		conversionContext.log.addSourceToTargetResource(sourceFile, file);
	}

	protected MessageFlow createSubflow(ConversionContext context, FlowResourceManager flowManager, String folderName,
			String flowName, String flowType) throws IOException {
		return PrimitiveManager.getOrCreateMessageFlow(context, flowManager, folderName, flowName, flowType);
	}

}
