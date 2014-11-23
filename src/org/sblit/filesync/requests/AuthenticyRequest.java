package org.sblit.filesync.requests;

import java.util.HashSet;
import java.util.Random;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class AuthenticyRequest implements Packet {

	public static HashSet<AuthenticyRequest> requests = new HashSet<>();
	private byte[] bytes;

	public AuthenticyRequest() {
		requests.add(this);
		bytes = new byte[512];
		new Random().nextBytes(bytes);

	}

	@Override
	public void send() throws BufException {
		for (String receiver : Configuration.getReceivers())
			Configuration.getApp().send(
					new String(PacketStarts.AUTHENTICY_REQUEST.toString() + ","
							+ bytes).getBytes(), Sblit.APPLICATION_IDENTIFIER,
					receiver);
	}
	
	public byte[] getBytes(){
		return bytes;
	}
}
