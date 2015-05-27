package net.sblit.configuration;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sblit.crypto.RSAPublicKey;

public class ReceiverConfiguration {

	protected static final String RECEIVER_PATH = "/receivers.txt";
	protected static final String FOREIGN_RECEIVER_PATH = "/freceivers.txt";
	private HashMap<RSAPublicKey, String> receivers = new HashMap<RSAPublicKey, String>();
	private File receiverFile;
	private File foreignReceiverFile;
	private HashMap<RSAPublicKey, String> foreignReceivers = new HashMap<RSAPublicKey, String>();
	private HashSet<RSAPublicKey> activeReceivers = new HashSet<RSAPublicKey>();

	public ReceiverConfiguration(String configurationDirectory) {
		receiverFile = new File(configurationDirectory
				+ RECEIVER_PATH);
		if (receiverFile.exists()) {
			try {
				String[] receivers = new String(
						Files.readAllBytes(Paths
								.get(receiverFile
										.getAbsolutePath())))
						.split(",");
				for (String receiver : receivers) {
					String[] temp = receiver.split("=");
					this.receivers.put(new RSAPublicKey(new BigInteger(temp[0].split(";")[1]), new BigInteger(temp[0].split(";")[0])), temp[1]);
				}
			} catch (Exception e) {
				System.out
						.println("Keine Receiver eingetragen!");
			}
		} else {
			try {
				receiverFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		foreignReceiverFile = new File(configurationDirectory
				+ FOREIGN_RECEIVER_PATH);
		if (foreignReceiverFile.exists()) {
			try {
				String[] receivers = new String(
						Files.readAllBytes(Paths
								.get(foreignReceiverFile
										.getAbsolutePath())))
						.split(",");
				for (String receiver : receivers) {
					String[] temp = receiver.split("=");
					this.foreignReceivers.put(new RSAPublicKey(new BigInteger(temp[0].split(";")[1]), new BigInteger(temp[0].split(";")[0])), temp[1]);
				}
			} catch (Exception e) {
				System.out
						.println("Keine fremden Receiver eingetragen!");
			}
		} else {
			try {
				foreignReceiverFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<RSAPublicKey, String> getReceivers() {
		return receivers;
	}

	public HashMap<RSAPublicKey, String> getForeignReceivers() {
		return foreignReceivers;
	}

	void addReceiver(String name, RSAPublicKey receiver) {
		receivers.put(receiver, name);

		updateFile(receiverFile, receivers);
	}

	private synchronized void updateFile(File file,
			Map<RSAPublicKey, String> receivers) {
		try {
			String temp = receivers.toString();
			temp = temp.substring(1, temp.length() - 1);
			Files.write(file.toPath(), temp.getBytes(),
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void removeReceiver(RSAPublicKey address) {
		receivers.remove(address);
		updateFile(receiverFile, receivers);
	}

	void removeForeignReceiver(String address) {
		foreignReceivers.remove(address);
		updateFile(foreignReceiverFile, foreignReceivers);
	}

	void addForeignReceiver(String name, RSAPublicKey receiver,
			boolean active) {
		foreignReceivers.put(receiver, name);
		//TODO nicht nur temporär
		if (active)
			activeReceivers.add(receiver);
		updateFile(foreignReceiverFile, foreignReceivers);
	}

	boolean checkActiveReceiver(String receiver) {
		if (activeReceivers.contains(receiver))
			return true;
		return false;
	}
}
