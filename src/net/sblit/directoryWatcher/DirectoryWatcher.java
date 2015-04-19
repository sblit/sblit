package net.sblit.directoryWatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
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
	private WatchService watcher;

	private Map<WatchKey, Path> watchKeys;

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

		watchKeys = new HashMap<WatchKey, Path>();

		try {
			watcher = filesDirectory.getFileSystem().newWatchService();
			filesDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

			registerAll(directory.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void registerAll(final Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException {
				WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

				watchKeys.put(key, dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public void waitForChanges() throws InterruptedException, IOException {
		WatchKey watchKey = watcher.take();
		Path dir = watchKeys.get(watchKey);
		System.out.println("WatchKey Directory: " + dir.toString());
		List<WatchEvent<?>> events = watchKey.pollEvents();
		Thread.sleep(TIME_TO_SLEEP);

		Map<String, LinkedList<Data>> files = getLogs();
		Map<String, LinkedList<Data>> synchronizedDevices = getSynchronizedDevices();

		filesToPush = new File[0];
		filesToDelete = new File[0];

		for (@SuppressWarnings("rawtypes")
		WatchEvent event : events) {
			File file = new File(dir + Configuration.slash + event.context().toString());
			String relativePath = file.getAbsolutePath()
					.replace(Configuration.getSblitDirectory().getAbsolutePath(), "").substring(1);
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				LinkedList<Data> hashes = new LinkedList<>();
				if (!file.isDirectory()) {
					byte[] fileContent = Files.readAllBytes(file.toPath());
					// �berpr�ft, was mit dem File passiert ist
					Data hash = Crypto.sha1(new Data(fileContent));
					hashes.add(hash);
					files.put(relativePath, hashes);
					LinkedList<Data> synchronizedDevice = new LinkedList<>();
					synchronizedDevice.add(Configuration.getPublicAddressKey().toData());
					synchronizedDevices.put(relativePath, synchronizedDevice);
				} else {
					registerAll(Configuration.getSblitDirectory().toPath());
				}
				filesToPush = refreshFilesArray(filesToPush, file);
			} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				files.remove(relativePath);
				if (file.isDirectory()) {
					registerAll(Configuration.getSblitDirectory().toPath());
				}
				filesToDelete = refreshFilesArray(filesToDelete, file);
			} else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				LinkedList<Data> hashes = files.get(relativePath);
				if (hashes == null)
					hashes = new LinkedList<>();
				boolean needToSend = true;
				if (!file.isDirectory()) {
					byte[] fileContent = Files.readAllBytes(file.toPath());
					// �berpr�ft, was mit dem File passiert ist
					Data hash = Crypto.sha1(new Data(fileContent));
					needToSend = !hashes.contains(hash);
					hashes.add(hash);
				} else {
					registerAll(Configuration.getSblitDirectory().toPath());
				}
				// Ueberprueft, ob ein User die Aenderung vorgenommen
				// hat, oder
				// das
				// File synchronisiert wurde bzw. ob sich �berhaupt etwas
				// ge�ndert hat
				if (!needToSend) {
					// File was edited by sblit

				} else {
					filesToPush = refreshFilesArray(filesToPush, file);
					LinkedList<Data> synchronizedDevice = new LinkedList<>();
					synchronizedDevice.add(Configuration.getPublicAddressKey().toData());
					synchronizedDevices.put(relativePath, synchronizedDevice);
				}
				;
			}
			System.out.println("Kontext: " + relativePath);
		}
		watchKey.reset();
		logFile.createNewFile();
		System.out.println(files);
		write(files, synchronizedDevices);
	}

	// private synchronized byte[] readFile(@SuppressWarnings("rawtypes")
	// WatchEvent event)
	// throws IOException {
	//
	// return
	// Files.readAllBytes(Paths.get(Configuration.getSblitDirectory().getAbsolutePath()
	// + Configuration.slash + event.context().toString()));
	// }

	private synchronized void write(Map<String, LinkedList<Data>> files,
			Map<String, LinkedList<Data>> synchronizedDevices) throws IOException {
		String s = "";
		try {
			if (files.size() > 0) {
				StringBuilder builder = new StringBuilder();
				for (String path : files.keySet()) {
					if (!new File(path).isDirectory()) {
						builder.append("\n");
						builder.append(path);
						builder.append("=");
						StringBuilder temp = new StringBuilder();
						for (Data data : files.get(path)) {
							temp.append(",");
							temp.append(data.toString());
						}
						temp.append(";");
						builder.append(temp.substring(1));
						temp = new StringBuilder();
						for (Data data : synchronizedDevices.get(path)) {
							temp.append(",");
							temp.append(data.toString());
						}
						builder.append(temp.substring(1));
					}
					System.out.println(builder);
				}
				s = builder.substring(1);
			}
			Files.write(logFile.toPath(), s.getBytes());
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	private synchronized void write(Map<String, LinkedList<Data>> files,
			Map<String, LinkedList<Data>> synchronizedDevices,
			Map<String, LinkedList<Data>> conflictOf) throws IOException {
		String s = "";
		try {
			if (files.size() > 0) {
				StringBuilder builder = new StringBuilder();
				for (String path : files.keySet()) {
					if (new File(path).isFile()) {
						builder.append("\n");
						builder.append(path);
						builder.append("=");
						StringBuilder temp = new StringBuilder();
						for (Data data : files.get(path)) {
							temp.append(",");
							temp.append(data.toString());
						}
						temp.append(";");
						builder.append(temp.substring(1));
						temp = new StringBuilder();
						for (Data data : synchronizedDevices.get(path)) {
							temp.append(",");
							temp.append(data.toString());
						}
						if (conflictOf.get(path) != null) {
							temp.append(";");
							temp.append(conflictOf.get(path));
						}
						builder.append(temp.substring(1));
					}
				}
				s = builder.substring(1);
			}
			Files.write(logFile.toPath(), s.getBytes());
		} catch (StringIndexOutOfBoundsException e) {

		}
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
				String[] filesArray = filesString.split("\n");
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
			String[] logFileContent = new String(Files.readAllBytes(logFile.toPath())).split("\n");
			for (String line : logFileContent) {
				if (!line.trim().equals("")) {
					LinkedList<Data> hashes = new LinkedList<>();
					for (String s : line.split("=")[1].split(";")[0].split(",")) {
						Data temp = new Data();
						temp.parse(s);
						hashes.add(temp);
					}

					result.put(line.split("=")[0], hashes);
				}
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
						.split("\n");
				for (String line : logFileContent) {
					if (line.trim().equals(""))
						break;
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
		File[] temp = oldFileArray.clone();
		oldFileArray = new File[oldFileArray.length + 1];
		for (int i = 0; i < temp.length; i++) {
			oldFileArray[i] = temp[i];
		}
		oldFileArray[oldFileArray.length - 1] = newFile;
		return oldFileArray;
	}

	public File[] getFilesToPush() {
		File[] filesToPush = this.filesToPush;
		this.filesToPush = new File[0];
		return filesToPush;
	}

	public File getLogFile() {
		return logFile;
	}

	public File[] getFilesToDelete() {
		File[] filesToDelete = this.filesToDelete;
		this.filesToDelete = new File[0];
		return filesToDelete;
	}

}
