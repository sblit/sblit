package org.sblit.filesync.requests;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;

import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.converter.Converter;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class AuthenticyRequest implements Packet {

	public static HashSet<AuthenticyRequest> requests = new HashSet<>();
	private byte[] bytes;
	private Data receiver;

	public AuthenticyRequest(Data receiver) {
		requests.add(this);
		bytes = new byte[512];
		new Random().nextBytes(bytes);
	}

	public Data getReceiver() {
		return receiver;
	}

	@Override
	public void send() throws BufException, IOException {
		try {
			RSAPublicKey pk = Converter.dataToKey(receiver);
			OutputStream out = Configuration.getChannel(receiver)
					.getOutputStream();
			out.write(new String(PacketStarts.AUTHENTICY_REQUEST.toString()
					+ "," + pk.encrypt(new Data(bytes))).getBytes());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

}
