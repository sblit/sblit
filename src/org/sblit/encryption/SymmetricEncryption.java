package org.sblit.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author Nikola
 * 
 */
@Deprecated
public class SymmetricEncryption {

	private SecretKeySpec key;
	private Cipher cipher;

	/**
	 * Creates a new instance for symmetric encryption.
	 * @param key
	 * The key for the encryption
	 */
	public SymmetricEncryption(byte[] key) {
		this.key = new SecretKeySpec(key, "AES");
		try {
			cipher = Cipher.getInstance("AES");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Encrypts a byte array with the key specified in the constructor.
	 * @param decrypted
	 * The decrypted byte array.
	 * @return
	 * The encrypted byte array.
	 */
	public byte[] encryptByteArray(byte[] decrypted) {
		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encrypted = cipher.doFinal(decrypted);
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		return encrypted;
	}

	
	/**
	 * Decrypts a byte array with the key specified in the constructor
	 * @param encrypted
	 * The encrypted byte array.
	 * @return
	 * The decrypted byte array.
	 */
	public byte[] decryptByteArray(byte[] encrypted) {
		byte[] decrypted = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
			decrypted = cipher.doFinal(encrypted);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return decrypted;
	}
}
