package org.sblit.filesync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bouncycastle.crypto.DataLengthException;
import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.exception.crypto.InvalidCipherCryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.fileProcessing.FileProcessor;
import org.sblit.filesync.requests.AuthenticyRequest;

public class ApplicationChannelActionListener implements
		org.dclayer.application.applicationchannel.ApplicationChannelActionListener {

	Receiver receiver;

	public ApplicationChannelActionListener(Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void onApplicationChannelDisconnected(ApplicationChannel applicationChannel) {
		try {
			applicationChannel.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			applicationChannel.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Configuration.removeChannel(applicationChannel.getRemotePublicKey().toData());

	}

	@Override
	public void onApplicationChannelConnected(ApplicationChannel applicationChannel) {
		Configuration.addUnauthorizedChannel(applicationChannel.getRemotePublicKey().toData(),
				applicationChannel);
		System.out.println("Connected with \"" + applicationChannel.getRemotePublicKey() + "\"");
		try {
			new AuthenticyRequest(applicationChannel).send();
		} catch (BufException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(applicationChannel.getInputStream()));
		while (true) {
			try {
				// TODO handle foreign files
				//int i;
				System.out.println("lesen...");
				byte[] received ;
				String s = reader.readLine();//new String(received);
				System.out.println("Received: \"" + s + "\"");
				Data sourceAddressData = applicationChannel.getRemotePublicKey().toData();
				if (s.startsWith(PacketStarts.AUTHENTICY_REQUEST.toString())) {
					receiver.handleAuthenticyRequest(s.getBytes(), sourceAddressData);
				} else if (s.startsWith(PacketStarts.AUTHENTICY_RESPONSE.toString())) {
					receiver.handleAuthenticyResponse(s.getBytes(), sourceAddressData);
				}
				received = new SymmetricEncryption(Configuration.getKey()).decrypt(s.getBytes());
				if (Configuration.getChannels().contains(sourceAddressData)) {
					if (s.startsWith(PacketStarts.CONFLICT_REQUEST.toString())) {
						receiver.handleConflictRequest(received, sourceAddressData);
					} else if (s.startsWith(PacketStarts.CONFLICT_RESPONSE.toString())) {
						receiver.handleConflictResponse(received);
					} else if (s.startsWith(PacketStarts.FILE_REQUEST.toString())) {
						receiver.handleFileRequest(received, sourceAddressData);
					} else if (s.startsWith(PacketStarts.FILE_RESPONSE.toString())) {
						receiver.handleFileResponse(received, sourceAddressData);
					} else {
						// FileProcessor fileProcessor =
						new FileProcessor(received);
						// byte[] checksum = receive();
					}
				}
			} catch (BufException | DataLengthException e) {
				e.printStackTrace();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidCipherCryptoException e) {
				e.printStackTrace();
			}
		}

	}

}
