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

import jettyClient.EnvelopeHandling.EnvelopeParts;

import org.eclipse.jetty.client.security.RealmResolver;
import org.opensaml.soap.soap11.Envelope;

/**
 * ExchangeContent contains everything that will be sent to an endpoint and the
 * response (envelope/envelopeParts) one receives back.
 * 
 * The exchangeContent should contain at least a Request- or Response SOAP
 * envelope after an exchange.
 * 
 * If it is used in a GET request to an SP, it will receive a response envelope.
 * 
 * If the exchangeContent is used to POST an envelope to an SP or an IdP it must
 * have a request envelope to send and will receive a response envelope.
 * 
 * If the exchangeContent is used to POST an envelope to an SP and receive a
 * resource that could be anything, it must have a request envelope to send and
 * could be extended to store an additional object.
 */

public class ExchangeContent {

	/* Request parts */
	private Envelope requestEnvelope = null;
	private RealmResolver realmResolver = null;

	/* Response parts */
	private Envelope responseEnvelope = null;
	private EnvelopeParts responseParts = null;
	private String cookieField = "";

	/**
	 * Constructor.
	 * 
	 * Creates a new ExchangeContent object. Any parameter can be null.
	 * 
	 * The ExchangeContent will store the sent and received SOAP envelopes and
	 * any cookies and/or credential used for authentication.
	 * 
	 * @param envelope
	 *            A SOAP Envelope that is to be sent.
	 * @param realmResolver
	 *            Credential used for HTTP BASIC authentication at the IdP.
	 */

	public ExchangeContent(Envelope envelope, RealmResolver realmResolver) {
		this.requestEnvelope = envelope;
		this.realmResolver = realmResolver;
	}

	/* Getters */

	public Envelope getRequestEnvelope() {
		return requestEnvelope;
	}

	public Envelope getResponseEnvelope() {
		return responseEnvelope;
	}

	public EnvelopeParts getResponseParts() {
		return responseParts;
	}

	public RealmResolver getRealmResolver() {
		return realmResolver;
	}

	public String getCookieField() {
		return cookieField;
	}

	/* Setters */

	public void setRequestEnvelope(Envelope envelope) {
		this.requestEnvelope = envelope;
	}

	public void setRealmResolver(RealmResolver realmResolver) {
		this.realmResolver = realmResolver;
	}

	public void setCookieField(String cookieField) {
		this.cookieField = cookieField;
	}

	public void setResponseEnvelope(Envelope responseEnvelope) {
		this.responseEnvelope = responseEnvelope;
	}

	public void setResponseParts(EnvelopeParts responseParts) {
		this.responseParts = responseParts;

	}

}
