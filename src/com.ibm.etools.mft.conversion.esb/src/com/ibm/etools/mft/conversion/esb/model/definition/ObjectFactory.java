//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.01 at 10:28:14 PM EDT 
//

package com.ibm.etools.mft.conversion.esb.model.definition;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * com.ibm.etools.mft.conversion.esb.model.definition package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _WesbConversion_QNAME = new QName(
			"http://www.ibm.com/WESBArtifactsConversionToWMB", "wesbConversion"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package:
	 * com.ibm.etools.mft.conversion.esb.model.definition
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link WesbConversionType }
	 * 
	 */
	public WesbConversionType createWesbConversionType() {
		return new WesbConversionType();
	}

	/**
	 * Create an instance of {@link ConversionType }
	 * 
	 */
	public ConversionType createConversionType() {
		return new ConversionType();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link WesbConversionType }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/WESBArtifactsConversionToWMB", name = "wesbConversion")
	public JAXBElement<WesbConversionType> createWesbConversion(WesbConversionType value) {
		return new JAXBElement<WesbConversionType>(_WesbConversion_QNAME, WesbConversionType.class, null, value);
	}

}