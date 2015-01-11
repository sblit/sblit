package org.sblit.crypto;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * 
 * @author Nikola Szucsich
 *
 */

public class SymmetricEncryption {
	
	public static final int KEY_SIZE = 256; //In bit
	
	private CipherParameters key;
	
	public SymmetricEncryption(byte[] key) {
		assert key.length == KEY_SIZE/8; //Convert bit to byte
		this.key = new KeyParameter(key);
	}
	
	public byte[] encrypt(byte[] decrypted){
		return process(decrypted, true);
	}
	
	private byte[] process(byte[] data, boolean encryption) throws DataLengthException {
		BlockCipher cipher = new AESEngine();
		BlockCipherPadding padding = new ZeroBytePadding();
		BufferedBlockCipher bufferedCipher = new PaddedBufferedBlockCipher(cipher, padding);
		bufferedCipher.init(encryption, key);
		byte[] output = new byte[bufferedCipher.getOutputSize(data.length)];
		int bytesProcessed = bufferedCipher.processBytes(data, 0, data.length, output, 0);
		try {
			bufferedCipher.doFinal(output, bytesProcessed);
			return output;
		} catch (IllegalStateException
				| InvalidCipherTextException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] encrypted){
		return process(encrypted, false);
	}
	
	public static byte[] generateKey(){
		SecureRandom secureRandom = new SecureRandom();
		byte[] keyBytes = new byte[KEY_SIZE/8];
		secureRandom.nextBytes(keyBytes);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		return keySpec.getEncoded();
	}
}
