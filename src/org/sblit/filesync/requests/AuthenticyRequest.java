package org.sblit.filesync.requests;

import java.io.IOException;
import java.util.HashSet;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.challenge.Fixed128ByteCryptoChallenge;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
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
	public synchronized void send() throws BufException, IOException {
		Data data = new Data((PacketStarts.AUTHENTICY_REQUEST.toString() + "," + new String(challenge.makeChallengeData().getData())).getBytes());
		
		//data = new Data("Hello World".getBytes());
		
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(data);
		
		System.out.println(String.format("sending authenticy request: %s\ndata: %s", dataComponent.represent(true), new String(dataComponent.getData().getData())));
		
		StreamByteBuf streamByteBuf = new StreamByteBuf(applicationChannel.getOutputStream());
		try {
			streamByteBuf.write(dataComponent);
		} catch (BufException e) {
			e.printStackTrace();
		}
		applicationChannel.getOutputStream().flush();
	}
//	public void send() throws BufException, IOException {
//		BufferedOutputStream out = applicationChannel
//				.getOutputStream();
//		PrintWriter pw = new PrintWriter(out);
//		out.write("Hello World");
//		out.flush();
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
//	}

	public boolean check(Data encrypted) throws CryptoException {
		return challenge.verifySolvedData(encrypted);
	}
}
