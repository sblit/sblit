package org.sblit.filesync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.dclayer.application.ApplicationInstance;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;

/**
 * 
 * @author Nikola
 * 
 */
public class FileSender {

	private String ownAddress;
	//private ApplicationInstance app;
	private byte[] password;
	private Data receiver;

	/**
	 * With this FileSender you are able to send files to multiple receivers.
	 * 
	 * @param password
	 *            Used for encryption of the own files to send. If you just want
	 *            to send foreign files this can be null.
	 * @param app
	 *            Application identifier of the app.
	 * @param ownAddress
	 *            Own address for identification in the cloud.
	 */
	@Deprecated
	public FileSender(byte[] password, ApplicationInstance app,
			String ownAddress) {
		//this.app = app;
		this.password = password;
		this.ownAddress = ownAddress;
	}

	public FileSender(Data sourceAddressData) {
		//app = Configuration.getApp();
		password = Configuration.getKey();
		ownAddress = Configuration.getPublicAddressKey().toString();
		receiver = sourceAddressData;
	}
	
	private synchronized byte[] getFileContent(Path path) throws IOException{
		return Files.readAllBytes(path);
	}

	/**
	 * Sends own files to multiple receivers.
	 * 
	 * @param receivers
	 *            Receivers for the files.
	 * @param filesToPush
	 *            Files to send to the receivers.
	 * @param logFile
	 *            File which contains the versions.
	 * @throws IOException
	 *             If a problem occurs while reading at least one of the files.
	 * @throws BufException
	 *             If a problem while sending data occurs
	 */
	public void sendOwnFiles(File[] filesToPush, File logFile)
			throws IOException, BufException {
		for (File f : filesToPush) {
			byte[] ownAddress = this.ownAddress.getBytes();
			byte[] logFileContent = getFileContent(Paths.get(logFile
					.getAbsolutePath()));
			byte[] fileContent = getFileContent(Paths.get(f
					.getAbsolutePath()));
			byte[] filePath = f.getAbsolutePath()
					.replaceAll(Configuration.getSblitDirectory().getAbsolutePath(), "").getBytes();
			byte[] hash;
			try {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(fileContent);
				hash = md.digest();

				SymmetricEncryption encryption = new SymmetricEncryption(
						password);
				byte[] encryptedLogFileContent = encryption
						.encrypt(logFileContent);
				byte[] encryptedFileContent = encryption.encrypt(fileContent);
				byte[] encryptedFilePath = encryption.encrypt(filePath);

				byte[] data = new byte[ownAddress.length + hash.length
						+ encryptedLogFileContent.length
						+ encryptedFileContent.length
						+ encryptedFilePath.length + 4];
				for (int i = 0; i < ownAddress.length; i++)
					data[i] = ownAddress[i];
				data[ownAddress.length] = (byte) '\0';
				for (int i = 0; i < hash.length; i++)
					data[i + 1 + ownAddress.length] = hash[i];
				data[ownAddress.length + hash.length + 1] = (byte) '\0';
				for (int i = 0; i < encryptedLogFileContent.length; i++)
					data[i + 2 + ownAddress.length + hash.length] = encryptedLogFileContent[i];
				data[ownAddress.length + hash.length + 2
						+ encryptedLogFileContent.length] = (byte) '\0';
				for (int i = 0; i < encryptedFileContent.length; i++)
					data[i + 3 + ownAddress.length + hash.length
							+ encryptedLogFileContent.length] = encryptedFileContent[i];
				data[ownAddress.length + hash.length
						+ encryptedLogFileContent.length
						+ encryptedFileContent.length + 3] = (byte) '\0';
				for (int i = 0; i < encryptedFilePath.length; i++)
					data[i + 4 + ownAddress.length + hash.length
							+ encryptedLogFileContent.length
							+ encryptedFileContent.length] = encryptedFilePath[i];

				send(data);
			} catch (NoSuchAlgorithmException e) {
				//hash = "".getBytes();
				e.printStackTrace();
			}

		}

	}

	/**
	 * Sends foreign files to multiple receivers.
	 * 
	 * @param receivers
	 *            Receivers for the files.
	 * @param filesToPush
	 *            Files to send.
	 * @throws IOException
	 *             If the file is corrupt.
	 * @throws BufException
	 *             If sending the file fails.
	 */
	public void sendForeignFiles(File[] filesToPush) throws IOException,
			BufException {
		for (File f : filesToPush) {
			byte[] data = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			send(data);
		}
	}

	/**
	 * The actual sending process.
	 * 
	 * @param data
	 *            The data to send.
	 * @param receivers
	 *            Receivers for the file.
	 * @throws BufException
	 *             If sending fails
	 */
	private void send(byte[] data) throws IOException {
		System.out.println(receiver);
		OutputStream out = Configuration.getChannel(receiver).getOutputStream();
		out.write(data);
		out.flush();
		// app.send(data, Sblit.APPLICATION_IDENTIFIER, receiver);
		// try {
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(data);
		// app.send(md.digest(), Sblit.APPLICATION_IDENTIFIER, receiver);
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// }

	}

}
