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

import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

// Could store the content as text, but then one would need separate
// schemas in order to parse the xml back to an XMLObject.

public class EnvelopeParts {

	private Header header;
	private Body body;
	private Envelope envelope;

	/**
	 * Constructor, store envelope parts.
	 * 
	 * @param header
	 * @param body
	 * @param envelope
	 */
	public EnvelopeParts(Header header, Body body, Envelope envelope) {

		this.header = header;
		this.body = body;
		this.envelope = envelope;
	}

	/* Getters and setters */

	public Header getHeader() {
		return header;
	}

	public Body getBody() {
		return body;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

}
