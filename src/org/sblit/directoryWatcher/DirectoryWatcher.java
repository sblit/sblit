package org.sblit.directoryWatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sblit.configuration.Configuration;

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

			Map<String, String> files = getLogFileContent(logFile);

			filesToPush = new File[0];
			filesToDelete = new File[0];

			for (@SuppressWarnings("rawtypes")
			WatchEvent event : events) {
				byte[] fileContent = readFile(event);
				// Überprüft, was mit dem File passiert ist
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(fileContent);
				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					files.put(event.context().toString(), " ;" + md.digest() + ";"
							+ deviceIdentifier);
					filesToPush = refreshFilesArray(filesToPush, new File(Configuration.getSblitDirectory() + Configuration.slash + event.context()
							.toString()));
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					files.remove(event.context().toString());
					filesToDelete = refreshFilesArray(filesToDelete, new File(Configuration.getSblitDirectory() + Configuration.slash + event.context()
							.toString()));
				} else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					// Überprüft, ob ein User die Änderung vorgenommen hat, oder
					// das
					// File synchronisiert wurde bzw. ob sich überhaupt etwas
					// geändert hat
					System.out.println("Neuer Hash:" + new String(md.digest()));
					System.out.println("Alter Hash: " + files.get(event.context().toString()).split(";")[1]);
					if (!md.digest().equals(files.get(event.context().toString()).split(";")[1])) {
						String oldHashCode = files.get(event.context().toString()).split(";")[0];
						files.remove(event.context().toString());
						files.put(event.context().toString(), oldHashCode + ";" + md.digest() + ";"
								+ deviceIdentifier);
						filesToPush = refreshFilesArray(filesToPush, new File(Configuration.getSblitDirectory() + Configuration.slash + event.context()
								.toString()));
					}
				}
			}

			logFile.createNewFile();

			write(files);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private synchronized byte[] readFile(@SuppressWarnings("rawtypes") WatchEvent event)
			throws IOException {

		return Files.readAllBytes(Paths.get(Configuration.getSblitDirectory().getAbsolutePath() + Configuration.slash
				+ event.context().toString()));
	}

	private synchronized void write(Map<String, String> files) throws IOException {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(logFile)));
		bw.write(files.toString().replace("{", "").replace("}", ""));
		bw.close();
		System.out.println(files.toString().replace("{", "").replace("}", ""));
	}

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
