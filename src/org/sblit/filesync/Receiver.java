package org.sblit.filesync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dclayer.application.NetworkEndpointActionListener;
import org.dclayer.application.networktypeslotmap.NetworkEndpointSlot;
import org.dclayer.crypto.key.Key;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.crypto.InvalidCipherCryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.llacache.LLA;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.converter.Converter;
import org.sblit.directoryWatcher.DirectoryWatcher;
import org.sblit.fileProcessing.FileWriter;
import org.sblit.filesync.exceptions.TimestampException;
import org.sblit.filesync.requests.AuthenticyRequest;
import org.sblit.filesync.requests.ConflictRequest;
import org.sblit.filesync.requests.FileRequest;
import org.sblit.filesync.responses.AuthenticyReponse;
import org.sblit.filesync.responses.ConflictResponse;
import org.sblit.filesync.responses.FileResponse;

/**
 * 
 * @author Nikola
 * 
 */

public class Receiver implements NetworkEndpointActionListener {

	public Receiver() {

	}

	void handleAuthenticyResponse(byte[] received, Data sourceAddressData) {
		for (AuthenticyRequest request : AuthenticyRequest.requests)
			try {
				if (request.getReceiver().equals(sourceAddressData)
						&& !request.check(new Data(received))) {
					Configuration.denyChannel(sourceAddressData);
				} else if (request.getReceiver().equals(sourceAddressData)
						&& request.check(new Data(received))) {
					Configuration.allowChannel(sourceAddressData);
					try {
						for (String s : DirectoryWatcher.getLogFileContent(
								new File(Configuration.getConfigurationDirectory()
										.getAbsolutePath() + Configuration.LOG_FILE)).values()) {
							byte[] hashcode = s.split(";")[1].getBytes();
							new FileRequest(hashcode).send();
						}
					} catch (IOException | BufException e) {
						e.printStackTrace();
					}
				}
			} catch (CryptoException e) {
				Configuration.denyChannel(sourceAddressData);
			}
	}

	void handleAuthenticyRequest(byte[] received, Data sender) throws InvalidCipherCryptoException {
		byte[] decrypted = Configuration.getPrivateAddressKey().decrypt(new Data(received))
				.getData();
		try {
			new AuthenticyReponse(decrypted, sender).send();
		} catch (BufException | IOException e) {
			e.printStackTrace();
		}
	}

	void handleFileResponse(byte[] received, Data sourceAddressData) throws IOException {
		String data = new String(received);
		if (Boolean.parseBoolean(data.split(",")[1])) {
			try {
				new FileSender(sourceAddressData).sendOwnFiles(new File[] {}, new File(
						Configuration.getConfigurationDirectory() + Configuration.LOG_FILE));
			} catch (BufException e) {
				e.printStackTrace();
			}
		}
	}

	void handleFileRequest(byte[] received, Data sourceAddressData) throws IOException {
		String data = new String(received);
		byte[] otherHashcode = data.split(",")[1].getBytes();
		boolean need;
		if (new String(Files.readAllBytes(Paths.get(Configuration.getConfigurationDirectory()
				+ Configuration.LOG_FILE))).contains(new String(otherHashcode))) {
			need = false;
		} else {
			need = true;
		}
		try {
			new FileResponse(need, otherHashcode, sourceAddressData).send();
		} catch (BufException e) {
			e.printStackTrace();
		}

	}

	void handleConflictResponse(byte[] received) throws IOException {
		String data = new String(received);
		for (FileWriter fileWriter : FileWriter.fileWriters) {
			if (fileWriter.getFile().equals(data.split(",")[1])) {
				fileWriter.conflict(Boolean.parseBoolean(data.split(",")[2]));
				FileWriter.fileWriters.remove(fileWriter);
			}
		}
	}

	void handleConflictRequest(byte[] received, Data sourceAddressData) throws BufException,
			IOException {
		// boolean requestExists = false;

		for (ConflictRequest request : ConflictRequest.requests) {
			String data = new String(received);
			try {
				if (request.getOriginalFile().equals(data.split(",")[1])) {
					boolean accepted;
					if (request.getTimestamp() > Integer.parseInt(data.split(",")[3])) {
						// Other File wins
						accepted = true;
					} else if (request.getTimestamp() == Integer.parseInt(data.split(",")[3])) {
						TimestampException e = new TimestampException();
						throw e;
					} else {
						// This File wins
						accepted = false;
					}
					ConflictResponse response = new ConflictResponse(accepted,
							request.getOriginalFile(), sourceAddressData);
					response.send();

					for (FileWriter fileWriter : FileWriter.fileWriters) {
						if (fileWriter.getFile().equals(data.split(",")[1])) {
							fileWriter.conflict(!accepted);
							FileWriter.fileWriters.remove(fileWriter);
						}
					}

					ConflictRequest.requests.remove(request);
				}
			} catch (TimestampException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				new ConflictRequest(request.getOriginalFile(), request.getNewFile()).send();
			}
		}
	}

	@Override
	public void onJoin(NetworkEndpointSlot networkEndpointSlot, Data ownAddressData) {
		System.out.println(String.format("joined: %s, local address: %s", networkEndpointSlot,
				ownAddressData));
		for (Data partner : Configuration.getReceivers()) {
			System.out.println(new String(partner.getData()));
			Configuration.getApp().requestApplicationChannel(networkEndpointSlot,
					Sblit.APPLICATION_IDENTIFIER, Converter.dataToKey(partner),
					new ApplicationChannelActionListener(this));
		}

	}

	@Override
	public void onReceive(NetworkEndpointSlot networkEndpointSlot, Data data, Data sourceAddressData) {
		// TODO auto-generated method stuff
	}

	@Override
	public ApplicationChannelActionListener onApplicationChannelRequest(
			NetworkEndpointSlot networkEndpointSlot, Key remotePublicKey, String actionIdentifier,
			LLA remoteLLA) {
		if (Configuration.getReceiversAndNames().containsKey(remotePublicKey.toData())) {
			return new ApplicationChannelActionListener(this);
		} else {
			return null;
		}
	}
}
