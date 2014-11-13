package org.sblit.crypto;

public class AsymmetricEncryption {
	
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
		if(publicKey == null)
			throw new NullPointerException();
		//TODO asymmetric encryption
		return null;
	}
	
	public byte[] decrypt(byte[] data) throws NullPointerException {
		if(privateKey == null)
			throw new NullPointerException();
		//TODO
		return null;
	}
	
}
