package org.sblit.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.sblit.crypto.SymmetricEncryption;

public class KeyConfiguration {

	public static final String KEY_CONFIGURATION_FILE = "/symmetricKey.txt";
	private String configurationFile;
	private byte[] key;
	private String os;

	public KeyConfiguration(String configurationDirectory, String os) {
		this.os = os;
		configurationFile = configurationDirectory + KEY_CONFIGURATION_FILE;
		if (new File(configurationFile).exists()) {
			try {
				key = Files.readAllBytes(Paths.get(configurationFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			key = SymmetricEncryption.generateKey();
			saveKey(key);
		}
	}

	public byte[] getKey() {
		return key;
	}

	private void saveKey(byte[] key) {
		File f = new File(configurationFile);
		try {
			f.createNewFile();
			if (os.contains("Windows"))
				Runtime.getRuntime()
						.exec("attrib +H "
								+ new File(configurationFile).getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(key);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void setKey(byte[] key) {
		this.key = key;
		File f = new File(configurationFile);
		f.delete();
		saveKey(key);
	}
}
