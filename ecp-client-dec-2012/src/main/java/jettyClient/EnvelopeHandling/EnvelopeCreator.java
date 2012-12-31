/* ***************************************************************************
 * Copyright 2012 Carolina Lindqvist
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ***************************************************************************/

package jettyClient.EnvelopeHandling;

import java.math.BigInteger;
import java.security.SecureRandom;

import jettyClient.parser.ParseHelper;
import jettyClient.simpleClient.ClientConfiguration;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultCode;
import org.opensaml.soap.util.SOAPHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


public class EnvelopeCreator {
	
	// Get the client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);
	
	/**
	 * Return an IdP Envelope (Header = null, Body = AuthnRequest from SP).
	 * 
	 * Creates a new Envelope to be sent to the IdP and adds the same
	 * AuthnRequest element that is stored in the body of the SP envelope.
	 * 
	 * @param envelopeParts
	 * @return An envelope with an AuthnRequest addressed for the IdP.
	 */
	public static Envelope createIdpEnvelope(EnvelopeParts envelopeParts) {

		Element envelopeElement = ParseHelper.marshall(buildEnvelope());
		Element bodyElement = ParseHelper.marshall(envelopeParts.getBody());

		ElementSupport.appendChildElement(envelopeElement, bodyElement);
		Envelope envelope = (Envelope) ParseHelper.unmarshall(envelopeElement);

		return envelope;
	}

	/**
	 * Return an envelope that will be returned to the SP (Header = PAOS
	 * Response, Body = Response that contains an assertion).
	 * 
	 * @param envelopeParts
	 * @return An envelope addressed for the SP.
	 */
	public static Envelope createSpResponseEnvelope(Body body) {

		Element envelopeElement = ParseHelper.marshall(buildEnvelope());
		Element bodyElement = ParseHelper.marshall(body);

		ElementSupport.appendChildElement(envelopeElement, bodyElement);

		Envelope envelope = (Envelope) ParseHelper.unmarshall(envelopeElement);
		envelope.setHeader(HeaderCreator.buildEcpToSpHeader()); // set PAOS header
		return envelope;
	}


	/**
	 * Create a SOAP Fault element and add it to a Body element.
	 * 
	 * @param faultString
	 * @return A SOAP Body that contains a SOAP fault.
	 */
	public static Body createSoapFaultBody(String faultString) {
		Fault fault = SOAPHelper.buildSOAP11Fault(FaultCode.CLIENT,
				faultString, null, null, null);

		Body body = (Body) ParseHelper.buildObject(Body.DEFAULT_ELEMENT_NAME);
		body.getUnknownXMLObjects().add(fault);

		return body;
	}

	/**
	 * Build an empty envelope.
	 * 
	 * @return An empty SOAP envelope.
	 */
	private static Envelope buildEnvelope() {
		return (Envelope) ParseHelper
				.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
	}

	/**
	 * Turn responseParts into an envelope.
	 * 
	 * @param responseParts
	 * @return An envelope constructed out of the envelope parts.
	 */

	public static Envelope partsToEnvelope(EnvelopeParts envelopeParts) {

		Element envelopeElement = ParseHelper.marshall(envelopeParts
				.getEnvelope());
		Element bodyElement = ParseHelper.marshall(envelopeParts.getBody());
		Element headerElement = ParseHelper.marshall(envelopeParts.getHeader());

		ElementSupport.appendChildElement(envelopeElement, headerElement);
		ElementSupport.appendChildElement(envelopeElement, bodyElement);

		Envelope envelope = (Envelope) ParseHelper.unmarshall(envelopeElement);

		return envelope;
	}

	/**
	 * Generates a random String value.
	 * 
	 * @return A random text string.
	 */
	private static String randomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

}
