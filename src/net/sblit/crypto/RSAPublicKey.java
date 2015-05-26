package net.sblit.crypto;

import java.math.BigInteger;

import org.dclayer.exception.crypto.InsufficientKeySizeException;

public class RSAPublicKey extends org.dclayer.crypto.key.RSAPublicKey{
	
	private BigInteger modulus;
	private BigInteger exponent;
	
	public RSAPublicKey(BigInteger modulus, BigInteger exponent)
			throws InsufficientKeySizeException {
		super(modulus, exponent);
		this.modulus = modulus;
		this.exponent = exponent;
	}
	
	@Override
	public String toString() {
		return exponent + ";" + modulus;
	}

}
