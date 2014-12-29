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

	public FileResponse(boolean need, byte[] hashcode) {
		this.need = need;
		this.hashcode = hashcode;
	}

	@Override
	public void send() throws BufException, IOException {
		byte[] message = new String(hashcode + "," + need).getBytes();
		byte[] encryptedMessage = new SymmetricEncryption(
				Configuration.getKey()).encrypt(message);
		for (Data receiver : Configuration.getChannels()) {
			OutputStream out = Configuration.getChannel(receiver)
					.getOutputStream();
			out.write(encryptedMessage);
			out.flush();

		}
	}
}
