package com.chinuaunicom.httpclient;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class RSA {
	//RSA公钥加密
		public static byte[] publicEncrypt(byte[] content) throws Exception{
			String publicKeyStr = "***";
			PublicKey publicKey = string2PublicKey(publicKeyStr);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] bytes = cipher.doFinal(content);
			return bytes;
		}
		//RSA将Base64编码后的公钥转换成PublicKey对象
		public static PublicKey string2PublicKey(String pubStr) throws Exception{
			byte[] keyBytes = base642Byte(pubStr);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		}
		//字节数组转Base64编码
		public static String byte2Base64(byte[] bytes){
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(bytes);
		}
			
		//Base64编码转字节数组
		public static byte[] base642Byte(String base64Key) throws IOException{
			BASE64Decoder decoder = new BASE64Decoder();
			return decoder.decodeBuffer(base64Key);
		}
}
