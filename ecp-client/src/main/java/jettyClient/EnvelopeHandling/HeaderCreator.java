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

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Header;

import ecp.liberty.paos.Response;

public class HeaderCreator {

	/**
	 * Build a Header for the Assertion Envelope sent from the ECP to an SP. The
	 * Header contains a mandatory PAOS response header block.
	 * 
	 * @return
	 */
	public static Header buildEcpToSpHeader() {
		Header header = buildHeader();
		header.getUnknownXMLObjects().add(buildPaosResponseBlock());
		return header;
	}

	/**
	 * Builds a PAOS response block, filled with predefined content as specified
	 * in sstc-saml-ecp-v2.0-wd02 section 4.2.4.5.
	 * 
	 * @return
	 */
	public static Response buildPaosResponseBlock() {

		Boolean mustUnderstand = true;
		String actor = "http://schemas.xmlsoap.org/soap/actor/next";

		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport
				.getBuilderFactory();

		SAMLObjectBuilder<Response> responseBuilder = ((SAMLObjectBuilder<Response>) builderFactory
				.getBuilder(Response.DEFAULT_ELEMENT_NAME));

		Response response = responseBuilder.buildObject();

		response.setSOAP11MustUnderstand(mustUnderstand);
		response.setSOAP11Actor(actor);

		return response;
	}

	/**
	 * Builds a SOAP message header.
	 * 
	 * @return A SOAP message header.
	 */
	public static Header buildHeader() {

		SOAPObjectBuilder<Header> headerBuilder = (SOAPObjectBuilder<Header>) XMLObjectProviderRegistrySupport
				.getBuilderFactory().getBuilder(Header.DEFAULT_ELEMENT_NAME);

		Header header = (Header) headerBuilder
				.buildObject(Header.DEFAULT_ELEMENT_NAME);

		return header;
	}

}
