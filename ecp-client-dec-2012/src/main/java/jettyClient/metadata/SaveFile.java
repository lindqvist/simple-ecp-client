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
package jettyClient.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jettyClient.simpleClient.ClientConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveFile {
	
	// Get the client logger
		private final static Logger logger = LoggerFactory
				.getLogger(ClientConfiguration.logger);

	public static void writeToFile(String text, String pathname) {
		
		File file = new File(pathname);
		FileOutputStream fos = null;
		Boolean append = false;
		
		//if (file.exists())... // ask permission to overwrite, not needed here.
		
		try {
			fos = new FileOutputStream(file, append);
		} catch (FileNotFoundException e) {
			logger.debug("File " +pathname  +"not found.");
		}
		
		try {
			fos.write(text.getBytes());
			fos.close();
		} catch (IOException e) {
			logger.debug("Could not read " +pathname +"\n" +e);
		}
	}
}
