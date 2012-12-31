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
package jettyClient.metadata;

import java.io.File;
import java.util.HashMap;

import jettyClient.parser.MetadataParser;
import jettyClient.simpleClient.ClientConfiguration;

import org.opensaml.saml.saml2.core.IDPEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdpMetadata {

	private final String metadataFolder = ClientConfiguration.metadataFolder;

	private HashMap<String, IDPEntry> idpList;

	// Client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);

	/**
	 * Register an idp from the given file.
	 * 
	 * (Load a list of IdP's from a given location.)
	 * 
	 * @param idpMetadataFile
	 */
	public IdpMetadata() {
		idpList = loadIdentityProviderList(metadataFolder);
	}

	/**
	 * Attempt to create a list of IdPs registered with this client.
	 * 
	 * @param idpMetadataFile
	 * @return
	 */
	private HashMap<String, IDPEntry> loadIdentityProviderList(
			String metadataFolder) {

		IDPEntry entry = null;
		HashMap<String, IDPEntry> map = new HashMap<String, IDPEntry>();

		File folder = new File(metadataFolder);

		if (folder != null) {
			String[] metadataFiles = folder.list();

			if (metadataFiles != null) {
				for (String filename : metadataFiles) {

					filename = metadataFolder + "/" + filename; // The "/" can
																// cause
																// problems.

					logger.info("Extracting entry from " + filename);

					// Get the idpEntry from a file (since the ECP entry is
					// everything we need)
					entry = MetadataParser.extractEntry(filename);

					if (entry != null) {
						map.put(entry.getProviderID(), entry);
						logger.info("Added an IdP with ID "
								+ entry.getProviderID() + " from file: "
								+ filename);
					} else {
						logger.info("Failed to extract IdP endpoint entry from file: "
								+ filename);
					}
				}

				if (map.isEmpty())
					logger.info("Could not find metadata for any IdentityProvider in folder "
							+ metadataFolder);

			} else {
				System.out.println("The IdP metadata folder " + metadataFolder
						+ " was not found");
				logger.info("The IdP metadata folder " + metadataFolder
						+ " was not found");
			}
		}
		return map;
	}

	/**
	 * Returns the list of registered IdPs.
	 * 
	 * @return
	 */
	public HashMap<String, IDPEntry> getIdpList() {
		return idpList;
	}

}
