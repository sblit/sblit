package net.sblit.crypto;

import java.math.BigInteger;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.dclayer.exception.crypto.InsufficientKeySizeException;

public class RSAPublicKey extends org.dclayer.crypto.key.RSAPublicKey{
	
	public RSAPublicKey(BigInteger modulus, BigInteger exponent)
			throws InsufficientKeySizeException {
		super(modulus, exponent);
	}
	
	public RSAPublicKey(RSAKeyParameters rsaKeyParameters) throws InsufficientKeySizeException{
		super(rsaKeyParameters);
	}
	
	@Override
	public String toString() {
		return super.getExponent() + ";" + super.getModulus();
	}
	
	public org.dclayer.crypto.key.RSAPublicKey getRSAPublicKey() throws InsufficientKeySizeException{
		return new org.dclayer.crypto.key.RSAPublicKey(super.getRSAKeyParameters());
	}

}
