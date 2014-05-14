package br.com.orlandoburli.framework.core.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Criptografia {

	private Cipher cipher;
	private byte[] encryptKey;
	private KeySpec keySpec;
	private SecretKeyFactory secretKeyFactory;
	private SecretKey secretKey;

	/**
	 * Method that create a new instance of class.
	 * 
	 * @param key
	 * 
	 * @return
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 */
	public static Criptografia newInstance(String key) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
		return new Criptografia(key);
	}

	/**
	 * Default Constructor.
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 */
	private Criptografia(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {

		encryptKey = key.getBytes("UTF-8");
		cipher = Cipher.getInstance("DESede");
		keySpec = new DESedeKeySpec(encryptKey);
		secretKeyFactory = SecretKeyFactory.getInstance("DESede");
		secretKey = secretKeyFactory.generateSecret(keySpec);
	}

	/**
	 * Method that encrypts a value.
	 * 
	 * @param value
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 */
	public String cripto(String value) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] cipherText = cipher.doFinal(value.getBytes("UTF-8"));

		return DatatypeConverter.printBase64Binary(cipherText);
	}

	/**
	 * Methot that decrypts a value.
	 * 
	 * @param value
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public String decripto(String value) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		byte[] decipherText = cipher.doFinal(DatatypeConverter.parseBase64Binary(value));
		return new String(decipherText);
	}
}