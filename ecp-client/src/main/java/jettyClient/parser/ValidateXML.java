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
import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import net.shibboleth.utilities.java.support.xml.SchemaBuilder;
import net.shibboleth.utilities.java.support.xml.SchemaBuilder.SchemaLanguage;

import org.xml.sax.SAXException;

public class ValidateXML {

	/**
	 * Validate SOAP message XML
	 * 
	 * Return true if responseMessage is a SOAP Envelope.
	 * 
	 * @param responseMessage
	 * @return
	 */

	public static boolean isValid(ByteArrayInputStream responseStream,
			String schemaFilePath) {

		Source xmlFile = new StreamSource(responseStream);

		// String schemaFilePath = "resources/schema/soap-envelope.xsd";
		Schema schema = null;

		try {
			schema = SchemaBuilder.buildSchema(SchemaLanguage.XML, new File(
					schemaFilePath));
		} catch (SAXException e) {
			System.out.println("Invalid schema given.");
			return false;
		}

		Validator validator = schema.newValidator();

		try {
			validator.validate(xmlFile);
		} catch (SAXException e) {
			System.out.println("The file contained invalid XML.");
			return false;
		} catch (IOException e) {
			System.out.println("The file contained invalid XML.");
			return false;
		}
		return true;
	}

	/**
	 * Validate a SOAP Envelope.
	 * 
	 * @param responseMessage
	 * @return
	 */
	public static boolean isValidEnvelope(ByteArrayInputStream responseStream) {

		String schemaFilePath = "resources/schema/soap-envelope.xsd";
		return isValid(responseStream, schemaFilePath);
	}

	/**
	 * Validate an EntityDescriptor that should contain metadata.
	 * 
	 * @param responseMessage
	 * @return
	 */
	public static boolean isValidEntityDescriptor(
			ByteArrayInputStream inputStream) {
		String schemaFilePath = "resources/schema/saml-schema-metadata-2.0.xsd"; // :(
		return isValid(inputStream, schemaFilePath);
	}
}
