package net.sblit.filesync.requests;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import net.sblit.configuration.Configuration;
import net.sblit.crypto.SymmetricEncryption;
import net.sblit.filesync.Packet;
import net.sblit.filesync.PacketStarts;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
@Deprecated
public class ConflictRequest implements Packet {

	public static HashSet<ConflictRequest> requests = new HashSet<>();
	private String originalFile;
	private String newFile;
	private final Date timestamp = new Date();
	private Data receiver;

	/**
	 * Is sent to tell the sender of a file that the file is in conflict with a
	 * file on the own device.
	 * 
	 * @param originalFile
	 *            Path of the original File
	 * @param newFile
	 *            The new files logfile entry
	 */
	public ConflictRequest(String originalFile, String newFile, Data receiver) {
		this.newFile = newFile;
		this.originalFile = originalFile;
		this.receiver = receiver;
		requests.add(this);
	}

	@Override
	public synchronized void send() throws BufException,IOException {
		byte[] data = new String(PacketStarts.CONFLICT_REQUEST + ","
				+ originalFile + "," + newFile + "," + timestamp.getTime())
				.getBytes();
		Data encryptedData = new Data(new SymmetricEncryption(Configuration.getKey())
				.encrypt(data));
		DataComponent dataComponent = new DataComponent();
		dataComponent.setData(encryptedData);
		
		System.out.println(String.format("sending: %s", dataComponent.represent(true)));
		
		StreamByteBuf streamByteBuf = new StreamByteBuf(Configuration.getChannel(receiver).getOutputStream());
		try {
			streamByteBuf.write(dataComponent);
		} catch (BufException e) {
			e.printStackTrace();
		}
		Configuration.getChannel(receiver).getOutputStream().flush();
	}

	public long getTimestamp() {
		return timestamp.getTime();
	}

	public String getOriginalFile() {
		return originalFile;
	}
	
	public String getNewFile(){
		return newFile;
	}
}
