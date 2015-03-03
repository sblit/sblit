package net.sblit.directoryWatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sblit.configuration.Configuration;

import org.dclayer.crypto.Crypto;
import org.dclayer.net.Data;

/**
 * 
 * @author Nikola
 * 
 */
public class DirectoryWatcher {

	/**
	 * 
	 */
	private final static int TIME_TO_SLEEP = 1000;

	private File[] filesToPush;
	private File[] filesToDelete;
	private File logFile;

	public final static int INDEX_OLD_HASH = 0;
	public final static int INDEX_CURRENT_HASH = 1;

	/**
	 * 
	 * @param directory
	 *            Sblits home directory
	 * @param logFile
	 *            Log-file for the file information
	 * @param deviceIdentifier
	 *            Identifier of the device which has to be unique between all
	 *            devices to synchronize This identifier should be appended to
	 *            the files-list, that everybody knows if the file is
	 *            synchronized between all your devices and can be deleted from
	 *            the partner devices
	 */
	public DirectoryWatcher(File directory, File logFile, String deviceIdentifier) {
		this.logFile = logFile;
		Path filesDirectory = Paths.get(directory.getAbsolutePath());
		System.out.println("FilesDirectory: \"" + filesDirectory + "\"");

		try {
			WatchService watcher = filesDirectory.getFileSystem().newWatchService();
			filesDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey watchKey = watcher.take();
			List<WatchEvent<?>> events = watchKey.pollEvents();
			Thread.sleep(TIME_TO_SLEEP);

			Map<String, LinkedList<Data>> files = getLogs();
			Map<String, LinkedList<Data>> synchronizedDevices = getSynchronizedDevices();

			filesToPush = new File[0];
			filesToDelete = new File[0];

			for (@SuppressWarnings("rawtypes")
			WatchEvent event : events) {
				byte[] fileContent = readFile(event);
				// �berpr�ft, was mit dem File passiert ist
				Data hash = Crypto.sha1(new Data(fileContent));
				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					LinkedList<Data> hashes = new LinkedList<>();
					hashes.add(hash);
					files.put(event.context().toString(), hashes);
					LinkedList<Data> synchronizedDevice = new LinkedList<>();
					synchronizedDevice.add(Configuration.getPublicAddressKey().toData());
					synchronizedDevices.put(event.context().toString(), synchronizedDevice);
					filesToPush = refreshFilesArray(filesToPush,
							new File(Configuration.getSblitDirectory() + Configuration.slash
									+ event.context().toString()));
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					files.remove(event.context().toString());
					filesToDelete = refreshFilesArray(filesToDelete,
							new File(Configuration.getSblitDirectory() + Configuration.slash
									+ event.context().toString()));
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					// �berpr�ft, ob ein User die �nderung vorgenommen
					// hat, oder
					// das
					// File synchronisiert wurde bzw. ob sich �berhaupt etwas
					// ge�ndert hat
					LinkedList<Data> hashes = files.get(event.context().toString());
					if (hashes.contains(hash)) {
						// File was edited by sblit

					} else {
						hashes.add(hash);
						filesToPush = refreshFilesArray(filesToPush,
								new File(Configuration.getSblitDirectory() + Configuration.slash
										+ event.context().toString()));
						LinkedList<Data> synchronizedDevice = new LinkedList<>();
						synchronizedDevice.add(Configuration.getPublicAddressKey().toData());
						synchronizedDevices.put(event.context().toString(), synchronizedDevice);
					}

					System.out.println("Neuer Hash:" + hash.toString());
				}
				System.out.println(event.context().toString());
			}

			logFile.createNewFile();
			write(files, synchronizedDevices);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	private synchronized byte[] readFile(@SuppressWarnings("rawtypes") WatchEvent event)
			throws IOException {

		return Files.readAllBytes(Paths.get(Configuration.getSblitDirectory().getAbsolutePath()
				+ Configuration.slash + event.context().toString()));
	}

	private synchronized void write(Map<String, LinkedList<Data>> files,Map<String, LinkedList<Data>> synchronizedDevices) throws IOException {
		String s = "";
		for(String path:files.keySet()){
			s += ", " + path + "=";
			String temp = "";
			for(Data data : files.get(path)){
				temp += "," + data.toString();
			}
			s += temp.substring(1) + ";";
			temp = "";
			for(Data data : synchronizedDevices.get(path)){
				temp += "," + data.toString();
			}
			s += temp.substring(1);
		}
		s = s.substring(2);
		Files.write(logFile.toPath(), s.getBytes());
	}

	@Deprecated
	/**
	 * use getLogs instead
	 * @param logFile
	 * @return
	 * @throws IOException
	 */
	public synchronized static HashMap<String, String> getLogFileContent(File logFile)
			throws IOException {
		Map<String, String> files = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
					logFile)));
			String filesString = br.readLine();
			if (filesString != null) {
				String[] filesArray = filesString.split(", ");
				for (int i = 0; i < filesArray.length; i++) {
					files.put(filesArray[i].split("=")[0], filesArray[i].split("=")[1]);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			logFile.createNewFile();
		}
		return (HashMap<String, String>) files;
	}

	public synchronized static HashMap<String, LinkedList<Data>> getLogs() throws IOException {

		HashMap<String, LinkedList<Data>> result = new HashMap<>();
		File logFile = new File(Configuration.getConfigurationDirectory().getAbsolutePath()
				+ Configuration.LOG_FILE);

		if (logFile.exists()) {
			String[] logFileContent = new String(Files.readAllBytes(logFile.toPath())).split(", ");
			for (String line : logFileContent) {
				if(line.trim().equals(""))
					break;
				LinkedList<Data> hashes = new LinkedList<>();
				for (String s : line.split("=")[1].split(";")[0].split(",")) {
					Data temp = new Data();
					temp.parse(s);
					hashes.add(temp);
				}

				result.put(line.split("=")[0], hashes);
			}
		}

		return result;
	}

	public synchronized static HashMap<String, LinkedList<Data>> getSynchronizedDevices()
			throws IOException {
		HashMap<String, LinkedList<Data>> result = new HashMap<>();
		File logFile = new File(Configuration.getConfigurationDirectory().getAbsolutePath()
				+ Configuration.LOG_FILE);
		try {
			if (logFile.exists()) {
				String[] logFileContent = new String(Files.readAllBytes(logFile.toPath()))
						.split(", ");
				for (String line : logFileContent) {
					if(line.trim().equals("")) break;
					LinkedList<Data> hashes = new LinkedList<>();
					for (String s : line.split("=")[1].split(";")[1].split(",")) {
						Data temp = new Data();
						temp.parse(s);
						hashes.add(temp);
					}
					result.put(line.split("=")[0], hashes);
				}
			}
		} catch (NoSuchFileException e) {

		}
		return result;
	}

	private File[] refreshFilesArray(File[] oldFileArray, File newFile) {
		File[] temp = filesToPush.clone();
		filesToPush = new File[filesToPush.length + 1];
		for (int i = 0; i < temp.length; i++) {
			filesToPush[i] = temp[i];
		}
		filesToPush[filesToPush.length - 1] = newFile;
		return filesToPush;
	}

	public File[] getFilesToPush() {
		return filesToPush;
	}

	public File getLogFile() {
		return logFile;
	}

}
