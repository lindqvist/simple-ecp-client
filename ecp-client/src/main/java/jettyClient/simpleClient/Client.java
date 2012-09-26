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

public class Client {

	/**
	 * Access a resource at the given url.
	 * 
	 * @param spURL
	 */
	public void accessResource(URL spURL, URL idpURL) {

		// This nullcheck should be unneccessary.
		if (idpURL != null) {

			Connections connections = new Connections();
			connections.accessResource(spURL, idpURL);
			// Get the resource and consume it somehow, or be denied access.
		} else {
			System.out.println("IdP endpoint not supported.");
		}
	}

}
