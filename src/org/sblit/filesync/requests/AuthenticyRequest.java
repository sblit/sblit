package org.sblit.filesync.requests;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.challenge.Fixed128ByteCryptoChallenge;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class AuthenticyRequest implements Packet {

	public static HashSet<AuthenticyRequest> requests = new HashSet<>();
	private Fixed128ByteCryptoChallenge challenge;
	private ApplicationChannel applicationChannel;

	public AuthenticyRequest(ApplicationChannel applicationChannel) {
		requests.add(this);
		this.applicationChannel = applicationChannel;
		challenge = new Fixed128ByteCryptoChallenge(applicationChannel.getRemotePublicKey());
	}

	public Data getReceiver() {
		return applicationChannel.getRemotePublicKey().toData();
	}

	@Override
	public void send() throws BufException, IOException {
		BufferedOutputStream out = applicationChannel
				.getOutputStream();
		out.write("Hallo Welt\n".getBytes());
		out.flush();
//		Data data = challenge.makeChallengeData();
//		try {
//			System.out.println("schreiben...");
//			BufferedOutputStream out = applicationChannel
//					.getOutputStream();
//			PrintWriter pw = new PrintWriter(out);
//			pw.println(new String(PacketStarts.AUTHENTICY_REQUEST.toString()
//					+ "," + new String(data.getData()) + "\n").getBytes());
//			pw.flush();
//			System.out.println("gesendet: " + new String(PacketStarts.AUTHENTICY_REQUEST.toString()
//					+ "," + new String(data.getData())).getBytes());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public boolean check(Data encrypted) throws CryptoException {
		return challenge.verifySolvedData(encrypted);
	}
}
