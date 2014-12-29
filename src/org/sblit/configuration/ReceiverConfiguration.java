package org.sblit.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ReceiverConfiguration {

	protected static final String RECEIVER_PATH = "receivers.txt";
	private HashMap<String, String> receivers = new HashMap<String,String>();
	private File receiverFile;

	public ReceiverConfiguration(String configurationDirectory) {
		receiverFile = new File(configurationDirectory + RECEIVER_PATH);
		if (receiverFile.exists()) {
			try {
				String[] receivers = new String(Files.readAllBytes(Paths
						.get(receiverFile.getAbsolutePath()))).split(",");
				for(String receiver : receivers){
					String[] temp = receiver.split("=");
					this.receivers.put(temp[0], temp[1]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			try {
				receiverFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String,String> getReceivers() {
		return receivers;
	}

	void addReceiver(String name, String receiver) {
		try {
			receivers.put(name,receiver);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(receiverFile)));
			String temp = receivers.toString();
			temp = temp.substring(1,temp.length()-1);
			bw.write(temp);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
