/*************************************************************************
 *  <copyright 
 *  notice="oco-source" 
 *  pids="5724-E11,5724-E26" 
 *  years="2010,2013" 
 *  crc="3749818052" > 
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
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.HashMap;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper;
import com.ibm.etools.mft.conversion.esb.model.mfc.Flow;
import com.ibm.etools.mft.conversion.esb.model.mfc.Node;
import com.ibm.wsspi.sca.scdl.Component;
import com.ibm.wsspi.sca.scdl.Part;

/**
 * @author Zhongming Chen
 *
 */
public interface IPrimitiveConverter extends IWESBArtifactConverter {
	public static final String copyright = "Licensed Material - Property of IBM 5724-E11, 5724-E26 (c)Copyright IBM Corp. 2010, 2013 - All Rights Reserved. US Government Users Restricted Rights - Use,duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public class ConverterContext {
		/**
		 * The SCA module converter
		 */
		public SCAModuleConverterHelper moduleConverter;
		/**
		 * The upstreamed SCA part (could be Export or another Component) of
		 * current mediation flow component in the assembly diagram.
		 */
		public Part upstreamedPart;
		/**
		 * The SCA component that represents the mediation flow that is being
		 * converted.
		 */
		public Component component;
		/**
		 * The interface operation name which is associated with the mediation
		 * flow that is being converted.
		 */
		public String operationName;
		/**
		 * Flow resource manager which contains a list of message flows that are
		 * being generated during conversion.
		 */
		public FlowResourceManager flowManager;
		/**
		 * The message flow that is being generated to implement the
		 * functionality of the source mediation flow.
		 */
		public MessageFlow targetFlow;
		/**
		 * Source mediation flow
		 */
		public Flow sourceFlow;
		/**
		 * The input node in the source mediation flow
		 */
		public Node inputNodeInSourceFlow;
		/**
		 * The source mediation primitive that is being converted
		 */
		public Node sourcePrimitive;
	}

	/**
	 * Convert the primitive specified in converterContext.sourcePrimitive to
	 * one or many message flow nodes.
	 * 
	 * @param converterContext
	 * @return
	 * @throws Exception
	 */
	public Nodes convert(ConverterContext converterContext) throws Exception;

	/**
	 * Convert the wires in source flow to target flow.
	 * 
	 * 
	 * @param targetFlow
	 * @param sourceFlow
	 * @param inputNodeInSourceFlow
	 * @param sourcePrimitive
	 * @param primitiveToNodeCollection
	 * @throws Exception
	 */
	public void convertWire(MessageFlow targetFlow, Flow sourceFlow, Node inputNodeInSourceFlow, Node sourcePrimitive,
			HashMap<Node, Nodes> primitiveToNodeCollection) throws Exception;

}
