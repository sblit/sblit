package org.sblit.filesync.responses;

import java.io.IOException;
import java.io.OutputStream;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.filesync.Packet;

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
	public void send() throws BufException, IOException {
		byte[] message = new String(hashcode + "," + need).getBytes();
		byte[] encryptedMessage = new SymmetricEncryption(
				Configuration.getKey()).encrypt(message);
		OutputStream out = Configuration.getChannel(sourceAddress)
				.getOutputStream();
		out.write(encryptedMessage);
		out.flush();

	}
}
