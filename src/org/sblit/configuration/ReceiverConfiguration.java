package org.sblit.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReceiverConfiguration {

	protected static final String RECEIVER_PATH = "/receivers.txt";
	protected static final String FOREIGN_RECEIVER_PATH = "/freceivers.txt";
	private HashMap<String, String> receivers = new HashMap<String, String>();
	private File receiverFile;
	private File foreignReceiverFile;
	private HashMap<String, String> foreignReceivers = new HashMap<String, String>();
	private HashSet<String> activeReceivers = new HashSet<String>();

	public ReceiverConfiguration(String configurationDirectory) {
		receiverFile = new File(configurationDirectory + RECEIVER_PATH);
		if (receiverFile.exists()) {
			try {
				String[] receivers = new String(Files.readAllBytes(Paths.get(receiverFile
						.getAbsolutePath()))).split(",");
				for (String receiver : receivers) {
					String[] temp = receiver.split("=");
					this.receivers.put(temp[0], temp[1]);
				}
			} catch (Exception e) {
				System.out.println("Keine Receiver eingetragen!");
			}
		} else {
			try {
				receiverFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		foreignReceiverFile = new File(configurationDirectory + FOREIGN_RECEIVER_PATH);
		if (foreignReceiverFile.exists()) {
			try {
				String[] receivers = new String(Files.readAllBytes(Paths.get(foreignReceiverFile
						.getAbsolutePath()))).split(",");
				for (String receiver : receivers) {
					String[] temp = receiver.split("=");
					this.foreignReceivers.put(temp[0], temp[1]);
				}
			} catch (Exception e) {
				System.out.println("Keine fremden Receiver eingetragen!");
			}
		} else {
			try {
				foreignReceiverFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String, String> getReceivers() {
		return receivers;
	}

	public HashMap<String, String> getForeignReceivers() {
		return foreignReceivers;
	}

	void addReceiver(String name, String receiver) {
		receivers.put(receiver, name);
		
		updateFile(receiverFile, receivers);
	}

	private synchronized void updateFile(File file, Map<String, String> receivers) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					receiverFile)));
			String temp = receivers.toString();
			temp = temp.substring(1, temp.length() - 1);
			bw.write(temp);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void removeReceiver(String address) {
		receivers.remove(address);
		updateFile(receiverFile, receivers);
	}

	void removeForeignReceiver(String address) {
		foreignReceivers.remove(address);
		updateFile(foreignReceiverFile, foreignReceivers);
	}

	void addForeignReceiver(String name, String receiver, boolean active) {
		foreignReceivers.put(receiver, name);
		if(active)
			activeReceivers.add(receiver);
		updateFile(foreignReceiverFile, foreignReceivers);
	}
	
	boolean checkActiveReceiver(String receiver){
		if(activeReceivers.contains(receiver))
			return true;
		return false;
	}
}
