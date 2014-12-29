package org.sblit.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dclayer.crypto.key.KeyPair;
import org.dclayer.crypto.key.RSAKey;
import org.dclayer.crypto.key.RSAPrivateKey;
import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
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
	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
	
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
			KeyPair<RSAKey> keyPair = getKeyPair(configurationDirectory);
			privateKey = (RSAPrivateKey) keyPair.getPrivateKey();
			publicKey = (RSAPublicKey) keyPair.getPublicKey();
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
			@SuppressWarnings("unchecked")
			KeyPair<RSAKey> keyPair = org.dclayer.crypto.Crypto.generateAddressRSAKeyPair();
			
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
	RSAPrivateKey getPrivateKey(){
		return privateKey;
	}
	
	/**
	 * Returns the public address key.
	 * @return
	 * Returns the public address key.
	 */
	RSAPublicKey getPublicKey(){
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
	private void saveKeyPair(KeyPair<RSAKey> keyPair, File configurationDirectory, String os){
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivateKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublicKey();
		//Writes the private key into the file
		File privateFile = new File(configurationDirectory.getAbsolutePath() + PRIVATE_FILE);
		try {
			privateFile.createNewFile();
			BufferedWriter privateOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(privateFile)));
			privateOut.write(privateKey.getExponent() + ";" + privateKey.getModulus());
			privateOut.close();
			
			//Hides the file
			if(os.contains("Windows"))
				Runtime.getRuntime().exec("attrib +H " + privateFile.getAbsolutePath());
			
			//Writes the public Key into the file
			File publicFile = new File(configurationDirectory.getAbsolutePath() + PUBLIC_FILE);
			publicFile.createNewFile();
			BufferedWriter publicOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(publicFile)));
			publicOut.write(publicKey.getExponent() + ";" + publicKey.getModulus());
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
	private KeyPair<RSAKey> getKeyPair(File configurationDirectory) {
		File privateFile = new File(configurationDirectory + PRIVATE_FILE);
		File publicFile = new File(configurationDirectory + PUBLIC_FILE);

		String privateKeyString;
		RSAPrivateKey privateKey = null;
		RSAPublicKey publicKey = null;
		try {
			privateKeyString = new String(Files.readAllBytes(Paths
					.get(privateFile.getAbsolutePath())));
			privateKey = new RSAPrivateKey(new BigInteger(
					privateKeyString.split(";")[1]), new BigInteger(
					privateKeyString.split(";")[0]));

			String publicKeyString = new String(Files.readAllBytes(Paths
					.get(publicFile.getAbsolutePath())));
			publicKey = new RSAPublicKey(new BigInteger(
					publicKeyString.split(";")[1]), new BigInteger(
					publicKeyString.split(";")[0]));
		} catch (InsufficientKeySizeException | IOException e) {
			e.printStackTrace();
		}
		
		return KeyPair.fromKeys(publicKey, privateKey);
	}
	
}
