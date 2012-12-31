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

import java.net.URL;

import jettyClient.EnvelopeHandling.EnvelopeParts;
import jettyClient.simpleClient.ClientConfiguration;

import org.eclipse.jetty.client.security.RealmResolver;
import org.eclipse.jetty.http.HttpFields;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ExchangeContent contains everything that will be sent to an endpoint and the response
 * (envelope/envelopeParts) one receives back. 
 * 
 * The exchangeContent should contain at least a Request- or Response SOAP envelope
 * after an exchange.
 * 
 * If it is used in a GET request to an SP, it will receive a response envelope.
 * 
 * If the exchangeContent is used to POST an envelope to an SP or an IdP it must have
 * a request envelope to send and will receive a response envelope.
 * 
 * If the exchangeContent is used to POST an envelope to an SP and receive a resource
 * that could be anything, it must have a request envelope to send and could be
 * extended to store an additional object.
 * 
 *  @author carolina
 */
public class ExchangeContent {
	
	// Get the client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);
	
	/* Request parts */
	private Envelope requestEnvelope = null;
	private RealmResolver realmResolver = null;
	private URL endpointURL = null;
	
	/* Response parts */
//	private Envelope responseEnvelope = null;
	private EnvelopeParts responseParts = null; 
	private String cookieField = "";
	private HttpFields headers = null;
	
	/* *
	 * Any response that is not an envelope will be stored here.
	 * Then it can be serialized into any object later, when needed.
	 * */
	private byte[] otherResponse = null;

	/**
	 * Constructor.
	 * 
	 * Creates a new ExchangeContent object. Any parameter can be null.
	 * 
	 * The ExchangeContent will store the sent and received SOAP envelopes
	 * and any cookies and/or credential used for authentication.
	 * 
	 * @param envelope A SOAP Envelope that is to be sent.
	 * @param realmResolver Credential used for HTTP BASIC authentication at the IdP.
	 */
	
	public ExchangeContent(Envelope envelope, RealmResolver realmResolver) {
		this.requestEnvelope = envelope;
		this.realmResolver = realmResolver;
	}
		
	/* Getters */
	
	public Envelope getRequestEnvelope() {
		return requestEnvelope;
	}
	
//	public Envelope getResponseEnvelope() {
//		return responseEnvelope;
//	}
	
	public EnvelopeParts getResponseParts() {
		return responseParts;
	}
	
	public RealmResolver getRealmResolver() {
		return realmResolver;
	}
	
	public String getCookieField() {
		return cookieField;
	}
	
	public byte[] getOtherResponse() {
		return otherResponse;
	}
	
	public void setHeaders(HttpFields headers) {
		this.headers = headers;
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

//	public void setResponseEnvelope(Envelope responseEnvelope) {
//		this.responseEnvelope = responseEnvelope;		
//	}

	public void setResponseParts(EnvelopeParts responseParts) {
		this.responseParts = responseParts;		
	}

	public void setOtherResponse(byte[] otherResponse) {
		this.otherResponse = otherResponse;		
	}
	
	public HttpFields getHeaders() {
		return headers;
	}
}
