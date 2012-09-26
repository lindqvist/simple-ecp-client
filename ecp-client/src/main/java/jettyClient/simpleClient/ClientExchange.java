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

import org.eclipse.jetty.client.ContentExchange;

public class ClientExchange extends ContentExchange {

	public ClientExchange(Boolean cache_headers) {
		super(cache_headers);
	}

	public ClientExchange() {
		super();
	}

	@Override
	protected void onConnectionFailed(Throwable x) {
		System.out.println("Connection failed: " + x.toString());
	}
}
