package net.sblit.converter;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.crypto.InsufficientKeySizeException;
import org.dclayer.net.Data;

public class Converter {
	public static byte[] intToByteArray(int integer)
	{
	    byte[] byteArray = new byte[4];
	    byteArray[3] = (byte) (integer & 0xFF);   
	    byteArray[2] = (byte) ((integer >> 8) & 0xFF);   
	    byteArray[1] = (byte) ((integer >> 16) & 0xFF);   
	    byteArray[0] = (byte) ((integer >> 24) & 0xFF);
	    return byteArray;
	}
	
	/**
	 * Parses long in a byte array.
	 * @param number
	 * Long to parse.
	 * @return
	 * The byte array.
	 */
	public static byte[] longToByteArray(long number){
		return new byte[] {
		        (byte) (number >> 56),
		        (byte) (number >> 48),
		        (byte) (number >> 40),
		        (byte) (number >> 32),
		        (byte) (number >> 24),
		        (byte) (number >> 16),
		        (byte) (number >> 8),
		        (byte) number
		    };
	}
	public static int byteArrayToInt(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}
	
	public static RSAPublicKey dataToKey(Data key){
		byte[] modulus = new byte[257];
		byte[] exponent = new byte[3];
		for (int i = 0; i < modulus.length; i++)
			modulus[i] = key.getByte(i);
		for (int i = 0; i < exponent.length; i++)
			exponent[i] = key.getByte(i + modulus.length);
		try {
			return new RSAPublicKey(new BigInteger(modulus), new BigInteger(exponent));
		} catch (InsufficientKeySizeException e) {
			e.printStackTrace();
			return null;
		}
		
	}

}


