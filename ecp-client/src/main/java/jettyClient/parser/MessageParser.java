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

package jettyClient.parser;

import java.io.ByteArrayInputStream;

import jettyClient.EnvelopeHandling.EnvelopeParts;
import jettyClient.EnvelopeHandling.HeaderCreator;

import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultCode;
import org.opensaml.soap.soap11.Header;
import org.opensaml.soap.util.SOAPHelper;
import org.w3c.dom.Element;

/**
 * Parse XML into a SOAP Message and vice versa.
 */

public class MessageParser {

	/**
	 * Turns a SOAP message XML into a SOAP Envelope and splits it into a
	 * Header, a Body and an empty Envelope. These items are stored in the
	 * returned EnvelopeParts object.
	 * 
	 * @param message
	 * @param schemaFilePath
	 * @return A SOAP Envelope in parts
	 */
	public static EnvelopeParts parseMessage(String message) {

		if (message != null) {

			String schemaFilePath = "resources/schema/soap-envelope.xsd"; // :(
			EnvelopeParts envelopeParts = null;
			Element element = null;

			// Attempt to extract the element specified by the schema from a
			// message.
			element = ParseHelper.extractElement(new ByteArrayInputStream(
					message.getBytes()), schemaFilePath);

			// Store the Envelope in parts. If the parsing went well.
			if (element != null) {
				envelopeParts = storeEnvelopeParts(element);
			}
			return envelopeParts;
		}
		return null;
	}

	/**
	 * Store element (=Envelope) parts in an EnvelopeParts object.
	 * 
	 * TODO: if the envelope does not have a header...
	 * 
	 * @param element
	 * @return
	 */
	private static EnvelopeParts storeEnvelopeParts(Element element) {

		Header header = null;
		Envelope emptyEnvelope = null;
		Body body = null;

		// ------------------ Header ----------------- //

		// Get the first child of the element.
		Element childElement = (Element) element.getFirstChild();

		// Check if the first child of the element is a header (it should be).
		if (ParseHelper.getDefaultElementName(childElement).equals(
				Header.DEFAULT_ELEMENT_NAME)) {
			// Store and remove Header.
			header = (Header) ParseHelper.unmarshall(childElement);
			element.removeChild(childElement);
		} else {
			System.out.println("Header was missing.");
		}

		// ------------------- Body ------------------ //

		// Get the new child element (since Header was removed)
		childElement = (Element) element.getFirstChild();

		// Check if the second child of the element is a body (it should be).
		if (ParseHelper.getDefaultElementName(childElement).equals(
				Body.DEFAULT_ELEMENT_NAME)) {
			// Store and remove Body.
			body = (Body) ParseHelper.unmarshall(childElement);
			element.removeChild(childElement);
		} else {
			System.out.println("Body was missing.");
		}

		// ----------------- Envelope ---------------- //

		// Store the (filled) envelope.
		emptyEnvelope = (Envelope) ParseHelper.unmarshall(element);

		return new EnvelopeParts(header, body, emptyEnvelope);
	}

	/**
	 * Return an IdP Envelope (Header = null, Body = AuthnRequest from SP).
	 * 
	 * Creates a new Envelope to be sent to the IdP and adds the same
	 * AuthnRequest element that is stored in the body of the SP envelope.
	 * 
	 * @param envelopeParts
	 * @return
	 */
	public static Envelope createIdpEnvelope(EnvelopeParts envelopeParts) {

		Element envelopeElement = ParseHelper.marshall(buildEnvelope());
		Element bodyElement = ParseHelper.marshall(envelopeParts.getBody());

		ElementSupport.appendChildElement(envelopeElement, bodyElement);
		Envelope envelope = (Envelope) ParseHelper.unmarshall(envelopeElement);

		return envelope;
	}

	/**
	 * Return an SP envelope (Header = PAOS Response, Body = Assertion).
	 * 
	 * @param envelopeParts
	 * @return
	 */
	public static Envelope createSpResponseEnvelope(EnvelopeParts envelopeParts) {

		Element envelopeElement = ParseHelper.marshall(buildEnvelope());
		Element bodyElement = ParseHelper.marshall(envelopeParts.getBody());

		ElementSupport.appendChildElement(envelopeElement, bodyElement);

		Envelope envelope = (Envelope) ParseHelper.unmarshall(envelopeElement);
		envelope.setHeader(HeaderCreator.buildEcpToSpHeader()); // set PAOS resp
																// header
		return envelope;
	}

	/**
	 * Create a Envelope with a SOAP Fault message. FaultCode.CLIENT =
	 * incorrect/malformed information received.
	 * 
	 * @return
	 */
	public static Envelope createSOAPFault(String faultString) {

		Envelope envelope = buildEnvelope();

		Fault fault = SOAPHelper.buildSOAP11Fault(FaultCode.CLIENT,
				faultString, null, null, null);

		Body body = (Body) ParseHelper.buildObject(Body.DEFAULT_ELEMENT_NAME);
		body.getUnknownXMLObjects().add(fault);

		envelope.setBody(body);

		return envelope;
	}

	/**
	 * Build an empty envelope.
	 * 
	 * @return
	 */
	private static Envelope buildEnvelope() {
		return (Envelope) ParseHelper
				.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
	}

}
