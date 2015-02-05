package net.sblit.configuration.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JFileChooser;
/**
 * 
 * @author Nikola
 *
 */
@Deprecated
public class DataDirectoryChooser {
	private File dataDirectoryFile;
	private JFileChooser fileChooser;

	/**
	 * Creates a new DataDirectoryChooser to choose the directory for sblit.
	 * If exists, it gets the data directory from the configuration file.
	 * @param configurationDirectory
	 * The directory where sblit configurations are saved
	 */
	public DataDirectoryChooser(File configurationDirectory) {
		dataDirectoryFile = new File(configurationDirectory.getAbsolutePath()
				+ "/DataDirectory.txt");
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	/**
	 * Returns the configured data directory.
	 * @return
	 * Returns the configured data directory.
	 */
	public File getDataDirectory()  {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(dataDirectoryFile)));
			File dataDirectory = new File(br.readLine());
			br.close();
			return dataDirectory;
		} catch (Exception e) {
			int dialog = fileChooser.showDialog(null,
					"Choose your Sblit directory");
			if (dialog == JFileChooser.APPROVE_OPTION) {
				try {
					dataDirectoryFile.createNewFile();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataDirectoryFile)));
					bw.write(fileChooser.getSelectedFile().getAbsolutePath());
					bw.close();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
				return fileChooser.getSelectedFile();
			}
			System.exit(0);
			return getDataDirectory();
		}

	}
}
