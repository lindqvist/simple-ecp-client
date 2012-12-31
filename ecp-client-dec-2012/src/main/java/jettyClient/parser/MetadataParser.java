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
import java.util.List;

import jettyClient.metadata.LoadFile;
import jettyClient.simpleClient.ClientConfiguration;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.w3c.dom.Element;

public class MetadataParser {

	/**
	 * Parse a String of Metadata (An entitydescriptor (Metadatastring could
	 * contain many of these, cant handle that yet, I think.)) and return the
	 * resulting Entitydescriptor object.
	 * 
	 * @param metadataString
	 * @return
	 */

	public static EntityDescriptor parseMetadata(String idpMetadataFile,
			String entityDescriptorSchema) {

		String entityDescriptorString = LoadFile.read(idpMetadataFile);

		// Check if the read file is empty.
		if (entityDescriptorString.isEmpty())
			return null;
		
		// Validate that the file contains metadata.
		if (ValidateXML.isValidEntityDescriptor(new ByteArrayInputStream(entityDescriptorString.getBytes())) == false)
			return null;

		Element element = ParseHelper.extractElement(
				new ByteArrayInputStream(entityDescriptorString.getBytes()), entityDescriptorSchema);
		
		// Can also be an entitiesdescriptor element => parse failure.
		EntityDescriptor ed = (EntityDescriptor) ParseHelper
				.unmarshall(element);

		return ed;
	}

	/**
	 * Extract an IDPentry that indicates ECP support from IdP metadata, read
	 * from a file.
	 * 
	 * @param idpMetadataFile
	 * @return
	 */
	public static IDPEntry extractEntry(String idpMetadataFile) {

		String supportedProtocol = SAMLConstants.SAML20P_NS; // urn:oasis:names:tc:SAML:2.0:protocol
		String soapSupport = SAMLConstants.SAML2_SOAP11_BINDING_URI; // urn:oasis:names:tc:SAML:2.0:bindings:SOAP
		String entryname = "ECP";

		String entityDescriptorSchema = ClientConfiguration.metadataSchemaLocation; 

		EntityDescriptor ed = parseMetadata(idpMetadataFile,
				entityDescriptorSchema);
		if (ed == null)
			return null;

		IDPEntry entry = (IDPEntry) ParseHelper
				.buildObject(IDPEntry.DEFAULT_ELEMENT_NAME);

		// IF the ECP profile is the only SSO Profile that uses SOAP, then this
		// information can be relied on.
		List<SingleSignOnService> list = ed.getIDPSSODescriptor(
				supportedProtocol).getSingleSignOnServices(); // = list of all
																// sso services
																// including ECP
		// https://wiki.shibboleth.net/confluence/display/SHIB2/IdP+ECP+Extension

		// Find ECP support.
		for (SingleSignOnService singleSignOnService : list) {

			if (singleSignOnService.getBinding().equals(soapSupport)) {
				entry.setProviderID(ed.getEntityID()); // or getID(), find out
														// if either is MUST and
														// get that.
				entry.setLoc(singleSignOnService.getLocation());
				entry.setName(entryname);

				return entry;
			}
		}
		return null;
	}
}
