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
import java.util.HashMap;
import java.util.Iterator;

import jettyClient.metadata.IdpMetadata;
import jettyClient.objectProviderRegisterer.ObjectProviderRegisterer;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.IDPEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ECP Client prototype.
 */

public class Main {

	// Client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);

	public static void main(String[] args) {

		// Client client = null; // command line client -version
		Client clientLogin = null; // client login version

		URL spURL = null, idpURL = null;
		String idpId = "";

		// Initialize and configure OpenSAML (Builderfactory, Marshaller...)
		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			logger.debug("Could not initialize OpenSAML.\n" + e);
		}

		// Register PAOS request header builder + marshaller.
		ObjectProviderRegisterer.register();

		// Load metadata (load it from a folder that contains metadata xml)
		IdpMetadata metadata = new IdpMetadata();

		// Parse command line parameters into configuration info
		ClientOptions options = Parameters.setOptions(args);

		if (options != null) {

			// Get the IdP id
			idpId = options.getIdpID();

			// Check if IdP id is in list.
			IDPEntry idpEntry = metadata.getIdpList().get(idpId);

			if (idpEntry != null) {
				// Get endpoint URLs.

				idpURL = Parameters.getURL(idpEntry.getLoc()); // :(
				spURL = options.getSpURL();

				// If both endpoints are ok, which they should be, send a
				// request.
				if (spURL != null && idpURL != null) {
					clientLogin = new Client(); // login client version
					clientLogin.accessResource(options, idpEntry);
				}

			} else {
				System.out.println("No IdP found matching id " + idpId + ".");
				printIdPList(metadata.getIdpList());
			}
		}
	}

	/**
	 * Prints some help and a list of registered IdPs.
	 */
	private static void printIdPList(HashMap<String, IDPEntry> hashMap) {
		System.out.println("List of registered IdP IDs: ");

		for (Iterator iterator = hashMap.keySet().iterator(); iterator
				.hasNext();) {
			System.out.println((String) iterator.next()); // Because the keys
															// are ids.
		}
	}
}