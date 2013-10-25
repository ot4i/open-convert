package com.ibm.etools.mft.conversion.esb.extensionpoint;

import org.eclipse.core.resources.IFile;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.etools.mft.conversion.esb.FlowResourceManager;
import com.ibm.etools.mft.conversion.esb.extension.resource.SCAModuleConverterHelper;
import com.ibm.wsspi.sca.scdl.Binding;
import com.ibm.wsspi.sca.scdl.wsdl.WSDLPortType;

/**
 * @author Zhongming Chen
 *
 */
public interface IBindingConverter extends IWESBArtifactConverter {

	public class ConverterContext {
		/**
		 * The SCA module converter
		 */
		public SCAModuleConverterHelper moduleConverter;
		/**
		 * The source Export / Import binding that is being converted
		 */
		public Binding sourceBinding;
		/**
		 * The message flow that is being generated to implement the
		 * functionality of source mediation module if the source binding is
		 * Export binding. The message flow that is being generated to implement
		 * mediation flow if the source binding is Import binding.
		 */
		public MessageFlow targetFlow;
		/**
		 * The port type that is used by the source Export / Import binding
		 */
		public WSDLPortType portType;
		/**
		 * The interface operation that is being used for the source Import
		 * binding. It is only applicable to Import binding.
		 */
		public String operationName;
		/**
		 * Flow resource manager which contains a list of message flows that are
		 * being generated during conversion.
		 */
		public FlowResourceManager flowManager;
		/**
		 * The Eclipse resource file corresponding to the targetFlow
		 */
		public IFile flowFile;
	}

	/**
	 * Convert the binding specified in converterContext.sourceBinding to one or
	 * many message flow nodes.
	 * 
	 * @param converterContext
	 * @return
	 * @throws Exception
	 */
	public Nodes convert(ConverterContext converterContext) throws Exception;

	/**
	 * Return a short descriptive name for an Export/Import binding class. E.g.
	 * "WebService Export".
	 * 
	 * @return
	 */
	public String getDisplayName();
}
