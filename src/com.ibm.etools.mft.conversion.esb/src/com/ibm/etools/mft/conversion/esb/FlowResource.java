package com.ibm.etools.mft.conversion.esb;

import org.eclipse.core.resources.IFile;

import com.ibm.broker.config.appdev.MessageFlow;

/**
 * @author Zhongming Chen
 * 
 */
public class FlowResource {
	public MessageFlow flow;
	public IFile file;
	public boolean requireAutoLayout = false;

	public FlowResource(MessageFlow flow, IFile file) {
		this.file = file;
		this.flow = flow;
		requireAutoLayout = true;
	}
}