package net.sblit.fileProcessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import net.sblit.configuration.Configuration;
import net.sblit.directoryWatcher.DirectoryWatcher;

import org.dclayer.net.Data;

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
	
	//TODO bei gelegenheit aufr�umen
	public FileWriter(LinkedList<Data> logs, Data file, String filePath, LinkedList<Data> synchronizedDevices){
		fileWriters.add(this);
		try {
			changeLogs(logs, filePath, synchronizedDevices);
			System.out.println("File content: " + file.represent());
			Files.write(Paths.get(Configuration.getSblitDirectory() + Configuration.slash + filePath), file.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void changeLogs(LinkedList<Data> hashes, String filePath, LinkedList<Data> synchronizedDevices) throws IOException{
		HashMap<String, LinkedList<Data>> logs = DirectoryWatcher.getLogs();
		logs.remove(filePath);
		LinkedList<Data> log = new LinkedList<Data>();
		for(Data hash: hashes)
			log.add(hash);
		logs.put(filePath, log);
		HashMap<String, LinkedList<Data>> devices = DirectoryWatcher.getSynchronizedDevices();
		devices.remove(filePath);
		LinkedList<Data> device = new LinkedList<>();
		for(Data temp : synchronizedDevices){
			device.add(temp);
		}
		devices.put(filePath, device);
		String s = "";
		for(String path:logs.keySet()){
			s += ", " + path + "=";
			String temp = "";
			for(Data data : logs.get(path)){
				temp += "," + data.toString();
			}
			s += temp.substring(1) + ";";
			temp = "";
			for(Data data : devices.get(path)){
				temp += "," + data.toString();
			}
			s += temp.substring(1);
		}
		s = s.substring(2);
		Files.write(Paths.get(Configuration.getConfigurationDirectory().getAbsolutePath() + Configuration.LOG_FILE), s.getBytes());
		
	}
	
	@Deprecated
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
	
	@Deprecated
	private synchronized void writeLogFile(byte[] logFile) throws IOException {
		File logs = new File(Configuration.getConfigurationDirectory() + Configuration.LOG_FILE);
		Map<String, String> myLogFile = DirectoryWatcher.getLogFileContent(logs);
		Map<String, String> synchedLogFile = new HashMap<String, String>();
		String[] newLogFileStrings = new String(logFile).split(",");

		for (int i = 0; i < newLogFileStrings.length; i++) {
			String[] s = newLogFileStrings[i].split("=");
			synchedLogFile.put(s[0], s[1]);
		}

		String[] temp = synchedLogFile.get(path).split(";");

		myLogFile
				.put(path,
						temp[1] + ";" + temp[1] + ";" + temp[2] + " "
								+ Configuration.getPublicAddressKey());

		String[] files = (String[]) myLogFile.keySet().toArray();
		for (int i = 0; i < files.length; i++) {
			if (myLogFile.get(files[i]).split(";")[1].equals(synchedLogFile.get(files[i])
					.split(";")[1]) // �berpr�ft den Hash der
									// zuletzt ge�nderten Files
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

		if (myLogFile.containsKey(path)) {
			myLogFile.remove(path);
		}
		apply(logs, files);
		write();
	}

	private synchronized void apply(File logs, String[] files) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logs)));
		bw.write(files.toString().replace("{", "").replace("}", ""));
		bw.close();
	}

	/**
	 * 
	 * @param myversion
	 *            True, if the version on this device is the non-conflict
	 *            version
	 * 
	 * @throws IOException
	 */
	public void conflict(boolean myversion) throws IOException {
		if (!myversion) {
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

	private synchronized void write() throws IOException {
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
