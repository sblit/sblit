package net.sblit.fileProcessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sblit.configuration.Configuration;
import net.sblit.crypto.SymmetricEncryption;

public class FileProcessor {

	private byte[] password;
	private String[] myDevices;
	private File configurationDirectory;
	@Deprecated
	public FileProcessor(byte[] received) {
		configurationDirectory = Configuration.getConfigurationDirectory();
		String sender = new String(received).split('\0' + "")[0];
		boolean ownFile = false;
		for (int i = 0; i < myDevices.length; i++) {
			if (myDevices[i].equals(sender)) {
				ownFile = true;
			}
		}
		if (ownFile) {
			String s = new String(received);
			int logFileStart = s.indexOf('\0' + "", 6);
			int fileStart = s.indexOf('\0' + "", logFileStart + 1);
			int pathStart = s.indexOf('\0' + "", fileStart + 1);

			byte[] encryptedLogFile = new byte[fileStart - logFileStart];
			byte[] encryptedFile = new byte[pathStart - fileStart];
			byte[] encryptedFilePath = new byte[received.length - pathStart];

			for (int i = 0; i < encryptedLogFile.length; i++)
				encryptedLogFile[i] = received[logFileStart + 1 + i];

			for (int i = 0; i < encryptedFile.length; i++)
				encryptedFile[i] = received[fileStart + 1 + i];

			for (int i = 0; i < encryptedFilePath.length; i++)
				encryptedFilePath[i] = received[pathStart + 1 + i];
			
			SymmetricEncryption encryption = new SymmetricEncryption(password);

			byte[] logFile = encryption.decrypt(encryptedLogFile);
			byte[] file = encryption.decrypt(encryptedFile);
			byte[] filePath = encryption.decrypt(encryptedFilePath);

			new FileWriter(logFile, file, filePath);

		} else {
			File directory = new File(configurationDirectory.getAbsolutePath()
					+ Configuration.FOREIGN_FILES);

			String timestamp = new String(received).split('\0' + "")[1];
			try {
				FileOutputStream fos = new FileOutputStream(new File(directory
						+ timestamp + ".sblit"));
				fos.write(received);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

//	public boolean check(byte[] checksum) {
//		return received.hashCode() == Converter.byteArrayToInt(checksum);
//
//	}
}
