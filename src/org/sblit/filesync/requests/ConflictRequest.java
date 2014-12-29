package org.sblit.filesync.requests;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.sblit.Sblit;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
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
	public void send() throws BufException,IOException {
		byte[] data = new String(PacketStarts.CONFLICT_REQUEST + ","
				+ originalFile + "," + newFile + "," + timestamp.getTime())
				.getBytes();
		byte[] encryptedData = new SymmetricEncryption(Configuration.getKey())
				.encrypt(data);
		for (Data receiver : Configuration.getChannels()){
			OutputStream out = Configuration.getChannel(receiver)
					.getOutputStream();
			out.write(encryptedData);
			
		}
	}

	public long getTimestamp() {
		return timestamp.getTime();
	}

	public String getOriginalFile() {
		return originalFile;
	}
}
