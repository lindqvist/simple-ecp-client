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

public class ClientOptions {
	
	private String idpID = "";
	private URL spEndpoint = null;

	private boolean verbose;
	private URL spURL;
	
	// The IDP URL is set in the Main class if the IDP-ID matches a metadata entry.
	private URL idpUrl;

	/*Getters and setters */
	
	public URL getSpURL() {
		return spURL;
	}
	public void setSpURL(URL spURL) {
		this.spURL = spURL;
	}
	
	public URL getIdpUrl() {
		return idpUrl;
	}
	public void setIdpUrl(URL idpUrl) {
		this.idpUrl = idpUrl;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public String getIdpID() {
		return idpID;
	}
	public void setIdpID(String idpID) {
		this.idpID = idpID;
	}

	public URL getSpEndpoint() {
		return spEndpoint;
	}
	public void setSpEndpoint(URL endpoint) {
		this.spEndpoint = endpoint;
	}
}
