package org.sblit.filesync.responses;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.filesync.Packet;

public class FileResponse implements Packet{
	
	boolean need;
	byte[] hashcode;
	
	public FileResponse(boolean need, byte[] hashcode) {
		this.need = need;
		this.hashcode = hashcode;
	}

	@Override
	public void send() throws BufException {
		byte[] message = new String(hashcode + "," + need).getBytes();
		byte[] encryptedMessage = new SymmetricEncryption(Configuration.getKey()).encrypt(message);
		for ( String receiver:Configuration.getReceivers())
			Configuration.getApp().send(encryptedMessage, Sblit.APPLICATION_IDENTIFIER, receiver);
	}
}
