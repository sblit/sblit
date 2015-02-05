package net.sblit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import net.sblit.configuration.Configuration;
import net.sblit.directoryWatcher.DirectoryWatcher;
import net.sblit.filesync.requests.FileRequest;

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
		

		// TODO Share public key

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					DirectoryWatcher directoryWatcher = new DirectoryWatcher(
							Configuration.getSblitDirectory(),
							new File(Configuration.getConfigurationDirectory().toString() + Configuration.LOG_FILE), new String(Configuration.getKey()));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(directoryWatcher.getFilesToPush().toString());
					for (File f : directoryWatcher.getFilesToPush()){
						System.out.println(f.getAbsolutePath());
						try {
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
