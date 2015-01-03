package org.sblit.filesync.responses;

import java.io.IOException;
import java.io.OutputStream;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

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
	public void send() throws BufException, IOException {
		byte[] data = new String(PacketStarts.CONFLICT_RESPONSE.toString()
				+ "," + originalFile + "," + accepted).getBytes();
		byte[] encryptedData = new SymmetricEncryption(Configuration.getKey())
				.encrypt(data);
		OutputStream out = Configuration.getChannel(sourceAddress)
				.getOutputStream();
		out.write(encryptedData);
	}

}
