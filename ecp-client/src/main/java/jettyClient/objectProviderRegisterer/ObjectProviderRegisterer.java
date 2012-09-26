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

package jettyClient.objectProviderRegisterer;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;

import ecp.liberty.paos.Request;
import ecp.liberty.paos.impl.RequestBuilder;
import ecp.liberty.paos.impl.RequestMarshaller;
import ecp.liberty.paos.impl.RequestUnmarshaller;

import ecp.liberty.paos.Response;
import ecp.liberty.paos.impl.ResponseBuilder;
import ecp.liberty.paos.impl.ResponseMarshaller;
import ecp.liberty.paos.impl.ResponseUnmarshaller;

public class ObjectProviderRegisterer {

	public static void register() {
		// Register openliberty PAOS request header block.
		registerObjectProvider(Request.DEFAULT_ELEMENT_NAME,
				new RequestBuilder(), new RequestMarshaller(),
				new RequestUnmarshaller());
		// Register openliberty PAOS response header block.
		registerObjectProvider(Response.DEFAULT_ELEMENT_NAME,
				new ResponseBuilder(), new ResponseMarshaller(),
				new ResponseUnmarshaller());
	}

	/**
	 * Registers an XMLObject as an objectprovider.
	 * 
	 * @param defaultElementName
	 * @param builder
	 * @param marshaller
	 * @param unmarshaller
	 */
	private static void registerObjectProvider(QName defaultElementName,
			XMLObjectBuilder builder, Marshaller marshaller,
			Unmarshaller unmarshaller) {
		XMLObjectProviderRegistrySupport.registerObjectProvider(
				defaultElementName, builder, marshaller, unmarshaller);

	}

}
