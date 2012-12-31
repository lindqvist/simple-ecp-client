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

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.core.IDPList;
import org.opensaml.saml.saml2.ecp.Response;
import org.opensaml.soap.soap11.Header;
import org.opensaml.soap.wssecurity.BinarySecurityToken;
import org.opensaml.soap.wssecurity.Security;

import ecp.liberty.paos.Request;

/**
 * ExtractField contains methods which can extract fields from SOAP Envelope headers.
 * 
 * The header fields that are important for the ECP profile are the
 * AssertionConsumerURL fields and IdPEntries.
 * 
 * @author carolina
 *
 */

public class ExtractField {
	
	/**
	 * Extracts a responseconsumerURL from a PAOS Request header block sent in a
	 * SOAP Envelope from the SP.
	 * 
	 * Should be able to extract the same text string (named
	 * AssertionConsumerServiceURL) from an IdP Response.
	 * 
	 * @param xmlObject
	 * @return
	 */
	public static String extractAssertionConsumerURL(Header header) {

		String responseConsumerURL = "";
		List<XMLObject> list = null;

		if (header.hasChildren()) {
			list = header.getOrderedChildren();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				XMLObject xmlObject = (XMLObject) iterator.next();
				QName name = xmlObject.getElementQName();

				if (name.equals(Request.DEFAULT_ELEMENT_NAME)) {
					responseConsumerURL = ((Request) xmlObject)
							.getResponseConsumerURL();
				} else if (name.equals(Response.DEFAULT_ELEMENT_NAME)) {
					responseConsumerURL = ((Response) xmlObject)
							.getAssertionConsumerServiceURL();
				}
			}
		}
		return responseConsumerURL;
	}

	/**
	 * Attempt to extract a preferred IdP from the Request header block that was
	 * sent with the AuthnRequest SOAP Envelope.
	 * 
	 * @param header
	 * @return
	 */
	public static IDPEntry extractIdPURL(Header header) {

		if (header == null)
			return null;

		// List = a list of header blocks (ECP, PAOS)
		IDPList idpList = extractIdPList(header);

		// If there is a list and it is not empty.
		if (idpList != null && !idpList.isNil()) {
			if (idpList.getIDPEntrys().size() == 1)
				return idpList.getIDPEntrys().get(0);

			// if there are many entries do something else:
		}
		return null;
	}

	/**
	 * Extract an IdPList XMLObject from a Header.
	 * 
	 * Returns null if no IdPList is found.
	 * 
	 * @param list
	 * @return
	 */
	private static IDPList extractIdPList(Header header) {
		if (header == null)
			return null;

		List<XMLObject> list = header.getUnknownXMLObjects();

		for (XMLObject xmlObject : list) {
			if (xmlObject.getElementQName() != null)
				if (xmlObject
						.getElementQName()
						.equals(org.opensaml.saml.saml2.ecp.Request.DEFAULT_ELEMENT_NAME))
					return ((org.opensaml.saml.saml2.ecp.Request) xmlObject)
							.getIDPList();
		}
		return null;
	}
	
	/**
	 * Extract a BinarySecurityToken from a Header.
	 * @param header
	 * @return
	 */
	public static BinarySecurityToken extractToken(Header header, String id) {

		if (header != null) {
			List<XMLObject> list = header.getUnknownXMLObjects();
			Response response = null;

			XMLObject obj = list.get(0);

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				XMLObject xmlObject = (XMLObject) iterator.next();

				// Ws trust security header block
				if (xmlObject.getElementQName().equals(Security.ELEMENT_NAME)) {

					for (XMLObject securityXMLObject : ((Security) xmlObject)
							.getUnknownXMLObjects()) {
						if (securityXMLObject.getElementQName().equals(
								// Binarysecuritytoken from header
								BinarySecurityToken.ELEMENT_NAME)) {
							BinarySecurityToken binaryToken = (BinarySecurityToken) securityXMLObject;
							if (binaryToken.getWSUId().equals(id)) {
								return binaryToken;
							}
						}
					}

				}
			}
		}
		return null;
	}
	
}
