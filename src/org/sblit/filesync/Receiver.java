package org.sblit.filesync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.lib.DCLApplication;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.fileProcessing.FileProcessor;
import org.sblit.fileProcessing.FileWriter;
import org.sblit.filesync.exceptions.TimestampException;
import org.sblit.filesync.requests.ConflictRequest;
import org.sblit.filesync.responses.ConflictResponse;
import org.sblit.filesync.responses.FileResponse;

/**
 * 
 * @author Nikola
 * 
 */

public class Receiver implements Runnable {

	private DCLApplication app;

	public Receiver(DCLApplication app) {
		this.app = app;
	}

	public byte[] receive() throws BufException {
		return app.receive();
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		while (true)
			try {
				//TODO handle foreign files
				byte[] received = new SymmetricEncryption(Configuration.getKey()).decrypt(receive());
				String s = new String(received);
				if (s.startsWith(PacketStarts.CONFLICT_REQUEST.toString())) {
					handleConflictRequest(received);
				} else if (s.startsWith(PacketStarts.CONFLICT_RESPONSE.toString())) {
					handleConflictResponse(received);
				} else if(s.startsWith(PacketStarts.FILE_REQUEST.toString())) {
					handleFileRequest(received);
				} else if(s.startsWith(PacketStarts.FILE_RESPONSE.toString())){
					handleFileResponse(received);
				} else if(s.startsWith(PacketStarts.AUTHENTICY_REQUEST.toString())){
					handleAuthenticyRequest(received);
				} else {
				
					FileProcessor fileProcessor = new FileProcessor(received);
					byte[] checksum = receive();
				}
			} catch (BufException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

	}
	
	private void handleAuthenticyRequest(byte[] received){
		String data = new String(received);
		//TODO decrypt asymmetrically 
	}
	
	private void handleFileResponse(byte[] received) throws IOException{
		String data = new String(received); 
		if(Boolean.parseBoolean(data.split(",")[1])){
			try {
				new FileSender(Configuration.getKey(), app, Configuration.getPublicAddressKey().toString()).sendOwnFiles(Configuration.getReceivers(), new File[]{}, new File(Configuration.getConfigurationDirectory() + Configuration.LOG_FILE));
			} catch (BufException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleFileRequest(byte[] received) throws IOException{
		String data = new String(received);
		byte[] otherHashcode = data.split(",")[1].getBytes();
		boolean need;
		if (new String(Files.readAllBytes(Paths.get(Configuration
				.getConfigurationDirectory() + Configuration.LOG_FILE)))
				.contains(new String(otherHashcode))) {
			need=false;
		} else {
			need=true;
		}
		new FileResponse(need, otherHashcode);
		
	}
	
	private void handleConflictResponse(byte[] received) throws IOException {
		String data = new String(received);
		for (FileWriter fileWriter : FileWriter.fileWriters) {
			if (fileWriter.getFile().equals(data.split(",")[1])) {
				fileWriter.conflict(Boolean.parseBoolean(data.split(",")[2]));
				FileWriter.fileWriters.remove(fileWriter);
			}
		}
	}

	private void handleConflictRequest(byte[] received) throws BufException,
			IOException {
		// boolean requestExists = false;
		try {
			for (ConflictRequest request : ConflictRequest.requests) {
				String data = new String(received);
				if (request.getOriginalFile().equals(data.split(",")[1])) {
					boolean accepted;
					if (request.getTimestamp() > Integer.parseInt(data
							.split(",")[3])) {
						// Other File wins
						accepted = true;
					} else if (request.getTimestamp() == Integer.parseInt(data
							.split(",")[3])) {
						TimestampException e = new TimestampException();
						throw e;
					} else {
						// This File wins
						accepted = false;
					}
					ConflictResponse response = new ConflictResponse(accepted,
							request.getOriginalFile());
					response.send();

					for (FileWriter fileWriter : FileWriter.fileWriters) {
						if (fileWriter.getFile().equals(data.split(",")[1])) {
							fileWriter.conflict(!accepted);
							FileWriter.fileWriters.remove(fileWriter);
						}
					}

					ConflictRequest.requests.remove(request);
				}
			}
		} catch (TimestampException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			// TODO Handle exception (send conflict request again)
		}
	}
}
