package org.sblit.filesync.responses;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
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
	public void send() throws BufException {
		byte[] data = new String(PacketStarts.CONFLICT_RESPONSE.toString() + "," + originalFile + "," + accepted).getBytes();
		//TODO encrypt
		for(String receiver : Configuration.getReceivers())
			Configuration.getApp().send(data, Sblit.APPLICATION_IDENTIFIER, receiver);
	}
	
}
