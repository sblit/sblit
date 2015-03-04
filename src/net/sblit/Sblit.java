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
							Configuration.getSblitDirectory(), new File(Configuration
									.getConfigurationDirectory().toString()
									+ Configuration.LOG_FILE), new String(Configuration.getKey()));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (File f : directoryWatcher.getFilesToPush()) {
						System.out.println(f.getAbsolutePath());
						String path = f.getAbsolutePath()
								.replace(Configuration.getSblitDirectory().getAbsolutePath(), "")
								.substring(1);
						LinkedList<Data> hashes;
						try {
							hashes = DirectoryWatcher.getLogs().get(path);
							message.set(SblitMessage.FILE_REQUEST);
							message.fileRequest.path.setString(path);
							message.fileRequest.hashes.setElements(hashes);
							for (Data channel : Configuration.getChannels()) {
								try {
									new StreamByteBuf(Configuration.getChannel(channel)
											.getOutputStream()).write(message);
								} catch (BufException e) {
									e.printStackTrace();
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					for (File f : directoryWatcher.getFilesToDelete()) {
						String path = f.getAbsolutePath()
								.replace(Configuration.getSblitDirectory().getAbsolutePath(), "")
								.substring(1);
						message.set(SblitMessage.DELETE_MESSAGE);
						message.deleteMessage.filePath.setString(path);
						for (Data channel : Configuration.getChannels()) {
							try {
								new StreamByteBuf(Configuration.getChannel(channel)
										.getOutputStream()).write(message);
							} catch (BufException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
		}).run();

	}
}
