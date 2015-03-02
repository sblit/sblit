package net.sblit;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.sblit.configuration.Configuration;
import net.sblit.directoryWatcher.DirectoryWatcher;
import net.sblit.message.SblitMessage;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;

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
				SblitMessage message = new SblitMessage();
				while (true) {
					DirectoryWatcher directoryWatcher = new DirectoryWatcher(
							Configuration.getSblitDirectory(),
							new File(Configuration.getConfigurationDirectory().toString() + Configuration.LOG_FILE), new String(Configuration.getKey()));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (File f : directoryWatcher.getFilesToPush()){
						System.out.println(f.getAbsolutePath());
						String path = f.getAbsolutePath().replace(
								Configuration.getSblitDirectory().getAbsolutePath(), "").substring(1);
						LinkedList<Data> hashes;
						try {
							System.out.println(path);
							hashes = DirectoryWatcher.getLogs().get(path);
							System.out.println(hashes);
							for (Data channel : Configuration.getChannels()) {
								message.set(SblitMessage.FILE_REQUEST);
								message.fileRequest.path.setString(path);
								//TODO not null
								message.fileRequest.hashes.setElements(hashes);
								try {
									new StreamByteBuf(
											Configuration.getChannel(channel).
											getOutputStream()).
											write(message);
								} catch (BufException e) {
									e.printStackTrace();
								};
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}
		}).run();
	
	}
	
}
