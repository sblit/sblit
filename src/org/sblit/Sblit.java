package org.sblit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.directoryWatcher.DirectoryWatcher;
import org.sblit.filesync.requests.FileRequest;

/**
 * 
 * @author Nikola
 * 
 */
public class Sblit {

	public static final String APPLICATION_IDENTIFIER = "org.sblit";

	public static void main(String[] args) {
		new Sblit();

	}

	public Sblit() {
		Configuration.initialize();
		System.out.println(new Configuration().toString());
		String message = "Hello World";
		SymmetricEncryption enc = new SymmetricEncryption(Configuration.getKey());
		System.out.println(new String(enc.decrypt(enc.encrypt(message.getBytes()))));

		// TODO Share public key

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					DirectoryWatcher directoryWatcher = new DirectoryWatcher(
							Configuration.getSblitDirectory(),
							new File(Configuration.getConfigurationDirectory().toString() + Configuration.LOG_FILE), new String(Configuration.getKey()));
					System.out.println(directoryWatcher.getFilesToPush()[0]);
					for (File f : directoryWatcher.getFilesToPush()){
						try {
							System.out.println(f.getAbsolutePath());
							MessageDigest md = MessageDigest.getInstance("SHA");
							md.update(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
							new FileRequest(md.digest()).send();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}

			}
		}).run();
	
	}
	
}
