package net.sblit.filesync;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

import net.sblit.configuration.Configuration;
import net.sblit.directoryWatcher.DirectoryWatcher;
import net.sblit.message.AuthenticityRequestMessage;
import net.sblit.message.AuthenticityResponseMessage;
import net.sblit.message.FileRequestMessage;
import net.sblit.message.FileResponseMessage;
import net.sblit.message.SblitMessage;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.challenge.Fixed128ByteCryptoChallenge;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.packetcomponent.OnReceive;

public class ApplicationChannelActionListener implements
		org.dclayer.application.applicationchannel.ApplicationChannelActionListener {

	Receiver receiver;
	protected ApplicationChannel applicationChannel;
	protected SblitMessage message;
	{
		message.loadOnReceiveObject(this);
	}
	private Fixed128ByteCryptoChallenge challenge;

	public ApplicationChannelActionListener(Receiver receiver) {
		this.receiver = receiver;
	}

	StreamByteBuf streamByteBuf;

	@Override
	public void onApplicationChannelDisconnected(ApplicationChannel applicationChannel) {
		System.out
				.println("Disconnected: "
						+ Configuration.getReceiversAndNames().get(
								applicationChannel.getRemotePublicKey()));
		// try {
		// applicationChannel.getInputStream().close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// try {
		// applicationChannel.getOutputStream().close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		Configuration.removeChannel(applicationChannel.getRemotePublicKey().toData());

	}

	@Override
	public void onApplicationChannelConnected(ApplicationChannel applicationChannel) {

		this.applicationChannel = applicationChannel;

		streamByteBuf = new StreamByteBuf(applicationChannel.getOutputStream());

		Runnable r = new Runnable() {

			@Override
			public void run() {
				SblitMessage message = new SblitMessage();
				InputStream in = ApplicationChannelActionListener.this.applicationChannel
						.getInputStream();
				while (true) {
					try {
						message.read(new StreamByteBuf(in));
						message.callOnReceive();
					} catch (ParseException | BufException e) {
						e.printStackTrace();
					}
				}
			}

		};
		new Thread(r).start();
		message.set(SblitMessage.AUTHENTICITY_REQUEST);
		challenge = new Fixed128ByteCryptoChallenge(applicationChannel.getRemotePublicKey());
		message.authenticityRequest.dataComponent.setData(challenge.makeChallengeData());
		try {
			message.write(streamByteBuf);
		} catch (BufException e) {
			e.printStackTrace();
			Configuration.denyChannel(applicationChannel.getRemotePublicKey().toData());
		}
	}

	@OnReceive(index = SblitMessage.AUTHENTICITY_REQUEST)
	public synchronized void handleAuthenticyRequest(AuthenticityRequestMessage authenticityRequest) {
		Fixed128ByteCryptoChallenge challenge = new Fixed128ByteCryptoChallenge(
				Configuration.getPrivateAddressKey());
		try {
			message.set(SblitMessage.AUTHENTICITY_RESPONSE);

			message.authenticityResponse.dataComponent.setData(challenge
					.solveChallengeData(authenticityRequest.dataComponent.getData()));

			message.write(streamByteBuf);
		} catch (CryptoException | BufException e1) {
			e1.printStackTrace();
		}
	}

	private synchronized void sendFileRequest(String path, LinkedList<byte[]> hashes) {
		message.set(SblitMessage.FILE_REQUEST);

		LinkedList<DataComponent> data = new LinkedList<>();
		DataComponent dataComponent = new DataComponent();

		for (byte[] hash : hashes) {
			dataComponent.setData(new Data(hash));
			data.add(dataComponent);
		}

		message.fileRequest.path.setString(path);
		message.fileRequest.hashes.setElements(data);

		try {
			message.write(streamByteBuf);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

	@OnReceive(index = SblitMessage.AUTHENTICITY_RESPONSE)
	public void handleAuthenticityResponse(AuthenticityResponseMessage authenticityResponse) {
		try {
			if (challenge.verifySolvedData(authenticityResponse.dataComponent.getData())) {
				Configuration.allowChannel(this.applicationChannel.getRemotePublicKey().toData());
				try {
					HashMap<String, LinkedList<byte[]>> logs = DirectoryWatcher.getLogs();
					for (String path : logs.keySet()) {
						LinkedList<byte[]> hashes = logs.get(path);

						if (hashes.size() > 1) {
							sendFileRequest(path, hashes);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (CryptoException e) {
			e.printStackTrace();
			Configuration.denyChannel(applicationChannel.getRemotePublicKey().toData());
		}
	}

	private synchronized void sendFileResponse(String path, Data need, Data newHash)
			throws BufException {
		message.set(SblitMessage.FILE_RESPONSE);
		message.fileResponse.path.setString(path);
		message.fileResponse.need.setData(need);
		message.fileResponse.newHash.setData(newHash);
		message.write(streamByteBuf);
	}

	@OnReceive(index = SblitMessage.FILE_REQUEST)
	public void handleFileRequest(FileRequestMessage fileRequest) throws IOException {
		LinkedList<byte[]> requestedHashes = new LinkedList<>();
		for (DataComponent dataComponent : fileRequest.hashes)
			requestedHashes.add(dataComponent.getData().getData());

		LinkedList<byte[]> ownHashes = DirectoryWatcher.getLogs().get(fileRequest.path.getString());

		Data need;
		if (ownHashes.get(ownHashes.size() - 1).equals(
				requestedHashes.get(requestedHashes.size() - 1))) {
			need = new Data(new byte[] { 0x00 });
		} else {
			need = new Data(new byte[] { 0x01 });
			if (!requestedHashes.contains(ownHashes.get(ownHashes.size() - 1))) {
				// TODO handle conflicts
			}
		}
		Data newHash = new Data(requestedHashes.get(requestedHashes.size() - 1));
		try {
			sendFileResponse(fileRequest.path.getString(), need, newHash);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendFile(LinkedList<byte[]> synchronizedDevices, Data fileContent,
			String filePath, Data hash) {
		message.set(SblitMessage.FILE_MESSAGE);
		LinkedList<DataComponent> devices = new LinkedList<>();
		DataComponent dataComponent = new DataComponent();
		for (byte[] device : synchronizedDevices) {
			dataComponent.setData(new Data(device));
			devices.add(dataComponent);
		}
		message.fileMessage.synchronizedDevices.setElements(devices);
		message.fileMessage.fileContent.setData(fileContent);
		message.fileMessage.filePath.setString(filePath);
		message.fileMessage.hash.setData(hash);
		try {
			message.write(streamByteBuf);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

	@OnReceive(index = SblitMessage.FILE_RESPONSE)
	public void handleFileResponse(FileResponseMessage fileResponse) throws IOException {
		if (fileResponse.need.getData().getData()[0] == 0x01) {
			LinkedList<byte[]> log = DirectoryWatcher.getLogs().get(fileResponse.path.getString());
			sendFile(DirectoryWatcher.getSynchronizedDevices().get(fileResponse.path.getString()),
					new Data(Files.readAllBytes(Paths.get(fileResponse.path.getString()))),
					fileResponse.path.getString(), new Data(log.get(log.size() - 1)));
		}
	}
}
