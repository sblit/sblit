package org.sblit.filesync.requests;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;

import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
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
	public void send() throws BufException,IOException {
		for (Data receiver : Configuration.getChannels()) {
			byte[] modulus = new byte[257];
			byte[] exponent = new byte[3];
			for (int i = 0; i < modulus.length; i++)
				modulus[i] = receiver.getByte(i);
			for (int i = 0; i < exponent.length; i++)
				exponent[i] = receiver.getByte(i + modulus.length);
			try {
				RSAPublicKey pk = new RSAPublicKey(new BigInteger(modulus),
						new BigInteger(exponent));
				OutputStream out = Configuration.getChannel(receiver)
						.getOutputStream();
				out.write(new String(PacketStarts.AUTHENTICY_REQUEST.toString()
						+ "," + pk.encrypt(new Data(bytes))).getBytes());
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

}
