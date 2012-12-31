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

import java.net.URL;

import jettyClient.EnvelopeHandling.EnvelopeCreator;
import jettyClient.paosClient.PaosClient;
import jettyClient.parser.ParseHelper;

import org.eclipse.jetty.client.HttpClient;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

	// Get the logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);

	/**
	 * Access a resource at the given url.
	 * 
	 * @param spURL
	 */
	
	public void accessResource(ClientOptions options, IDPEntry idpEntry) {

		HttpClient httpClient = getClient();

		try {
			httpClient.start();
		} catch (Exception e) {
			logger.debug("Could not start client.");
		}
		logger.debug("Client started");

		// If there is an IdP
		if (idpEntry != null) {

			Connections connections = new Connections();

			Body assertionResponse = connections.accessResource(options,
					idpEntry, httpClient);

			if (assertionResponse != null) {

				// -------- Changing the following lines -------
				// ------ changes the purpose of the client ----

				// Get the resource and consume it somehow, or be denied access.
				// sendAssertion(options, httpClient, assertionResponse);

				// ---------------- CHANGES END -----------------
			}
		}
	}

	/**
	 * Send the Response with the assertion to the endpoint.
	 * @param options
	 * @param httpClient
	 * @param assertionResponse
	 */
	private void sendAssertion(ClientOptions options, HttpClient httpClient,
			Body assertionResponse) {
		
		// If the assertion is sent elsewhere, a class can be added and the 
		// assertion can be sent to that class from here. For example.
		
//		// Create the envelope that will be sent to the SP.
//		Envelope spEnvelope = EnvelopeCreator.createSpResponseEnvelope(assertionResponse);
//		String envelopeString = ParseHelper.anythingToXMLString(spEnvelope);
//		
//		System.out.println("Sending envelope to SP endpoint: " +options.getSpEndpoint());
//		System.out.println(envelopeString);
//		logger.info("Sent to SP: \n" +envelopeString);
//		
//		URL spURL = null;
//		
//		if (options.getSpEndpoint() != null)
//		{spURL = options.getSpEndpoint();} else {
//			logger.debug("ERROR! NO SP ENDPOINT SPECIFIED!");				
//		}
		
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

}
