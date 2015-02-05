package net.sblit.filesync.responses;

import java.io.IOException;

import net.sblit.configuration.Configuration;
import net.sblit.crypto.SymmetricEncryption;
import net.sblit.filesync.Packet;
import net.sblit.filesync.PacketStarts;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;

public class ConflictResponse implements Packet {

	private boolean accepted;
	private String originalFile;
	Data sourceAddress;

	public ConflictResponse(boolean accepted, String originalFile,
			Data sourceAddressData) {
		this.accepted = accepted;
		this.originalFile = originalFile;
		sourceAddress = sourceAddressData;
	}

	@Override
	public synchronized void send() throws BufException, IOException {
		byte[] data = new String(PacketStarts.CONFLICT_RESPONSE.toString()
				+ "," + originalFile + "," + accepted).getBytes();
		Data encryptedData = new Data(new SymmetricEncryption(Configuration.getKey())
				.encrypt(data));
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(encryptedData);
		
		System.out.println(String.format("sending: %s", dataComponent.represent(true)));
		
		StreamByteBuf streamByteBuf = new StreamByteBuf(Configuration.getChannel(sourceAddress).getOutputStream());
		
		try {
			streamByteBuf.write(dataComponent);
		} catch (BufException e) {
			e.printStackTrace();
		}
		Configuration.getChannel(sourceAddress).getOutputStream().flush();
	}

}
