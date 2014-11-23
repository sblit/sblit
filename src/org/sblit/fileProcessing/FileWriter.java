package org.sblit.fileProcessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.dclayer.exception.net.buf.BufException;
import org.sblit.configuration.Configuration;
import org.sblit.directoryWatcher.DirectoryWatcher;
import org.sblit.filesync.requests.ConflictRequest;

public class FileWriter {
	private String path;
	private String newPath;
	private File logs;
	private String[] files;
	private byte[] fileContent;
	private boolean conflict;
	public static HashSet<FileWriter> fileWriters = new HashSet<>();

	/**
	 * FileWriter for own files
	 * 
	 * @param logFile
	 *            Logfile content of the logfile which was sent with the File
	 * @param file
	 *            Content of the file
	 * @param filePath
	 *            Path of the File
	 */
	public FileWriter(byte[] logFile, byte[] file, byte[] filePath) {
		fileWriters.add(this);
		path = new String(filePath);
		fileContent = file;
		try {
			writeLogFile(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeLogFile(byte[] logFile) throws IOException {
		File logs = new File(Configuration.getConfigurationDirectory()
				+ Configuration.LOG_FILE);
		Map<String, String> myLogFile = DirectoryWatcher
				.getLogFileContent(logs);
		Map<String, String> synchedLogFile = new HashMap<String, String>();
		String[] newLogFileStrings = new String(logFile).split(",");

		for (int i = 0; i < newLogFileStrings.length; i++) {
			String[] s = newLogFileStrings[i].split("=");
			synchedLogFile.put(s[0], s[1]);
		}

		String[] temp = synchedLogFile.get(path).split(";");

		myLogFile.put(path, temp[1] + ";" + temp[1] + ";" + temp[2] + " "
				+ Configuration.getPublicAddressKey());

		String[] files = (String[]) myLogFile.keySet().toArray();
		for (int i = 0; i < files.length; i++) {
			if (myLogFile.get(files[i]).split(";")[1].equals(synchedLogFile
					.get(files[i]).split(";")[1]) // überprüft den Hash der
													// zuletzt geänderten Files
					&& myLogFile.get(files[i]).split(",")[2].length() < synchedLogFile
							.get(files[i]).split(",")[2].length()) {
				String value = myLogFile.get(files[i]);
				value = value.split(";")[0] + ";" + value.split(";")[1] + ";";
				value += synchedLogFile.get(files[i]).split(";")[3];

				myLogFile.put(files[i], value);
			}
		}
		logs.delete();
		logs.createNewFile();

		// Conflict detection
		if (myLogFile.get(path).split(";")[1].equals(synchedLogFile.get(path)
				.split(";")[0])) {
			conflict = false;
			if (myLogFile.containsKey(path)) {
				myLogFile.remove(path);
			}
			apply(logs, files);
			write();
		} else {
			conflict = true;
			int i = 1;
			newPath = path;
			while (new File(newPath).exists()) {
				newPath = path.substring(0, path.lastIndexOf('.'))
						+ "(conflict " + i + ")"
						+ path.substring(path.lastIndexOf('.'));
			}
			try {
				this.logs = logs;
				this.files = files;
				ConflictRequest request = new ConflictRequest(path, newPath);
				request.send();
			} catch (BufException e) {
				e.printStackTrace();
			}
		}
	}

	private void apply(File logs, String[] files) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logs)));
		bw.write(files.toString().replace("{", "").replace("}", ""));
		bw.close();
	}

	/**
	 * 
	 * @param myversion
	 * True, if the version on this device is the non-conflict version
	 * 
	 * @throws IOException
	 */
	public void conflict(boolean myversion) throws IOException {
		if(!myversion){
			String temp = newPath;
			newPath = path;
			path = temp;
		}
		
		apply(logs, files);
		write();
	}

	public String getFile() {
		return path;
	}

	private void write() throws IOException {
		File f;
		if (!conflict) {
			f = new File(path);
		} else {
			f = new File(newPath);
		}
		if (!f.exists()) {
			f.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(fileContent);
		fos.close();
	}
}
