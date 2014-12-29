package org.sblit.filesync.responses;

import java.io.IOException;
import java.io.OutputStream;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.configuration.Configuration;
import org.sblit.filesync.Packet;

public class AuthenticyReponse implements Packet{
	
	byte[] data;
	Data receiver;
	
	public AuthenticyReponse(byte[] decryptedData, Data receiver) {
		this.data = decryptedData;
		this.receiver = receiver;
	}

	@Override
	public void send() throws BufException, IOException {
		OutputStream out = Configuration.getChannel(receiver)
				.getOutputStream();
		out.write(data);
		out.flush();
	}

}
