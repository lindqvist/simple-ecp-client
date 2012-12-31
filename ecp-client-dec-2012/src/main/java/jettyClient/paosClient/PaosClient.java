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

package jettyClient.paosClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import jettyClient.parser.MessageParser; // parse text to Envelope
import jettyClient.parser.ValidateXML; // validates the SOAP envelope.
import jettyClient.simpleClient.ClientConfiguration;
import jettyClient.simpleClient.ClientExchange; // extends HttpContentExchange

import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Fault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaosClient {

	private final HttpClient httpClient;

	// Get the client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);

	public PaosClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Send a GET request to the specified URL. Used when the client requests a
	 * resource from a SP without being authenticated. The expected response is
	 * a SOAP Envelope that contains an AuthnRequest.
	 * 
	 * @param endpoint
	 * @return
	 */
	public ExchangeContent sendHttpGETRequest(URL endpoint,
			ExchangeContent content) {

		// Set up a connection to the SP.
		ClientExchange serviceProviderExchange = getPAOSExchange(endpoint);

		System.out.println("Getting resource: " + endpoint);

		// Send GET request
		serviceProviderExchange = exchangeContent(httpClient,
				serviceProviderExchange);

		if (serviceProviderExchange != null) {
			// Store response (SOAP Envelope with an AuthnRequest)
			content = storeResponse(serviceProviderExchange, content);
		}

		return content;
	}

	/**
	 * Send a SOAP envelope to an endpoint. The response will be either a SOAP
	 * envelope or any other kind of response.
	 * 
	 * Whatever the response contains, it will be stored in the ExchangeContent
	 * object.
	 * 
	 * Any single cookie that is sent along with a Response will also be stored
	 * in the ExchangeContent object.
	 * 
	 * The content object is returned, possibly unchanged, but only if no
	 * response at all was received.
	 * 
	 * @param endpoint
	 * @param content
	 * @return Returns an ExchangeContent object with a response.
	 */
	public ExchangeContent send(URL endpoint, ExchangeContent content) {

		// Create a new POST exchange.
		ClientExchange clientExchange = getPOSTExchange(endpoint);

		// Write the Envelope to a ByteArrayOutputStream
		ByteArrayOutputStream stream = MessageParser.envelopeToStream(content
				.getRequestEnvelope());

		// Add content to the Exchange
		clientExchange.setRequestContent(new ByteArrayBuffer(stream
				.toByteArray()));

		logger.info("\nSent to " + clientExchange.getAddress().getHost()
				+ clientExchange.getRequestURI() + "\n" + stream.toString());

		// Add the cookie to the Exchange (if there is one)
		// VERY SP SPECIFIC CODE. CAN FAIL.
		if (!content.getCookieField().equals(""))
			clientExchange.setRequestHeader(HttpHeaders.COOKIE,
					content.getCookieField() + ";");

		// Set realmResolver if there is one to set.
		if (content.getRealmResolver() != null)
			httpClient.setRealmResolver(content.getRealmResolver());

		// Send exchange
		clientExchange = exchangeContent(httpClient, clientExchange);

		// exchangeContent() will return null when something fails.
		if (clientExchange != null) {

			// Inspect and store the response message. Could be *anything*.
			content = storeResponse(clientExchange, content);

			// If the response was not an envelope.
			if (content.getResponseParts() != null) {
				// Check if the IdP sent a SOAP fault message
				if (containsSoapFault(content.getResponseParts().getBody())) {
					logger.info("Received a SOAP fault from the IdP.");
				}
			}
			// Check if anything could be stored. React.

		} else {
			logger.info("Could not send envelope.");
		}
		return content;
	}

	/**
	 * Validate responseContent to see if it contained a SOAP Envelope. If it
	 * did, store it in a ExchangeContent object. Content can be returned
	 * unchanged.
	 * 
	 * responseContent should be filled with a response from the IdP, or it will
	 * be returned empty.
	 * 
	 * @param clientExchange
	 * @param content
	 */
	private ExchangeContent storeResponse(ClientExchange clientExchange,
			ExchangeContent content) {

		// String response = "";
		String cookieField = "";
		byte[] responseBytes = null; //

		// response = clientExchange.getResponseContent();
		// byte[] = any other response object
		responseBytes = clientExchange.getResponseContentBytes();

		// Check response status (200 = OK)
		if (clientExchange.getResponseStatus() == 200) {
			// If the response contains an envelope...

			logger.info("\nReceived from "
					+ clientExchange.getAddress().getHost() + ":\n"
					+ new String(responseBytes));
			
			
			if (isEnvelope(responseBytes)) {
				// Extract + save an Envelope as EnvelopeParts from the
				// response.
				content.setResponseParts(MessageParser
						.parseMessage(responseBytes));

			} else {
				logger.debug("No SOAP Envelope received as response.");
				
				// Make sure the responseparts are empty.
				content.setResponseParts(null);
				
				// This is where the resource will be stored. Or anything
				// else that is received, that is not a SOAP Envelope.
				content.setOtherResponse(responseBytes);
			}

			// Check if the response contains any headers.
			if (clientExchange.getResponseFields() != null) {

				// Save all the response headers (in order to get the
				// Authorization header from the SP when logged in.)
				content.setHeaders(clientExchange.getResponseFields());

				// Get the cookie field.
				cookieField = clientExchange.getResponseFields()
						.getStringField("Set-Cookie");

				// Save the session cookie (from the SP).
				if (cookieField != null) {
					if (cookieField.isEmpty() == false)
						content.setCookieField(cookieField);
				}
			}
		} else {
			String error = "HTTP Error, status: "
					+ clientExchange.getResponseStatus() + " from "
					+ clientExchange.getAddress().getHost() + ".";

			System.out.println(error);
			logger.debug(error + "\n" + new String(responseBytes));
			content.setOtherResponse(responseBytes);
		}
		return content;
	}

	/**
	 * Generate a new POST exchange.
	 * 
	 * @param host
	 * @param port
	 * @param uri
	 * @return
	 */
	private ClientExchange getPOSTExchange(URL url) {

		ClientExchange exchange = null;

		exchange = new ClientExchange();
		exchange.setMethod(HttpMethods.POST);
		exchange.setScheme(HttpSchemes.HTTPS_BUFFER); // Enable HTTPS

		exchange.setAddress(new Address(url.getHost(), url.getPort()));
		exchange.setRequestURI(url.getFile());

		exchange.setRequestHeader(HttpHeaders.ACCEPT, MimeTypes.TEXT_HTML);

		return exchange;
	}

	/**
	 * Generate a GET request for a Service Provider. Ask for a resource and
	 * receive an AuthnRequest in an Envelope.
	 * 
	 * @param host
	 *            Host address
	 * @param port
	 *            Port number
	 * @param uri
	 *            Resource identifier
	 * @return
	 */
	private ClientExchange getPAOSExchange(URL url) {

		ClientExchange exchange = null;

		String PAOS_header = "PAOS";
		String acceptsPAOS = "application/vnd.paos+xml";

		// Should use constant field values from opensaml library instead?
		String supportsService = "\"urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp\"";
		String supportsPaosV_1_1 = "ver=\"urn:liberty:paos:2003-08\"";

		// Configure HttpExchange
		Boolean cache_headers = true;
		exchange = new ClientExchange(cache_headers);
		exchange.setMethod(HttpMethods.GET);
		exchange.setScheme(HttpSchemes.HTTPS_BUFFER);

		// Set address & uri
		exchange.setAddress(new Address(url.getHost(), url.getPort()));
		exchange.setRequestURI(url.getFile());

		// Set headers
		exchange.setRequestHeader(HttpHeaders.ACCEPT, MimeTypes.TEXT_HTML
				+ "; " + acceptsPAOS);
		exchange.setRequestHeader(PAOS_header, supportsPaosV_1_1 + "; "
				+ supportsService);

		return exchange;
	}

	/**
	 * Sends a contentExchange to a receiver, returns the response, whatever it
	 * may be, as a text string (if STATUS_COMPLETED), or else null.
	 * 
	 * Universal code, can be used with any httpClient.
	 * 
	 * @param httpClient
	 * @param clientExchange
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UnsupportedEncodingException
	 */
	private ClientExchange exchangeContent(HttpClient httpClient,
			ClientExchange clientExchange) {

		// Send clientExchange
		try {
			httpClient.send(clientExchange);
		} catch (IOException e) {
			logger.debug("Could not send message to "
					+ clientExchange.getAddress().getHost());
		}

		// Wait until something is received.
		int state = 0;

		try {
			state = clientExchange.waitForDone();
		} catch (InterruptedException e) {
			logger.error("ContentExchange was interrupted.");
		}

		if (state == ClientExchange.STATUS_COMPLETED) {
			return clientExchange;
		} else if (state == ClientExchange.STATUS_EXCEPTED)
			logger.error("Exception in ContextExchange");
		else if (state == ClientExchange.STATUS_EXPIRED) // client.setTimeout(timeToExpire);
			logger.error("ContentExchange expired");
		return null;
	}

	/**
	 * Check if the message contains a SOAP envelope.
	 * 
	 * @param responseMessage
	 * @return
	 */
	private boolean isEnvelope(byte[] responseMessage) {

		if (responseMessage != null)
			if (ValidateXML.isValidEnvelope(new ByteArrayInputStream(
					responseMessage)))
				return true;
		return false;
	}

	/**
	 * Determine if the message body contains a SOAP fault message.
	 * 
	 * Return true if a SOAP fault is present.
	 * 
	 * @param body
	 * @return
	 */
	public boolean containsSoapFault(Body body) {

		// Body can't be null, since if the idp
		// sent an envelope it has to contain a body.

		List<XMLObject> list = body.getUnknownXMLObjects();

		for (XMLObject xmlObject : list) {
			if (xmlObject.getElementQName() != null)
				if (xmlObject.getElementQName().equals(
						Fault.DEFAULT_ELEMENT_NAME))
					return true;
		}
		return false;
	}

}
