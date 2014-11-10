package org.sblit.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
/**
 * 
 * @author Nikola
 *
 */
class AddressConfiguration {
	
	/**
	 * The file which contains the private address key.
	 */
	final static String PRIVATE_FILE = "/rk.txt";
	
	/**
	 * The file which contains the public address key.
	 */
	final static String PUBLIC_FILE = "/uk.txt";
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	/**
	 * Creates new addresses if not configured before.
	 * Otherwise it creates a new address key-pair. 
	 * @param configurationDirectory
	 * The configuration directory of sblit.
	 * @param os
	 * The operating system of the current system.
	 */
	AddressConfiguration(File configurationDirectory, String os) {
		
		try {
			KeyPair keyPair = getKeyPair(configurationDirectory);
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (Exception e) {
			createNewAddresses(configurationDirectory, os);
		}
		
	}
	
	/**
	 * Creates a new address-pair and saves it to the configuration directory.
	 * @param configurationDirectory
	 * Configuration directory (for saving Files).
	 * @param os
	 * The current operating system
	 */
	void createNewAddresses(File configurationDirectory, String os){
		//TODO get addresses from DCLayer
		//Creates the new directory for the configuration files 
		configurationDirectory.mkdir();
		try {
			//Generates a new keypair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair keyPair = keyGen.generateKeyPair();

			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			
			saveKeyPair(keyPair, configurationDirectory, os);
		} catch(Exception e1){
			e1.printStackTrace();
		}
	}
	
	/**
	 * Returns the private address key.
	 * @return
	 * Returns the private address key.
	 */
	PrivateKey getPrivateKey(){
		return privateKey;
	}
	
	/**
	 * Returns the public address key.
	 * @return
	 * Returns the public address key.
	 */
	PublicKey getPublicKey(){
		return publicKey;
	}
	
	/**
	 * Saves the address key-pair to the file .
	 * @param keyPair
	 * The key-pair to save.
	 * @param configurationDirectory
	 * The configuration directory in which the key pair shall be saved
	 * @param os
	 * The current operating system
	 */
	private void saveKeyPair(KeyPair keyPair, File configurationDirectory, String os){
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
		//Writes the private key into the file
		File privateFile = new File(configurationDirectory.getAbsolutePath() + PRIVATE_FILE);
		try {
			privateFile.createNewFile();
			FileOutputStream privateOut = new FileOutputStream(privateFile);
			X509EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(privateKey.getEncoded());
			privateOut.write(privateKeySpec.getEncoded());
			privateOut.close();
			
			//Hides the file
			if(os.contains("Windows"))
				Runtime.getRuntime().exec("attrib +H " + privateFile.getAbsolutePath());
			
			//Writes the public Key into the file
			File publicFile = new File(configurationDirectory.getAbsolutePath() + PUBLIC_FILE);
			publicFile.createNewFile();
			FileOutputStream publicOut = new FileOutputStream(publicFile);
			PKCS8EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(publicKey.getEncoded());
			publicOut.write(publicKeySpec.getEncoded());
			publicOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns the key-pair which was already configured and saved to the given directory.
	 * @param configurationDirectory
	 * The configuration directory of sblit.
	 * @return
	 * Returns the key-pair which was already configured.
	 * @throws FileNotFoundException
	 * If the key-pair wasn't configured.
	 * @throws IOException
	 * If an error occurs while reading the file.
	 */
	private KeyPair getKeyPair(File configurationDirectory) throws FileNotFoundException, IOException{
		File privateFile = new File(configurationDirectory + PRIVATE_FILE);
		File publicFile = new File(configurationDirectory + PUBLIC_FILE);
		
		byte[] privateKeyBytes = new byte[(int)privateFile.length()];
		FileInputStream fis = new FileInputStream(privateFile);
		fis.read(privateKeyBytes);
		fis.close();
		
		byte[] publicKeyBytes = new byte[(int)publicFile.length()];
		fis = new FileInputStream(publicFile);
		fis.read(publicKeyBytes);
		fis.close();
		PublicKey publicKey = null;
		PrivateKey privateKey = null;
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return new KeyPair(publicKey, privateKey);
	}
	
}
