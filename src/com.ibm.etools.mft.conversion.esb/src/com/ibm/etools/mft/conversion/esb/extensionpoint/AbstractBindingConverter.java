/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import org.eclipse.core.resources.IFile;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;

/**
 * @author Zhongming Chen
 * 
 */
abstract public class AbstractBindingConverter extends AbstractWESBArtifactConverter implements IBindingConverter {

	/**
	 * 
	 */
	public AbstractBindingConverter() {
	}

	/**
	 * Create a Nodes container to host all the IIB nodes that implement the
	 * logic for the source WESB Export/Import binding.
	 * 
	 * @param converterContext
	 * @return
	 */
	protected Nodes createNodes(ConverterContext converterContext) {
		Nodes nc = new Nodes();
		converterContext.moduleConverter.getBindingToNodes().put(converterContext.sourceBinding, nc);
		return nc;
	}

	@Override
	public String getDisplayName() {
		return getType();
	}

	protected String getProposedIIBNodeNameFromBinding(ConverterContext converterContext) {
		return BindingManager.getNodeName(converterContext.sourceBinding, converterContext.moduleConverter.getBindingToNodes());
	}

	protected IFile getMainFlowFile(ConverterContext converterContext) {
		return converterContext.flowManager.getFlowResource(ConversionUtils.getFullyQualifiedFlowName(null,
				converterContext.moduleConverter.getTargetProject().getName()));
	}

}
