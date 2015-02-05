package net.sblit.filesync.requests;

import java.io.IOException;
import java.util.HashSet;

import net.sblit.configuration.Configuration;
import net.sblit.crypto.SymmetricEncryption;
import net.sblit.filesync.Packet;
import net.sblit.filesync.PacketStarts;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;

public class FileRequest implements Packet {

	byte[] hashcode;
	public static HashSet<FileRequest> fileRequests = new HashSet<>();

	public FileRequest(byte[] hashcode) {
		fileRequests.add(this);
		this.hashcode = hashcode;
	}

	@Override
	public synchronized void send() throws BufException, IOException {
		byte[] data = new String(PacketStarts.FILE_REQUEST.toString() + ","
				+ hashcode).getBytes();
		Data encryptedData = new Data(new SymmetricEncryption(Configuration.getKey())
				.encrypt(data));
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(encryptedData);
			
		for (Data receiver : Configuration.getChannels()) {
			System.out.println(String.format("sending file request: %s", dataComponent.represent(true)));
			System.out.println("decrypted: " + new SymmetricEncryption(Configuration.getKey()).decrypt(data));
			StreamByteBuf streamByteBuf = new StreamByteBuf(Configuration.getChannel(receiver).getOutputStream());
			
			try {
				streamByteBuf.write(dataComponent);
			} catch (BufException e) {
				e.printStackTrace();
			}
			Configuration.getChannel(receiver).getOutputStream().flush();
		}
	}

}
