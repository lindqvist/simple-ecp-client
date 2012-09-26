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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import jettyClient.metadata.IdpMetadata;
import jettyClient.objectProviderRegisterer.ObjectProviderRegisterer;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.IDPEntry;

/**
 * ECP Client prototype.
 */

public class Main {

	public static void main(String[] args) {

		Client client = null;
		URL spURL = null, idpURL = null;
		String idpId = "";

		// Initialize and configure OpenSAML (Builderfactory, Marshaller...)
		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			System.out.println("Could not initialize OpenSAML.");
			e.printStackTrace();
		}

		// Register PAOS request header builder + marshaller.
		ObjectProviderRegisterer.register();

		// Load metadata (from /resources/metadata)
		// Currently supports metadata files with one single <EntityDescriptor>
		// that contains an <IDPSSODescriptor>.
		IdpMetadata metadata = new IdpMetadata();

		// Test
		args = new String[2];
		args[0] = "INSERT SP URL"; // e.g. http://localhost:8443/
		args[1] = "INSERT IDP ID"; // metadata is read from /resources/metadata
		// Test

		// Attempt to read the command line arguments.
		if (args.length == 2) {

			idpId = args[1];

			// Check if IdP id is in list.
			IDPEntry idpEntry = metadata.getIdpList().get(idpId);

			if (idpEntry != null) {
				// Try to create endpoint URLs.
				spURL = getURL(args[0]);
				idpURL = getURL(idpEntry.getLoc());

				// If both endpoints are ok, send a request.
				if (spURL != null && idpURL != null) {
					client = new Client();
					client.accessResource(spURL, idpURL); // send
				}

			} else {
				System.out.println("No IdP found matching id " + idpId + ".");
				printHelp(metadata.getIdpList());
			}

		} else {
			System.out.println("Wrong argument(s).");
			printHelp(metadata.getIdpList());
		}
		System.out.println("Exit.");
	}

	/**
	 * Attempts to create an URL from the given parameter.
	 * 
	 * @param string
	 * @return
	 */
	private static URL getURL(String string) {
		URL url = null;

		try {
			url = new URL(string);
			if (url.getPort() == -1) {
				System.out.println("Missing port number in URL.");
				throw new MalformedURLException();
			}
		} catch (MalformedURLException e) {
			url = null;
			System.out.println("Malformed endpoint URL: " + string);
		}
		return url;
	}

	/**
	 * Prints some help and a list of registered IdPs.
	 */
	private static void printHelp(HashMap<String, IDPEntry> hashMap) {
		System.out
				.println("Usage: java -jar simpleClient.jar 'Service provider endpoint URL:port' 'IdP ID'");
		System.out
				.println("Example: java -jar simpleClient.jar  http://localhost:8443/ some-shibboleth-idp-id");
		System.out.println("List of registered IdP IDs: ");

		for (Iterator iterator = hashMap.keySet().iterator(); iterator
				.hasNext();) {
			System.out.println((String) iterator.next()); // Because the keys
															// are ids.
		}
	}
}