package com.ibm.etools.mft.conversion.esb.extensionpoint;

import com.ibm.broker.config.appdev.Terminal;

/**
 * @author Zhongming Chen
 *
 */
public interface IWESBArtifactConverter {

	/**
	 * Return the type that the converter is able to convert. E.g. MessageFilter
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Map the input terminal of the primitive to the input terminal of IB node.
	 * 
	 * @param sourceTerminalName
	 * @param targetNode
	 * @return
	 */
	public Terminal getInputTerminal(String sourceTerminalName, Nodes nodes);

	/**
	 * Map the output or failure terminal of the primitive to the output
	 * terminal of IB node.
	 * 
	 * @param sourceTerminalName
	 * @param targetNode
	 * @return
	 */
	public Terminal getOutputTerminal(String sourceTerminalName, Nodes targetNode);

	/**
	 * Return a free form description on what the primitive will be converted
	 * to. E.g., "Route" or "MQHeader, MQOutput and MQGet".
	 * 
	 * @return
	 */
	public String getConvertedTo();

	/**
	 * Set the conversion context.
	 * 
	 * @param context
	 */
	public void setConversionContext(ConversionContext context);

}
