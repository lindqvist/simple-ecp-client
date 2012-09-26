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
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.SchemaBuilder;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import net.shibboleth.utilities.java.support.xml.SchemaBuilder.SchemaLanguage;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ParseHelper {
	/**
	 * Unmarshall anything into an XMLObject. The invoker should cast the
	 * XMLObject.
	 * 
	 * @param defaultElementName
	 * @return
	 */
	public static XMLObject unmarshall(Element element) {

		if (element == null)
			return null;

		QName defaultElementName = getDefaultElementName(element);

		UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport
				.getUnmarshallerFactory();

		Unmarshaller unmarshaller = unmarshallerFactory
				.getUnmarshaller(defaultElementName);

		XMLObject object = null;

		try {
			object = (XMLObject) unmarshaller.unmarshall(element);
		} catch (UnmarshallingException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * Return the elements DEFAULT_ELEMENT_NAME.
	 * 
	 * @param node
	 * @return
	 */

	public static QName getDefaultElementName(Node node) {
		if (node == null)
			return null;

		QName qName = null;

		if (node.getPrefix() != null) {
			qName = new QName(node.getNamespaceURI(), node.getLocalName(),
					node.getPrefix());
		} else {
			qName = new QName(node.getNamespaceURI(), node.getLocalName());
		}
		return qName;
	}

	/**
	 * Turn any xml object into a text string. (Test/Debug/Printing)
	 * 
	 * @param xmlObject
	 * @return
	 */

	public static String anythingToXMLString(XMLObject object) {
		Element element = marshall(object);
		return SerializeSupport.prettyPrintXML(element);
	}

	/**
	 * Marshall anything into an element.
	 * 
	 * @param object
	 * @return
	 */
	public static Element marshall(XMLObject object) {

		MarshallerFactory MarshallerFactory = XMLObjectProviderRegistrySupport
				.getMarshallerFactory();

		Marshaller marshaller = MarshallerFactory.getMarshaller(object
				.getElementQName());

		Element element = null;

		try {
			element = marshaller.marshall(object);
		} catch (MarshallingException e) {
			e.printStackTrace();
		}
		return element;
	}

	/**
	 * Create, configure and initialize a parser for a given schema.
	 * 
	 * @param schemaFilePath
	 * @return
	 */
	private static BasicParserPool createBasicParserPool(String schemaFilePath) {

		BasicParserPool pool = null;
		Schema schema = null;
		File file = new File(schemaFilePath);

		if (file.exists()) {

			try {
				schema = SchemaBuilder.buildSchema(SchemaLanguage.XML, file);
			} catch (SAXException e) {
				System.out.println("SAXException when parsing file "
						+ schemaFilePath);
			}

			// Configure pool and set schema as given in parameter.
			pool = new BasicParserPool();
			pool.setIgnoreElementContentWhitespace(true);
			pool.setNamespaceAware(true);
			pool.setSchema(schema);

			try {
				pool.initialize(); // initialize
			} catch (ComponentInitializationException e) {
				System.out
						.println("Could not initialize parserpool using schema "
								+ schema + ".");
			}

		} else {
			System.out.println("File " + schemaFilePath + " not found.");
		}
		return pool;
	}

	/**
	 * Attempt to parse an element from text stored in a byte array, using a
	 * schema from schemafilepath.
	 * 
	 * @param bytes
	 * @param schemaFilePath
	 * @return
	 */
	public static Element extractElement(ByteArrayInputStream inputStream,
			String schemaFilePath) {
		// Create parser pool for the schema. (e.g. SOAP Envelope,
		// EntityDescriptor)
		BasicParserPool pool = createBasicParserPool(schemaFilePath);

		if (pool == null)
			return null; // :(

		// Create a XML document from the stream/response.
		Document document = null;

		try {
			document = pool.parse(inputStream);
		} catch (XMLParserException e) {
			System.out.println("Unable to parse XML.");
			return null;
		}

		// Get the element from the document.
		return document.getDocumentElement();
	}

	/**
	 * Build any XMLObject. The caller will cast the returned XMLObject.
	 * 
	 * Seems unsafe somehow? - Can fail if builder not available => catch
	 * 
	 * @param defaultElementName
	 * @return
	 */
	public static XMLObject buildObject(QName defaultElementName) {
		return XMLObjectProviderRegistrySupport.getBuilderFactory()
				.getBuilder(defaultElementName).buildObject(defaultElementName);
	}

}
