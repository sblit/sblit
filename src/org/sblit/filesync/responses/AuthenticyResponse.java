package org.sblit.filesync.responses;

import java.io.IOException;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class AuthenticyResponse implements Packet{
	
	byte[] data;
	ApplicationChannel applicationChannel;
	
	public AuthenticyResponse(byte[] decryptedData, ApplicationChannel applicationChannel) {
		this.data = decryptedData;
		this.applicationChannel = applicationChannel;
	}

	@Override
	public void send() throws BufException, IOException {
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(new Data((PacketStarts.AUTHENTICY_RESPONSE.toString() + "," + new String(data)).getBytes()));
		
		System.out.println(String.format("sending authenticy response: %s\ndata: %s", dataComponent.represent(true), new String(dataComponent.getData().getData())));
		
		StreamByteBuf streamByteBuf = new StreamByteBuf(applicationChannel.getOutputStream());
		
		try {
			streamByteBuf.write(dataComponent);
			//streamByteBuf.write(dataComponent);
		} catch (BufException e) {
			e.printStackTrace();
		}
	}

}
