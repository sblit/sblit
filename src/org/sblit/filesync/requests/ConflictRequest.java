package org.sblit.filesync.requests;

import java.util.Date;
import java.util.HashSet;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.filesync.Packet;
import org.sblit.filesync.PacketStarts;

public class ConflictRequest implements Packet {

	public static HashSet<ConflictRequest> requests = new HashSet<>();
	private String originalFile;
	private String newFile;
	private final Date timestamp = new Date();

	/**
	 * Is sent to tell the sender of a file that the file is in conflict with a
	 * file on the own device.
	 * 
	 * @param originalFile
	 *            Path of the original File
	 * @param newFile
	 *            The new files logfile entry
	 */
	public ConflictRequest(String originalFile, String newFile) {
		this.newFile = newFile;
		this.originalFile = originalFile;
		requests.add(this);
	}

	@Override
	public void send() throws BufException {
		byte[] data = new String(PacketStarts.CONFLICT_REQUEST + ","
				+ originalFile + "," + newFile + "," + timestamp.getTime())
				.getBytes();
		// TODO encryption
		byte[] encryptedData = new byte[34];
		for (String receiver : Configuration.getReceivers())
			Configuration.getApp().send(data, Sblit.APPLICATION_IDENTIFIER,
					receiver);
	}

	public long getTimestamp() {
		return timestamp.getTime();
	}

	public String getOriginalFile() {
		return originalFile;
	}
}
