//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.01 at 10:33:48 PM EDT 
//

package com.ibm.etools.mft.conversion.esb.model.mfc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * <p>
 * Java class for operationFlow complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="operationFlow">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ibm.com/xmlns/prod/websphere/2010/MediationFlow}flow">
 *       &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="portType" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="operation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;anyAttribute processContents='lax'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "operationFlow")
@XmlSeeAlso({ ResponseFlow.class, ErrorFlow.class, RequestFlow.class })
public class OperationFlow extends Flow {

	@XmlAttribute(name = "ref")
	protected QName ref;
	@XmlAttribute(name = "portType")
	protected QName portType;
	@XmlAttribute(name = "operation")
	protected String operation;

	/**
	 * Gets the value of the ref property.
	 * 
	 * @return possible object is {@link QName }
	 * 
	 */
	public QName getRef() {
		return ref;
	}

	/**
	 * Sets the value of the ref property.
	 * 
	 * @param value
	 *            allowed object is {@link QName }
	 * 
	 */
	public void setRef(QName value) {
		this.ref = value;
	}

	/**
	 * Gets the value of the portType property.
	 * 
	 * @return possible object is {@link QName }
	 * 
	 */
	public QName getPortType() {
		return portType;
	}

	/**
	 * Sets the value of the portType property.
	 * 
	 * @param value
	 *            allowed object is {@link QName }
	 * 
	 */
	public void setPortType(QName value) {
		this.portType = value;
	}

	/**
	 * Gets the value of the operation property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * Sets the value of the operation property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOperation(String value) {
		this.operation = value;
	}

}