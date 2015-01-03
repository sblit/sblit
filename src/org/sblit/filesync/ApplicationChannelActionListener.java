package org.sblit.filesync;

import java.io.BufferedInputStream;
import java.io.IOException;

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
		new AuthenticyRequest(applicationChannel.getRemotePublicKey().toData());
		while (true) {
			try {
				// TODO handle foreign files

				BufferedInputStream stream = new BufferedInputStream(
						applicationChannel.getInputStream());

				byte[] received = new byte[stream.available()];
				stream.read(received);
				Data sourceAddressData = applicationChannel.getRemotePublicKey().toData();
				String s = new String(received);
				if (s.startsWith(PacketStarts.AUTHENTICY_REQUEST.toString())) {
					receiver.handleAuthenticyRequest(received, sourceAddressData);
				} else if (s.startsWith(PacketStarts.AUTHENTICY_RESPONSE.toString())) {
					receiver.handleAuthenticyResponse(received, sourceAddressData);
				}
				received = new SymmetricEncryption(Configuration.getKey()).decrypt(received);
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
			} catch (BufException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidCipherCryptoException e) {
				e.printStackTrace();
			}
		}

	}

}
