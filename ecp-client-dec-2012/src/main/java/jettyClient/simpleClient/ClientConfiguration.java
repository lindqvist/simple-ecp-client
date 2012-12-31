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

/**
 * Contains all the filepaths for and -names of
 * files etc that the client needs.
 * 
 * @author carolina
 *
 */
public class ClientConfiguration {
	
	/* Metadata location*/
	public static final String metadataFolder = "resources/metadata";
	
	/* Schema files */
	public static final String soapEnvelopeSchemaLocation = "resources/schema/soap-envelope.xsd";
	public static final String metadataSchemaLocation = "resources/schema/saml-schema-metadata-2.0.xsd";
	
	// TEST (validates assertion XML)
	public static final String assertionSchemaLocation = "resources/schema/saml-schema-assertion-2.0.xsd";
	
	/* Logger name */
	public static final String logger = "defaultLogger";
	
	/* Cli parameters */
//	public static final String verbose = "verbose";
//	public static final String help = "help";
//	public static final String idpID = "idp";
	
}
