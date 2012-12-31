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

import jettyClient.EnvelopeHandling.EnvelopeCreator;
import jettyClient.paosClient.ExchangeContent;
import jettyClient.paosClient.PaosClient;
import jettyClient.parser.ExtractField;
import jettyClient.parser.ParseHelper;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.security.Realm;
import org.eclipse.jetty.client.security.RealmResolver;
import org.eclipse.jetty.client.security.SimpleRealmResolver;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.core.IDPList;
import org.opensaml.saml.saml2.ecp.Request;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connections contains Exchanges between the SP <--> ECP <--> IdP. This version
 * has an accessResources method that returns the Response Envelope from the
 * IdP.
 * 
 * @author carolina
 * 
 */
public class Connections {

	// Client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);

	private boolean verbose = false;

	/**
	 * Access some resource at a SP. Returns an ExchangeContent object that
	 * contains the response that was sent from the IdP.
	 * 
	 * @param spHost
	 * @param spPort
	 * @param spUri
	 * @throws Exception
	 */

	public Body accessResource(ClientOptions options, IDPEntry idpEntry,
			HttpClient httpClient) {

		PaosClient paosClient = null;

		ExchangeContent spContent = null;
		URL assertionConsumerEndpoint = null;

		// Set parameters from options in args.
		setParameters(options);

		// Create a Paos HttpClient.
		paosClient = new PaosClient(httpClient);

		// Get the AuthnRequest from the SP
		spContent = getRequestToSP(options.getSpURL(), paosClient);

		if (spContent.getResponseParts() != null) {
			String spAssertionConsumer = ExtractField
					.extractAssertionConsumerURL(spContent.getResponseParts()
							.getHeader());

			// Check if we received an AuthnRequest as a response.
			// validate(spContent.getEnvelope.getbody.getUnknownXMLObjects);

			// Get the SOAP Envelope Body from the IdP that contains the
			// response or a soap fault.
			Body body = getResponseBody(spContent, idpEntry, paosClient);

			if (body != null) {
				if (verbose) {
					System.out.println("Received from idp: \n"
							+ ParseHelper.anythingToXMLString(body));
				}

				logger.debug("Received from idp: \n"
						+ ParseHelper.anythingToXMLString(body));
			}

			// Build the envelope you want to send.
			Envelope assertionEnvelope = EnvelopeCreator
					.createSpResponseEnvelope(body);

			// Build an empty exchangeContent with the envelope
			ExchangeContent assertionContent = new ExchangeContent(
					assertionEnvelope, null);

			// Turn the assertionConsumer string into an URL
			assertionConsumerEndpoint = getURL(spAssertionConsumer);

			// Add the sp session cookie back
			assertionContent.setCookieField(spContent.getCookieField());

			// Send the exchangeContent.
			assertionContent = paosClient.send(assertionConsumerEndpoint,
					assertionContent);

			String envelopeString = ParseHelper
					.anythingToXMLString(assertionEnvelope);

			System.out.println("Sending envelope to SP endpoint: "
					+ options.getSpEndpoint());
			System.out.println(envelopeString);
			logger.info("Sent to SP: \n" + envelopeString);

			if (assertionContent.getOtherResponse() != null) {
				System.out.println("Response received from SP: \n");
				System.out.println(new String(assertionContent.getOtherResponse()));
			}

			// This return is unnecessary in a normal SP exchange.
			return body;
		}
		logger.debug("The SP did not respond to the GET request.");
		return null; // :(
	}

	// Set the -verbose parameter
	private void setParameters(ClientOptions options) {
		verbose = options.isVerbose();
	}

	/**
	 * Return a SOAP Envelope Body that contains the Response the IdP sent, if
	 * there is one.
	 * 
	 * Returns null if the IdP returned no response at all. Nothing.
	 * 
	 * @return
	 */
	private Body getResponseBody(ExchangeContent spContent, IDPEntry idpEntry,
			PaosClient paosClient) {

		String spAssertionConsumerURL = "";
		ExchangeContent idpContent = null;
		Envelope idpEnvelope = null;
		URL idpURL = null;

		// Extract idplist from authnrequest and check if the SP supports
		// the one that was chosen. If not, complain.
		idpURL = determineIdP(spContent.getResponseParts().getHeader(),
				idpEntry);

		spAssertionConsumerURL = ExtractField
				.extractAssertionConsumerURL(spContent.getResponseParts()
						.getHeader());

		// If no matching idp was found from the list the SP sent...
		if (idpURL == null) {
			logger.info("The SP did not indicate support for the chosen IdP.");
			idpURL = getURL(idpEntry.getLoc()); // Get an assertion from the IdP
												// and let the SP trust an
												// unknown IdP.
		}

		// Create the envelope with the AuthnRequest that will be sent to the
		// IdP
		idpEnvelope = EnvelopeCreator.createIdpEnvelope(spContent
				.getResponseParts());

		// Get the Assertion from the IdP (send AuthnRequest to IdP)
		idpContent = getAssertion(paosClient, idpEnvelope, idpURL);

		// If the IdP sent back anything at all as a response:
		if (idpContent != null) {
			// Check assertionConsumerURL. If it does not match, send a SOAP
			// fault to the SP/endpoint
			if (consumerUrlsMatch(idpContent, spAssertionConsumerURL)) {
				return idpContent.getResponseParts().getBody();
			} else {
				logger.debug("AssertionConsumerURLs from AuthnRequest and Response did not match.");
				logger.debug("Returning a SOAP fault message to the endpoint.");
				return EnvelopeCreator
						.createSoapFaultBody("AssertionConsumerURLs did not match.");
			}
		} // else the paosclient has complained about this.
		return null;
	}

	/**
	 * Send a GET request to an SP. Receive an AuthnRequest in the spContent.
	 * 
	 * @param spURL
	 * @param paosClient
	 * @return
	 */
	private ExchangeContent getRequestToSP(URL spURL, PaosClient paosClient) {

		// Create a new, empty SP ExchangeContent.
		ExchangeContent spContent = new ExchangeContent(null, null);

		// Send a PAOS GET request to the given SP endpoint.
		spContent = paosClient.sendHttpGETRequest(spURL, spContent);

		if (verbose) {
			System.out.println("AuthnRequest from SP: \n"
					+ new String(ParseHelper.anythingToXMLString(spContent
							.getResponseParts().getBody())));
		}

		return spContent;
	}

	/**
	 * Get an envelope with an assertion from the IdP. If this returns null,
	 * something went wrong between the IdP and Client.
	 * 
	 * @param paosClient
	 * @param spContent
	 *            ExchangeContent that contains the e
	 * @param idpURL
	 * @return
	 */
	public ExchangeContent getAssertion(PaosClient paosClient,
			Envelope idpEnvelope, URL idpURL) {

		ExchangeContent idpContent = null;

		String principal = "";
		String credentials = "";

		if (verbose) {
			System.out.println("Forwarding Authnrequest to "
					+ idpURL.toString());
			System.out.println(ParseHelper.anythingToXMLString(idpEnvelope));
		}

		Console console = null;

		console = System.console();

		// ------- LOGIN -----------
		// Ask the user for login information. Does not work in
		// an IDE.
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429
		if (console != null) {
			principal = console.readLine("Please enter username: ");
			credentials = new String(
					console.readPassword("Please enter password: "));
			System.out.println("");
		}
		// -------------------

		// Set the login credentials at IdP exchangecontent.
		idpContent = new ExchangeContent(idpEnvelope, createRealmResolver(
				principal, credentials));

		logger.debug("\nWill forward the request to: " + idpURL.toString()
				+ "\n");

		// Send everything to the IdP.
		idpContent = paosClient.send(idpURL, idpContent);

		// If this does not exist, something went wrong @
		// PaosClient.
		return idpContent;
	}

	/**
	 * Read the list of supported IDPs that the SP sent and determine if the
	 * chosen IdP is supported. Request = opensaml ECP request header.
	 * 
	 * @param header
	 * @return
	 */
	public URL determineIdP(Header header, IDPEntry idpEntry) {

		IDPList idpList = null;
		List<XMLObject> list = header.getUnknownXMLObjects();

		for (XMLObject xmlObject : list) {
			if (xmlObject.getElementQName()
					.equals(Request.DEFAULT_ELEMENT_NAME)) {
				idpList = ((Request) xmlObject).getIDPList();
			}
		}

		// If the list from the SP contains the same entry that
		// was chosen by the client...
		if (idpList != null) {
			for (IDPEntry spIdpEntry : idpList.getIDPEntrys()) {
				if (spIdpEntry.getName() != null && spIdpEntry.getLoc() != null
						&& idpEntry.getProviderID() != null)
					if (spIdpEntry.getName().equals(idpEntry.getName()))
						if (spIdpEntry.getLoc().equals(idpEntry.getLoc()))
							if (spIdpEntry.getProviderID().equals(
									idpEntry.getProviderID()))
								return getURL(spIdpEntry.getLoc());
			}
		}
		return null;
	}

	/**
	 * Verify that both ConsumerURLs match.
	 * 
	 * @param idpAssertionConsumerURL
	 * @param spAssertionConsumerURL
	 * @return
	 */
	// The serviceproviders URL MAY be relative & IdP URL MAY be absolute.
	// But not if the latest specs are followed.
	private boolean consumerUrlsMatch(ExchangeContent idpContent,
			String spAssertionConsumerURL) {
		// if header != null ..

		if (idpContent.getResponseParts().getHeader() != null) {
			// Extract the assertionConsumerURL from an IdP response header.
			String idpAssertionConsumerURL = ExtractField
					.extractAssertionConsumerURL(idpContent.getResponseParts()
							.getHeader());

			if (idpAssertionConsumerURL.equals(spAssertionConsumerURL)
					|| idpAssertionConsumerURL.endsWith(spAssertionConsumerURL))
				return true;
		}
		return false;
	}

	/**
	 * Returns a new RealmResolver with the given credentials set. These will be
	 * added as an HTTP header field in the request that is sent to the IdP. The
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
	 * Attempts to create an URL from the given parameter.
	 * 
	 * @param string
	 * @return
	 */

	// This could be made to default to a) the same port the SP used b)
	// something more standard as in 443 or 8443 c) something else

	private URL getURL(String string) {
		URL url = null;

		try {
			url = new URL(string);
			if (url.getPort() == -1) {
				logger.debug("Missing port number in URL.");
				throw new MalformedURLException();
			}
		} catch (MalformedURLException e) {
			url = null;
			logger.debug("Malformed endpoint URL: " + string);
		}
		return url;
	}

}
