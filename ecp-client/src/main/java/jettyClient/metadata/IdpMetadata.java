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

import org.opensaml.saml.saml2.core.IDPEntry;

public class IdpMetadata {

	// private final String idpMetadataFile =
	// "resources/metadata/shibboleth-idp-metadata.xml";
	private final String metadataFolder = "resources/metadata";

	// private IDPList IDPList;
	private HashMap<String, IDPEntry> idpList;

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
		String[] metadataFiles = folder.list();

		for (String filename : metadataFiles) {

			filename = metadataFolder + "/" + filename; // Can cause problems.

			System.out.println("\nExtracting entry from " + filename);

			// Get the idpEntry from a file (since the ECP entry is everything
			// we need)
			entry = MetadataParser.extractEntry(filename);

			if (entry != null) {
				map.put(entry.getProviderID(), entry);
				System.out.println("Added an IdP with ID "
						+ entry.getProviderID() + " from file: " + filename);
			} else {
				System.out
						.println("Failed to extract IdP endpoint entry from file: "
								+ filename);
			}
		}

		if (map.isEmpty())
			System.out
					.println("Could not find metadata for any IdentityProvider in folder "
							+ metadataFolder);

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
