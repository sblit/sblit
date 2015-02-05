package net.sblit.filesync.responses;

import java.io.IOException;

import net.sblit.configuration.Configuration;
import net.sblit.crypto.SymmetricEncryption;
import net.sblit.filesync.Packet;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;

public class FileResponse implements Packet {

	boolean need;
	byte[] hashcode;
	Data sourceAddress;

	public FileResponse(boolean need, byte[] hashcode, Data sourceAddressData) {
		this.need = need;
		this.hashcode = hashcode;
		sourceAddress = sourceAddressData;
	}

	@Override
	public synchronized void send() throws BufException, IOException {
		byte[] message = new String(hashcode + "," + need).getBytes();
		Data encryptedMessage = new Data(new SymmetricEncryption(
				Configuration.getKey()).encrypt(message));
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(encryptedMessage);
		
		System.out.println(String.format("sending file response: %s\ndata: %s", dataComponent.represent(true), new String(dataComponent.getData().getData())));
		
		StreamByteBuf streamByteBuf = new StreamByteBuf(Configuration.getChannel(sourceAddress).getOutputStream());
		
		try {
			streamByteBuf.write(dataComponent);
		} catch (BufException e) {
			e.printStackTrace();
		}

	}
}
