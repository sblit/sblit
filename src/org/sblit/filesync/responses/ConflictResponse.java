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
	
	public ConflictResponse(boolean accepted, String originalFile) {
		this.accepted = accepted;
		this.originalFile = originalFile;
	}

	@Override
	public void send() throws BufException, IOException {
		byte[] data = new String(PacketStarts.CONFLICT_RESPONSE.toString() + "," + originalFile + "," + accepted).getBytes();
		byte[] encryptedData = new SymmetricEncryption(Configuration.getKey()).encrypt(data);
		for(Data receiver : Configuration.getReceivers()){
			OutputStream out = Configuration.getChannel(receiver)
					.getOutputStream();
			out.write(encryptedData);
		}
	}
	
}
