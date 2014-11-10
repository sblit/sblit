package org.sblit.filesync.requests;

import java.util.HashSet;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class FileRequest implements Packet {

	int hashcode;
	public static HashSet<FileRequest> fileRequests = new HashSet<>(); 

	public FileRequest(int hashcode) {
		fileRequests.add(this);
		this.hashcode = hashcode;
	}

	@Override
	public void send() throws BufException {
		byte[] data = new String(PacketStarts.FILE_REQUEST.toString() + "," + hashcode).getBytes();
		// TODO verschlüsseln
		for (String receiver : Configuration.getReceivers())
			Configuration.getApp().send(data, Sblit.APPLICATION_IDENTIFIER,
					receiver);
	}

}
