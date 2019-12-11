package com.chinuaunicom.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESC {
	// 算法名称
	final String KEY_ALGORITHM = "AES";
	// 加解密算法/模式/填充方式
	final String algorithmStr = "AES/CBC/PKCS5Padding";
	//
	private Key key;
	private Cipher cipher;
	boolean isInited = false;

	byte[] iv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	public void init(byte[] keyBytes) {

		// 如果密钥不足16位，那么就补足. 这个if 中的内容很重要
		int base = 16;
		if (keyBytes.length % base != 0) {
			int groups = keyBytes.length / base + (keyBytes.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
			keyBytes = temp;
		}
		// 初始化
		Security.addProvider(new BouncyCastleProvider());
		// 转化成JAVA的密钥格式
		key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
		try {
			// 初始化cipher
			cipher = Cipher.getInstance(algorithmStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 加密方法
	 *
	 * @param content
	 *            要加密的字符串
	 * @param keyBytes
	 *            加密密钥
	 * @return
	 */
	public byte[] encrypt(byte[] content, byte[] keyBytes) {
		byte[] encryptedText = null;
		init(keyBytes);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			encryptedText = cipher.doFinal(content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedText;
	}

	/**
	 * 加密文件方法
	 *
	 * @param content
	 *            要加密的字符串
	 * @param keyBytes
	 *            加密密钥
	 * @return
	 */
	public byte[] encryptFile(String sourceFilePath, String destFilePath, byte[] keyBytes) {
		File sourceFile = new File(sourceFilePath);
		File destFile = new File(destFilePath);
		byte[] encryptedText = null;
		init(keyBytes);
		try {
			if (sourceFile.exists() && sourceFile.isFile()) {
				if (!destFile.getParentFile().exists()) {
					destFile.getParentFile().mkdirs();
				}
				destFile.createNewFile();
				InputStream in = new FileInputStream(sourceFile);
				OutputStream out = new FileOutputStream(destFile);
				cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
				CipherInputStream cin = new CipherInputStream(in, cipher);
				// encryptedText = cipher.doFinal(content);
				byte[] cache = new byte[1024];
				int nRead = 0;
				while ((nRead = cin.read(cache)) != -1) {
					out.write(cache, 0, nRead);
					out.flush();
				}
				out.close();
				cin.close();
				in.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedText;
	}

	/**
	 * 解密方法
	 *
	 * @param encryptedData
	 *            要解密的字符串
	 * @param keyBytes
	 *            解密密钥
	 * @return
	 */
	public byte[] decrypt(byte[] encryptedData, byte[] keyBytes) {
		byte[] encryptedText = null;
		init(keyBytes);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			encryptedText = cipher.doFinal(encryptedData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedText;
	}
	/**
	 * 解密文件方法
	 *
	 * @param encryptedData
	 *            要解密的字符串
	 * @param keyBytes
	 *            解密密钥
	 * @return
	 */
	public byte[] decryptFile(String sourceFilePath, String destFilePath, byte[] keyBytes) {
		File sourceFile = new File(sourceFilePath);
		File destFile = new File(destFilePath);
		byte[] encryptedText = null;
		init(keyBytes);
		try {
			if (sourceFile.exists() && sourceFile.isFile()) {
				if (!destFile.getParentFile().exists()) {
					destFile.getParentFile().mkdirs();
				}
				destFile.createNewFile();
				InputStream in = new FileInputStream(sourceFile);
				OutputStream out = new FileOutputStream(destFile);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
//			encryptedText = cipher.doFinal(encryptedData);
			CipherOutputStream cout = new CipherOutputStream(out, cipher);
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = in.read(cache)) != -1) {
                cout.write(cache, 0, nRead);
                cout.flush();
            }
            cout.close();
            out.close();
            in.close();

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encryptedText;
	}
}
