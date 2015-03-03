package net.sblit.filesync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

import net.sblit.configuration.Configuration;
import net.sblit.directoryWatcher.DirectoryWatcher;
import net.sblit.fileProcessing.FileWriter;
import net.sblit.message.AuthenticityRequestMessage;
import net.sblit.message.AuthenticityResponseMessage;
import net.sblit.message.FileMessage;
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

	protected ApplicationChannel applicationChannel;
	protected SblitMessage message = new SblitMessage(this);

	private Fixed128ByteCryptoChallenge challenge;

	public ApplicationChannelActionListener() {
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
		Configuration.addUnauthorizedChannel(applicationChannel.getRemotePublicKey().toData(), applicationChannel);

		streamByteBuf = new StreamByteBuf(applicationChannel.getOutputStream());

		Runnable r = new Runnable() {

			@Override
			public void run() {
				InputStream in = ApplicationChannelActionListener.this.applicationChannel
						.getInputStream();
				SblitMessage message = new SblitMessage(ApplicationChannelActionListener.this);
				while (true) {
					try {
						message.read(new StreamByteBuf(in));
						System.out.println("empfangen: " + message.toString());
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
			streamByteBuf.write(message);
			System.out.println("sending... " + message.authenticityRequest.dataComponent.toString());
		} catch (BufException e) {
			e.printStackTrace();
			Configuration.denyChannel(applicationChannel.getRemotePublicKey().toData());
		}
	}
	@OnReceive(index = SblitMessage.AUTHENTICITY_REQUEST)
	public synchronized void handleAuthenticyRequest(AuthenticityRequestMessage authenticityRequest) {
		System.out.println("In der OnReceive: \""
				+ authenticityRequest.dataComponent.getData().represent() + "\"");
		Fixed128ByteCryptoChallenge challenge = new Fixed128ByteCryptoChallenge(
				Configuration.getPrivateAddressKey());
		try {
			message.set(SblitMessage.AUTHENTICITY_RESPONSE);

			message.authenticityResponse.dataComponent.setData(challenge
					.solveChallengeData(authenticityRequest.dataComponent.getData()));

			streamByteBuf.write(message);
			System.out.println("response sent: " + message.authenticityResponse.dataComponent.getData().toString());
		} catch (CryptoException | BufException e1) {
			e1.printStackTrace();
		}
	}

	public synchronized void sendFileRequest(String path, LinkedList<Data> hashes) {
		message.set(SblitMessage.FILE_REQUEST);

		message.fileRequest.path.setString(path);
		message.fileRequest.hashes.setElements(hashes);

		try {
			streamByteBuf.write(message);
			applicationChannel.getOutputStream().flush();
		} catch (BufException | IOException e) {
			e.printStackTrace();
		}
	}

	@OnReceive(index = SblitMessage.AUTHENTICITY_RESPONSE)
	public void handleAuthenticityResponse(AuthenticityResponseMessage authenticityResponse) {
		System.out.println("received: " + authenticityResponse.toString());
		try {
			if (challenge.verifySolvedData(authenticityResponse.dataComponent.getData())) {
				Configuration.allowChannel(this.applicationChannel.getRemotePublicKey().toData());
				try {
					HashMap<String, LinkedList<Data>> logs = DirectoryWatcher.getLogs();
					for (String path : logs.keySet()) {
						LinkedList<Data> hashes = logs.get(path);

						if (hashes.size() > 1) {
							sendFileRequest(path, hashes);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Configuration.denyChannel(this.applicationChannel.getRemotePublicKey().toData());
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
		streamByteBuf.write(message);
	}

	@OnReceive(index = SblitMessage.FILE_REQUEST)
	public void handleFileRequest(FileRequestMessage fileRequest) throws IOException {
		LinkedList<Data> requestedHashes = new LinkedList<>();
		for (Data dataComponent : fileRequest.hashes)
			requestedHashes.add(dataComponent);

		LinkedList<Data> ownHashes = DirectoryWatcher.getLogs().get(fileRequest.path.getString());

		Data need;
		if (ownHashes.get(ownHashes.size() - 1).equals(
				requestedHashes.get(requestedHashes.size() - 1))) {
			need = new Data(new byte[] { 0x00 });
		} else {
			need = new Data(new byte[] { 0x01 });
			if (!requestedHashes.contains(ownHashes.get(ownHashes.size() - 1))) {
				String path = fileRequest.path.getString();
				int dotIndex = path.lastIndexOf(".");
				File conflictFile;
				if(dotIndex > path.lastIndexOf(Configuration.slash)){
					for(int i = 1;; i++){
						if(!new File(Configuration.getSblitDirectory() + path.substring(0,dotIndex-1) + "(Conflict " + i + ")" + path.substring(dotIndex)).exists()){
							conflictFile = new File(Configuration.getSblitDirectory() + path.substring(0,dotIndex-1) + "(Conflict " + i + ")" + path.substring(dotIndex));
							break;
						}
					}
				} else {
					for(int i = 1;; i++){
						if(!new File(Configuration.getSblitDirectory() + path + "(Conflict " + i + ")" ).exists()){
							conflictFile = new File(Configuration.getSblitDirectory() + path + "(Conflict " + i + ")" );
							break;
						}
					}
				}
				File file = new File(path);
				conflictFile.createNewFile();
				Files.copy(file.toPath(), conflictFile.toPath());
			}
		}
		Data newHash = requestedHashes.get(requestedHashes.size() - 1);
		try {
			sendFileResponse(fileRequest.path.getString(), need, newHash);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendFile(LinkedList<Data> synchronizedDevices, Data fileContent,
			String filePath, LinkedList<Data> hashes) {
		message.set(SblitMessage.FILE_MESSAGE);
		LinkedList<DataComponent> devices = new LinkedList<>();
		DataComponent dataComponent = new DataComponent();
		for (Data device : synchronizedDevices) {
			dataComponent.setData(device);
			devices.add(dataComponent);
		}
		message.fileMessage.synchronizedDevices.setElements(devices);
		message.fileMessage.fileContent.setData(fileContent);
		message.fileMessage.filePath.setString(filePath);
		message.fileMessage.hashes.setElements(hashes);
		try {
			streamByteBuf.write(message);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

	@OnReceive(index = SblitMessage.FILE_RESPONSE)
	public void handleFileResponse(FileResponseMessage fileResponse) throws IOException {
		if (fileResponse.need.getData().getData()[0] == 0x01) {
			LinkedList<Data> log = DirectoryWatcher.getLogs().get(fileResponse.path.getString());
			LinkedList<Data> logs = new LinkedList<>();
			for(Data temp: log)
				logs.add(temp);
			sendFile(DirectoryWatcher.getSynchronizedDevices().get(fileResponse.path.getString()),
					new Data(Files.readAllBytes(Paths.get(fileResponse.path.getString()))),
					fileResponse.path.getString(), logs);
		}
	}
	
	@OnReceive(index = SblitMessage.FILE_MESSAGE) 
	public void handleFileMessage(FileMessage fileMessage){
		Data[] hashes = (Data[]) fileMessage.hashes.getChildren();
		new FileWriter(hashes, fileMessage.fileContent.getData(), fileMessage.filePath.getString(), (Data[])fileMessage.synchronizedDevices.getChildren());
	}
	
}
