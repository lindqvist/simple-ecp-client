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
package jettyClient.simpleClient;

import java.io.Console;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import jettyClient.EnvelopeHandling.EnvelopeParts;
import jettyClient.paosClient.ExchangeContent;
import jettyClient.paosClient.PaosClient;
import jettyClient.parser.ExtractField;
import jettyClient.parser.MessageParser;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.security.Realm;
import org.eclipse.jetty.client.security.RealmResolver;
import org.eclipse.jetty.client.security.SimpleRealmResolver;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;

// Contains Exchanges between SP <--> ECP <--> IdP

public class Connections {

	/**
	 * Access some resource at a SP. Should return the resource or deny access.
	 * 
	 * @param spHost
	 * @param spPort
	 * @param spUri
	 * @throws Exception
	 */
	public void accessResource(URL spURL, URL idpURL) {

		URL spAssertionConssumerURL = null;

		PaosClient paosClient = null;

		ExchangeContent idpContent = null;
		ExchangeContent spContent = null;

		Envelope spEnvelope = null;
		Envelope idpEnvelope = null;

		String principal = "";
		String credentials = "";

		HttpClient httpClient = null;
		String spAssertionConsumer = "", idpAssertionConsumer = "";

		EnvelopeParts idpResponseParts = null;

		Console console = null;

		// Create & start client
		httpClient = getClient();

		try {
			httpClient.start();
		} catch (Exception e) {
			System.out.println("Could not start client.");
		}
		System.out.println("Client started");

		// Create a Paos HttpClient.
		paosClient = new PaosClient(httpClient);

		// Create a new, empty SP ExchangeContent.
		spContent = new ExchangeContent(null, null);

		// Send a PAOS GET request to the given SP endpoint.
		spContent = paosClient.sendHttpGETRequest(spURL, spContent);

		// Check if we received an AuthnRequest as a response.
		// validate(spContent.getEnvelope.getbody.getUnknownXMLObjects);

		// Get the AssertionConsumerURL from the envelope.
		spAssertionConsumer = ExtractField
				.extractAssertionConsumerURL(spContent.getResponseParts()
						.getHeader());

		// Set envelope as IdP exchange content.
		idpEnvelope = MessageParser.createIdpEnvelope(spContent
				.getResponseParts());

		console = System.console();

		// ------- LOGIN -----------
		// Ask the user for login information. Does not work in
		// an IDE.
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429
		// if (console != null) {
		// principal = console
		// .readLine("Please enter username: ");
		// credentials = new String(
		// console.readPassword("Please enter password: "));
		// }
		// -------------------

		// ECLIPSE TEST
		principal = "INSERT USERNAME";
		credentials = "INSERT PASSWORD";

		// Set the login credentials at IdP exchangecontent.
		idpContent = new ExchangeContent(idpEnvelope, createRealmResolver(
				principal, credentials));

		// Extract idplist from authnrequest and check if the SP supports
		// the one that was chosen. If not, complain.
		// idpURL = determineIdP(spContent.getResponseParts().getHeader());

		// Send everything to the IdP.
		idpContent = paosClient.send(idpURL, idpContent);

		// If this does not exist, something went wrong @
		// PaosClient.
		idpResponseParts = idpContent.getResponseParts();

		// Check the IdP response
		if (idpResponseParts != null) {

			// If the response did not contain a SOAP Fault
			if (containsSoapFault(idpResponseParts.getBody()) == false) {

				idpAssertionConsumer = ExtractField
						.extractAssertionConsumerURL(idpResponseParts
								.getHeader());

				// Verify assertionConsumerURL
				if (consumerUrlsMatch(idpAssertionConsumer, spAssertionConsumer)) {

					// Create a URL out of the string
					spAssertionConssumerURL = getURL(spAssertionConsumer); // REAL
					// spAssertionConssumerURL = spURL; // TEST!

					// Create the envelope that is sent to the IdP.
					spEnvelope = MessageParser
							.createSpResponseEnvelope(idpResponseParts);

					// Add the envelope as content.
					spContent.setRequestEnvelope(spEnvelope);

					// Send the assertion.
					paosClient.send(spAssertionConssumerURL, spContent);

				} else {
					spEnvelope = MessageParser
							.createSOAPFault("ConsumerURLs did not match.");
					System.out.println("ConsumerURL did not match.");
				}
			} else {
				System.out.println("Received a SOAP fault from IdP.");
			}
		} else {
			System.out.println("No response received.");
		}
	}

	// /**
	// * Read the list of supported IDPs that the SP sent
	// * and determine if the chosen IdP is supported.
	// * @param header
	// * @return
	// */
	// private URL determineIdP(Header header) {
	//
	// // Get IDPList from header
	// // Check if chosen Id/URL is there
	// // complain if not.
	// return null;
	// }

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

	/**
	 * Verify that both ConsumerURLs match. The serviceproviders URL MAY be
	 * relative & IdP URL MAY be absolute.
	 * 
	 * @param idpAssertionConsumerURL
	 * @param spAssertionConsumerURL
	 * @return
	 */
	private boolean consumerUrlsMatch(String idpAssertionConsumerURL,
			String spAssertionConsumerURL) {

		if (idpAssertionConsumerURL.equals(spAssertionConsumerURL)
				|| idpAssertionConsumerURL.endsWith(spAssertionConsumerURL))
			return true;
		return false;
	}

	/**
	 * Returns a new RealmResolver with the given credentials set. These will be
	 * added as an HTTP header field in the request that is sent to the Id. The
	 * returned RealmResolver will be set on the httpClient.
	 * 
	 * @param principal
	 *            username
	 * @param credentials
	 *            password
	 * @return a RealmResolver
	 */
	public RealmResolver createRealmResolver(final String principal,
			final String credentials) {

		return new SimpleRealmResolver(new Realm() {

			String id = "IdP Password Authentication"; // Info from IdP

			public String getPrincipal() {
				return principal;
			}

			public String getId() {
				return id;
			}

			public String getCredentials() {
				return credentials;
			}
		});
	}

	/**
	 * Create and configure a Jetty Httpclient.
	 * 
	 * @return A HttpClient
	 */
	private HttpClient getClient() {

		HttpClient client = new HttpClient();
		client.setIdleTimeout(1000);
		client.setTimeout(100000); // STATUS_EXPIRED
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);

		return client;
	}

	/**
	 * Attempts to create an URL from the given parameter.
	 * 
	 * @param string
	 * @return
	 */
	private URL getURL(String string) {
		URL url = null;

		try {
			url = new URL(string);
			if (url.getPort() == -1) {
				System.out.println("Missing port number in URL.");
				throw new MalformedURLException();
			}
		} catch (MalformedURLException e) {
			url = null;
			System.out.println("Malformed endpoint URL: " + string);
		}
		return url;
	}
}
