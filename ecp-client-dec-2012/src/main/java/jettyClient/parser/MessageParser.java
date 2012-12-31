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
import java.io.ByteArrayOutputStream;
import jettyClient.EnvelopeHandling.EnvelopeParts;
import jettyClient.simpleClient.ClientConfiguration;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;

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
	public static EnvelopeParts parseMessage(byte[] message) {

		if (message != null) {

			String schemaFilePath = ClientConfiguration.soapEnvelopeSchemaLocation;
			EnvelopeParts envelopeParts = null;
			Element element = null;

			// Attempt to extract the element specified by the schema from a
			// message.
			element = ParseHelper.extractElement(new ByteArrayInputStream(
					message), schemaFilePath);

			// Store the Envelope in parts. If the parsing went well.
			if (element != null) {
				envelopeParts = storeEnvelopeParts(element);
			}
			return envelopeParts;
		}
		return null;
	}

	/**
	 * Turns a SOAP message XML into a SOAP Envelope.
	 * 
	 * @param message
	 * @param schemaFilePath
	 * @return A SOAP Envelope in parts
	 */
	public static Envelope parseMessageToEnvelope(byte[] message) {

		Envelope envelope = null;

		if (message != null) {

			String schemaFilePath = ClientConfiguration.soapEnvelopeSchemaLocation;
			EnvelopeParts envelopeParts = null;
			Element element = null;

			// Attempt to extract the element specified by the schema from a
			// message.
			element = ParseHelper.extractElement(new ByteArrayInputStream(
					message), schemaFilePath);

			envelope = (Envelope) ParseHelper.unmarshall(element);
		}
		return envelope;
	}

	/**
	 * Store element (=Envelope) parts in an EnvelopeParts object.
	 * 
	 * TODO: if the envelope does not have a header...
	 * 
	 * @param element
	 *            The Envelope element.
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
	 * Create a ByteArrayOutputStream and write the envelope given as parameter
	 * to this stream.
	 * 
	 * @param envelope
	 *            A SOAP Envelope
	 * @return A ByteArrayOutputStream to which an Envelope has been written.
	 */
	public static ByteArrayOutputStream envelopeToStream(Envelope envelope) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		SerializeSupport.writeNode(ParseHelper.marshall(envelope), stream);

		return stream;
	}

	/**
	 * Create a ByteArrayOutputStream and write the envelope given as parameter
	 * to this stream.
	 * 
	 * @param envelope
	 *            A SOAP Envelope
	 * @return A ByteArrayOutputStream to which an Envelope has been written.
	 */
	public static ByteArrayOutputStream envelopeToStreamV2(Envelope envelope) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		SerializeSupport.writeNode(ParseHelper.marshall(envelope), stream);

		Marshaller marshaller = XMLObjectProviderRegistrySupport
				.getMarshallerFactory().getMarshallers()
				.get(Envelope.DEFAULT_ELEMENT_NAME);
		Element element = null;
		try {
			element = marshaller.marshall(envelope);
		} catch (MarshallingException e) {
			System.out.println("Could not marshall the envelope!");
		}
		Document document = element.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document
				.getImplementation();
		LSOutput lsOutput = domImplLS.createLSOutput();
		lsOutput.setByteStream(stream);

		return stream;
	}

}
