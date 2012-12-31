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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import jettyClient.simpleClient.ClientConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyHelper {
	
	// Get the client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);
	
	/**
	 * Read a PrivateKey from file keyFile.
	 * 
	 * @param keyFile
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static RSAPublicKey readKeyFile(String keyFile) {

		// Read & create key
		InputStream is = null;
		byte[] key = null;
		RSAPublicKey publicKey = null;
		KeyFactory kf = null;

		try {
			is = getInputStream(keyFile);
			key = new byte[is.available()];
			kf = KeyFactory.getInstance("RSA");
			is.read(key, 0, is.available());
			is.close();
		} catch (NoSuchAlgorithmException e) {
			logger.debug("Failed to create an instance of KeyFactory.");
		} catch (IOException e) {
			logger.debug("Failed to read key file " +keyFile);
		}

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key); // Public key		

		try {
			publicKey = (RSAPublicKey) kf.generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {
			logger.debug("Invalid KeySpec. Could not generate public key." +e);
		}
		return publicKey;
	}
	
	/**
	 * Read buffer inputstream.
	 * 
	 * @param fname
	 *            Filename
	 * @return Inputstream
	 * @throws IOException
	 */
	private static InputStream getInputStream(String fname) {
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(fname);
		} catch (FileNotFoundException e) {
			logger.error("File " + fname + " not found.");
//			System.out.println("File " + fname + " not found.");
			return null;
		}
		
		DataInputStream dis = new DataInputStream(fis);
		byte[] bytes = null;

		try {
			bytes = new byte[dis.available()];
			dis.readFully(bytes);
		} catch (IOException e) {
			logger.debug("Could not read input stream.");
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return bais;
	}

	
	/**
	 * Generate a RSA key pair.
	 * @return
	 */
	public static KeyPair getKeyPair() {

		KeyPairGenerator keyGen = null;
		SecureRandom random = null;

		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchProviderException e) {
		}

		if (keyGen != null)
			return keyGen.genKeyPair(); // method returns FINAL keypair

		return null;
	}
}
