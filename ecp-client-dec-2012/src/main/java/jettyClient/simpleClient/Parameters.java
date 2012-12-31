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
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the parameters given as command line options.
 * 
 * @author carolina
 * 
 */
public class Parameters {

	// Client logger
	private final static Logger logger = LoggerFactory
			.getLogger(ClientConfiguration.logger);


	// Strings describing the parameters
	private static String verbose = "verbose";
	private static String help = "help";
	private static String idpID = "idp";
	private static String spEndpoint = "endpoint";

	// Defines the command for starting the client.
	private static String usage = "java -jar client.jar <SP endpoint> [options]";

	/**
	 * Stores the given command line arguments for the client to use.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @return Options for the client to use.
	 */
	public static ClientOptions setOptions(String[] args) {

		CommandLine line = null;
		Options options = defineOptions();
		BasicParser parser = new BasicParser();

		if (args.length > 0) {
			// Parse the given options
			try {
				line = parser.parse(options, args);
			} catch (ParseException e) {

				if (e instanceof MissingArgumentException) {
					System.out.println("Error: " + e.getMessage());
					logger.debug("MissingArgumentException: " + e.getMessage());
				} else {
					System.out.println("Error: " + e.getMessage());
					logger.debug("ParseException:" + e.getMessage());
				}
				showHelp(options);
			}

			// Print help
			if (line.hasOption(help)) {
				showHelp(options);
			}

			// Set the options read from the command line.
			ClientOptions clientOptions = setOptions(line);

			return clientOptions;

		} else {
			showHelp(options);
		}
		// Unreachable.
		return null;
	}

	/**
	 * Shows a help message AND exits the application.
	 * 
	 * @param options
	 */
	private static void showHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(usage, options);
		logger.debug("Help was needed.");
		System.exit(0);
	}

	/**
	 * Check if the given parameters are correct.
	 * 
	 * @param line
	 *            Command line arguments.
	 * @return Configuration options for the client.
	 */
	private static ClientOptions setOptions(CommandLine line) {

		URL spURL = null;

		// Create the configuration for the client.
		ClientOptions options = new ClientOptions();

		// Get the arguments not matching options.
		String leftoverArgs[] = line.getArgs();

		// If there is an SP given.
		if (leftoverArgs.length > 0) {

			// Parse the SP URL
			spURL = getURL(leftoverArgs[0]);

			// // Set the IdP ID
			// options.setIdpID(leftoverArgs[1]);

		} else {
			System.out.println("SP endpoint missing. Usage: " + usage);
			System.out
					.println("Example: java -jar simpleClient.jar  http://localhost:8443/initiateECP some-shibboleth-idp-id");
		}

		// Prints help if the SP URL has failed.
		if (spURL != null) {
			options.setSpURL(spURL);
		} else {
			System.out.println("Invalid SP URL.");
			System.out.println("Usage: " + usage);
			System.out
					.println("Example: java -jar simpleClient.jar  http://localhost:8443/initiateECP some-shibboleth-idp-id");
			System.exit(1);
		}

		// Save the idp-id.
		if (line.hasOption(idpID)) {
			options.setIdpID(line.getOptionValue(idpID));
			logger.debug("Using IdP id: " +idpID);
		} else {
			System.out.println("No IdP id specified.");
			System.out.println("Usage: " + usage);
			System.out
					.println("Example: java -jar simpleClient.jar  http://localhost:8443/initiateECP some-shibboleth-idp-id");
			System.exit(1);
		}		

		// Verbose
		if (line.hasOption(verbose)) {
			options.setVerbose(true);
			logger.debug("Verbose mode activated.");
		}

		// SP Endpoint
		if (line.hasOption(spEndpoint)) {
			String endpointValue = line.getOptionValue(spEndpoint);
			URL endpoint = getURL(endpointValue);
			options.setSpEndpoint(endpoint);
			logger.debug("The endpoint URL is : " + endpointValue);

			if (endpoint == null) {
				System.out.println("Invalid SP endpoint URL: " +endpointValue);
				System.exit(1);
			}
		}

		return options;
	}

	/**
	 * Defines the options the client will accept as parameters.
	 * 
	 * @return Options for the client.
	 */
	private static Options defineOptions() {
		Options options = new Options();

		// Options that MUST have an argument.
		options.addOption(idpID, true, "The IdP ID.");
		options.addOption("e", spEndpoint, true,
				"The SP endpoint URL. Given as URL:PORT/URI.");

		// Options that MUST NOT have an argument.
		options.addOption("v", verbose, false,
				"Prints the messages sent between the client, SP and IdP.");
		options.addOption("h", help, false, "Prints a help message.");

		return options;
	}

	/**
	 * Attempts to create an URL from the given parameter.
	 * 
	 * @param string
	 *            A string value that will be turned into an URL.
	 * @return An URL with the value of the given string.
	 */
	public static URL getURL(String string) {
		URL url = null;

		try {
			url = new URL(string);
			if (url.getPort() == -1) {
				System.out.println("Error: Missing port number in URL.");
				logger.info("Error: Missing port number in URL." + string);
				throw new MalformedURLException();
			}
		} catch (MalformedURLException e) {
			url = null;
			logger.info("Malformed endpoint URL: " + string);
		}
		return url;
	}
}
