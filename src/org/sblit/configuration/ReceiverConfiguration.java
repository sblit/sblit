package org.sblit.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReceiverConfiguration {

	protected static final String RECEIVER_PATH = "receivers.txt";
	private String[] receivers = null;
	private File receiverFile;

	public ReceiverConfiguration(String configurationDirectory) {
		receiverFile = new File(configurationDirectory + RECEIVER_PATH);
		if (receiverFile.exists()) {
			try {
				receivers = new String(Files.readAllBytes(Paths
						.get(receiverFile.getAbsolutePath()))).split(",");
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

	public String[] getReceivers() {
		return receivers;
	}

	void addReceiver(String receiver) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(receiverFile)));
			String temp = "";
			for (String s : receivers)
				temp += s + ",";
			temp += receiver;
			bw.write(temp);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
