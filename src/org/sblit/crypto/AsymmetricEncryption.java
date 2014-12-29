package org.sblit.crypto;

import java.security.KeyPair;
@Deprecated
public class AsymmetricEncryption {
	
	public static final int KEY_SIZE = 2048; //In bit
	public static final int RSA_ADDRESS_KEY_CERTAINTY = 160;
	
	byte[] publicKey;
	byte[] privateKey;
	
	public AsymmetricEncryption(byte[] publicKey, byte[] privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public AsymmetricEncryption(byte[] publicKey){
		this.publicKey = publicKey;
	}
	
	public byte[] encrypt(byte[] data) throws NullPointerException{
		if(privateKey == null)
			throw new NullPointerException();
		//TODO asymmetric encryption
		return null;
	}
	
	public byte[] decrypt(byte[] data) throws NullPointerException {
		if(publicKey == null)
			throw new NullPointerException();
		//TODO
		return null;
	}
	
	public static KeyPair generateNewKeyPair(int numBits){
		return null;
	}
	
}
