package org.sblit.crypto;

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
	CipherParameters key;
	
	public SymmetricEncryption(byte[] key) {
		assert key.length == 512; //512 Byte == 4096 Bit
		this.key = new KeyParameter(key);
	}
	
	public byte[] encrypt(byte[] decrypted){
		return process(decrypted, true);
	}
	
	private byte[] process(byte[] data, boolean encryption){
		BlockCipher cipher = new AESEngine();
		BlockCipherPadding padding = new ZeroBytePadding();
		BufferedBlockCipher bufferedCipher = new PaddedBufferedBlockCipher(cipher, padding);
		bufferedCipher.init(encryption, key);
		byte[] output = new byte[bufferedCipher.getOutputSize(data.length)];
		int bytesProcessed = bufferedCipher.processBytes(data, 0, data.length, output, 0);
		try {
			bufferedCipher.doFinal(output, bytesProcessed);
			return output;
		} catch (DataLengthException | IllegalStateException
				| InvalidCipherTextException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] encrypted){
		return process(encrypted, false);
	}
	
	public static byte[] generateKey(){
		//TODO generate key
		return null;
	}
}
