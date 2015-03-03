package net.sblit.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataDirectoryConfiguration {
	
	public static final String DATA_DIRECTORY = "/sblitDirectory.txt";
	private File dir;
	private File configurationFile;
	
	public DataDirectoryConfiguration(String configurationDirectory) {
		configurationFile = new File(configurationDirectory + DATA_DIRECTORY);
		if(configurationFile.exists()){
			try {
				byte[] data = Files.readAllBytes(Paths.get(configurationDirectory + DATA_DIRECTORY));
				dir = new File(new String(data).split("\n")[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				configurationFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public File getDataDirectory(){
		return dir;
	}
	
	void setDataDirectory(String directory) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configurationFile)));
			bw.write(directory);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
