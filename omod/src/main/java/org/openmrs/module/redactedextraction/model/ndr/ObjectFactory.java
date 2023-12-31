//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.09.01 at 12:51:14 PM WAT 
//

package org.openmrs.module.redactedextraction.model.ndr;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the com.openmrs package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation
 * for XML content. The Java representation of XML content can consist of schema derived interfaces
 * and classes representing the binding of schema type definitions, element declarations and model
 * groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {
	
	/**
	 * Create a new ObjectFactory that can be used to create new instances of schema derived classes
	 * for package: com.openmrs
	 */
	public ObjectFactory() {
	}
	
	/**
	 * Create an instance of {@link Container }
	 */
	public Container createContainer() {
		return new Container();
	}
	
	/**
	 * Create an instance of {@link MessageHeaderType }
	 */
	public MessageHeaderType createMessageHeaderType() {
		return new MessageHeaderType();
	}
	
	/**
	 * Create an instance of {@link PatientDemographicsType }
	 */
	public PatientDemographicsType createPatientDemographicsType() {
		return new PatientDemographicsType();
	}
	
	/**
	 * Create an instance of {@link RedactedVisitType }
	 */
	public RedactedVisitType createRedactedVisitType() {
		return new RedactedVisitType();
	}
	
	/**
	 * Create an instance of {@link MessageSendingOrganisationType }
	 */
	public MessageSendingOrganisationType createMessageSendingOrganisationType() {
		return new MessageSendingOrganisationType();
	}
	
}
