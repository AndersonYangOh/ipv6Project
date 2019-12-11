package com.chinuaunicom.httpclient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinuaunicom.httpclient.SkeyCallback;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HttpsUtils {
	private static Logger logger = LoggerFactory.getLogger(Ipv6HttpClient.class);    //日志记录
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static SSLConnectionSocketFactory sslsf = null;
    private static PoolingHttpClientConnectionManager cm = null;
    private static SSLContextBuilder builder = null;
    static {
        try {
            builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, new PlainConnectionSocketFactory())
                    .register(HTTPS, sslsf)
                    .build();
            cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(200);//max connection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * httpClient post请求 注册接口
     * @return 可能为空 需要处理
     * @throws Exception
     *
     */
    public static String post(String pathname) throws Exception {
    	String url = "***";
    	String jsonResult = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost(url);
          //随机生成Ckey
    		AESC aesC = new AESC();
    		RSA rsa=new RSA();
    		Random random = new Random();
    		byte[] keybytes = new byte[16];
    		for (int i = 0; i < keybytes.length; i++)
    			keybytes[i] = (byte)random.nextInt(256);
    		// 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
  		   	int base = 16;
  		   	if (keybytes.length % base != 0) {
	  		   int groups = keybytes.length / base + (keybytes.length % base != 0 ? 1 : 0);
	  		   byte[] temp = new byte[groups * base];
	  		   Arrays.fill(temp, (byte) 0);
	  		   System.arraycopy(keybytes, 0, temp, 0, keybytes.length);
	  		   keybytes = temp;
  		   	}
    		String keybytesString = bytesToHexString(keybytes);
    		String json;
    		String jsonEncrypt;
    		Gson gs = new Gson();
    		Ckey ck = new Ckey();
    		//添加传递参数
    		Map<String,Object> ckMap=new HashMap<>();
    		ckMap.put("AppNo", "103001");
    		ckMap.put("Ckey", keybytesString);
    		//获取上一次Skey
        	TxtIO io = new TxtIO();
        	String ioResult="";
        	ioResult=io.readFile(pathname,ioResult);
//        	ioResult=ioResult.substring(0,ioResult.length() - 1);
        	System.out.println("文件：" + ioResult);
    		//增加Skey
    		if(!ioResult.equals("")){
    			ckMap.put("Skey", ioResult);
    		}
    		json = gs.toJson(ckMap);
    		System.out.println("json：" + json);
    		//RSA加密
    		byte[] ckeyToByte=json.getBytes();
    		byte[] ckeySendByte=rsa.publicEncrypt(ckeyToByte);
    		String ckeySend = bytesToHexString(ckeySendByte);
//    		String ckeySend = rsa.byte2Base64(ckeySendByte);
    		System.out.println("Encrypt：" + ckeySend);
    		ck.setEncryptMsg(ckeySend);
    		jsonEncrypt = gs.toJson(ck);
    		System.out.println("jsonEncrypt：" + jsonEncrypt);
            // 设置请求参数
    		try {
	    		StringEntity entity = new StringEntity(jsonEncrypt, "utf-8");
	            entity.setContentEncoding("UTF-8");
	            entity.setContentType("application/json");
	            httpPost.setEntity(entity);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    		HttpResponse result = httpClient.execute(httpPost);
            url = URLDecoder.decode(url, "UTF-8");
            logger.info("http返回代码:" + result.getStatusLine().getStatusCode());
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(result.getEntity());
                    logger.info("返回内容：" + str);
                    Ckey ckResponse = gs.fromJson(str, Ckey.class);
                  //解密
            		byte[] keybytesResult=hexStringToByte(ckResponse.getEncryptMsg());
            		byte[] resultDecrypt = aesC.decrypt(keybytesResult, keybytes);
                    logger.info("返回内容解密后：" + new String(resultDecrypt));
                    /**把json字符串转换成json对象**/
                    jsonResult = new String(resultDecrypt);
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (Exception e) {
        	throw e;
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return jsonResult;
    }
    
    /**
     * httpClient post请求 文件信息接口
     * @return 可能为空 需要处理
     * @throws Exception
     *
     */
    public static String post2(String sourceFilePath, String fileName, String identifier, String skey) throws Exception {
    	String url = "***";
    	String jsonResult = null;
        CloseableHttpClient httpClient = null;
		String json = null;
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost(url);
			AESC aesC = new AESC();
    		byte[] skeyToByte=hexStringToByte(skey);
          //文件信息
    		//时间点
    		String h="";    
        	Calendar cal=Calendar.getInstance();
        	int time=cal.get(Calendar.HOUR_OF_DAY);
        	switch(time){
        		case 0:h=" 00:";break;
        		case 1:h=" 00:";break;
        		case 2:h=" 00:";break;
        		case 3:h=" 00:";break;
        		case 4:h=" 04:";break;
        		case 5:h=" 04:";break;
        		case 6:h=" 04:";break;
        		case 7:h=" 04:";break;
        		case 8:h=" 08:";break;
        		case 9:h=" 08:";break;
        		case 10:h=" 08:";break;
        		case 11:h=" 08:";break;
        		case 12:h=" 12:";break;
        		case 13:h=" 12:";break;
        		case 14:h=" 12:";break;
        		case 15:h=" 12:";break;
        		case 16:h=" 16:";break;
        		case 17:h=" 16:";break;
        		case 18:h=" 16:";break;
        		case 19:h=" 16:";break;
        		case 20:h=" 20:";break;
        		case 21:h=" 20:";break;
        		case 22:h=" 20:";break;
        		case 23:h=" 20:";break;
        	}
    		
        	FileDescription fd = new FileDescription();
        	FileReport fr = new FileReport();
        	File file = new File(sourceFilePath);
    		Gson gs = new Gson();
    		if (file.exists() && file.isFile()) {
        		fr.setIdentifier(identifier);
            	fd.setFileNo(1);
            	fd.setFileName(fileName);
            	fd.setFileSize(file.length());
            	try {
    				fd.setFileMd5(DigestUtils.md5Hex(new FileInputStream(sourceFilePath)));
    			} catch (FileNotFoundException e1) {
    				e1.printStackTrace();
    			} catch (IOException e1) {
    				e1.printStackTrace();
    			}
            	SimpleDateFormat dfStart = new SimpleDateFormat("yyyy-MM-dd");
            	SimpleDateFormat dfEnd = new SimpleDateFormat("yyyy-MM-dd");
            	fd.setStartTime(dfStart.format(new Date())+h+"00");
            	fd.setEndTime(dfEnd.format(new Date())+h+"10");
    			FileDescription[] encryptMsg=new FileDescription[1];
    			encryptMsg[0]=fd;
    			String jsonMsg;
    			jsonMsg = gs.toJson(encryptMsg);
    			System.out.println("加密前：" + jsonMsg);
    			//AES加密,使用skey
        		byte[] jsonMsgToByte=jsonMsg.getBytes();
        		byte[] encryptMsgEncrypt=aesC.encrypt(jsonMsgToByte,skeyToByte);
        		String encryptMsgString=bytesToHexString(encryptMsgEncrypt);
        		System.out.println("加密后：" + encryptMsgString);
    			fr.setEncryptMsg(encryptMsgString);
    			json=gs.toJson(fr);
    			System.out.println("json：" + json);
            }
            // 设置请求参数
    		try {
	    		StringEntity entity = new StringEntity(json, "utf-8");
	            entity.setContentEncoding("UTF-8");
	            entity.setContentType("application/json");
	            httpPost.setEntity(entity);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    		HttpResponse result = httpClient.execute(httpPost);
            url = URLDecoder.decode(url, "UTF-8");
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(result.getEntity());
                    logger.info("返回内容：" + str);
                    if(str.equals("{\"Code\":\"0201\"}")){
                    	jsonResult=str;
                    	logger.info("返回内容无需解密：" + str);
                    }else{
                    	Ckey ckResponse = gs.fromJson(str, Ckey.class);
                        //解密
                    	try{
                      		byte[] keybytesResult=hexStringToByte(ckResponse.getEncryptMsg());
                      		byte[] resultDecrypt = aesC.decrypt(keybytesResult, skeyToByte);
                      		logger.info("返回内容解密后：" + new String(resultDecrypt));
                              /**把json字符串转换成json对象**/
                              jsonResult = new String(resultDecrypt);
          					logger.info("文件路径:" + sourceFilePath);
                    	}catch(Exception e){
                            logger.error("解密出错:" + url, e);
                    		SkeyCallback sc=new SkeyCallback();
                			sc.setCode("1000000");
                			jsonResult=gs.toJson(sc);
                    	}
                    }
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (Exception e) {
        	throw e;
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return jsonResult;
    }
    
    /**
     * httpClient post请求 上传文件接口
     * @return 可能为空 需要处理
     * @throws Exception
     *
     */
    public static String upload(String sourceFilePath, String destFilePath, String identifier, String skey) throws Exception {
    	String url = "***";
    	String jsonResult = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = getHttpClient();
			AESC aesC = new AESC();
			byte[] skeyToByte=hexStringToByte(skey);
			Gson gs = new Gson();
//	        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000).build();
	        HttpPost httpPost = new HttpPost(url);
//	        httpPost.setConfig(requestConfig);
	        aesC.encryptFile(sourceFilePath,destFilePath,skeyToByte);
	        File file = new File(destFilePath);
	        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
	        //添加文件以及参数
	        multipartEntityBuilder.addBinaryBody("file",file);
	        multipartEntityBuilder.addTextBody("Identifier", identifier);
	        HttpEntity httpEntity = multipartEntityBuilder.build();
	        httpPost.setEntity(httpEntity);
	             
	        HttpResponse result = httpClient.execute(httpPost);
	        url = URLDecoder.decode(url, "UTF-8");
	        if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(result.getEntity());
                    logger.info("返回内容：" + str);
                    if(str.equals("{\"Code\":\"0201\"}")){
                    	jsonResult=str;
                    	logger.info("返回内容无需解密：" + str);
                    }else{
                    	Ckey ckResponse = gs.fromJson(str, Ckey.class);
                        //解密
                    	try{
                      		byte[] keybytesResult=hexStringToByte(ckResponse.getEncryptMsg());
                      		byte[] resultDecrypt = aesC.decrypt(keybytesResult, skeyToByte);
                      		logger.info("返回内容解密后：" + new String(resultDecrypt));
                            jsonResult = new String(resultDecrypt);
                    	}catch(Exception e){
                            logger.error("解密出错:" + url, e);
                    		SkeyCallback sc=new SkeyCallback();
                			sc.setCode("1000000");
                			jsonResult=gs.toJson(sc);
                    	}
                    }
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
            
        } catch (Exception e) {
        	throw e;
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return jsonResult;
    }
    
    public static CloseableHttpClient getHttpClient() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();
        return httpClient;
    }
    public static String readHttpResponse(HttpResponse httpResponse)
            throws ParseException, IOException {
        StringBuilder builder = new StringBuilder();
        // 获取响应消息实体
        HttpEntity entity = httpResponse.getEntity();
        // 响应状态
        builder.append("status:" + httpResponse.getStatusLine());
        builder.append("headers:");
        HeaderIterator iterator = httpResponse.headerIterator();
        while (iterator.hasNext()) {
            builder.append("\t" + iterator.next());
        }
        // 判断响应实体是否为空
        if (entity != null) {
            String responseString = EntityUtils.toString(entity);
            builder.append("response length:" + responseString.length());
            builder.append("response content:" + responseString.replace("\r\n", ""));
        }
        return builder.toString();
    }
    
//    /**
//	 * 数组转换成十六进制字符串
//	 * 
//	 * @param byte[]
//	 * @return HexString
//	 */
//	public static final String bytesToHexString(byte[] bArray) {
//		StringBuffer sb = new StringBuffer(bArray.length);
//		String sTemp;
//		for (int i = 0; i < bArray.length; i++) {
//			sTemp = Integer.toHexString(0xFF & bArray[i]);
//			if (sTemp.length() < 2)
//				sb.append(0);
//			sb.append(sTemp.toUpperCase());
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * 把16进制字符串转换成字节数组
//	 * 
//	 * @param hexString
//	 * @return byte[]
//	 */
//	public static byte[] hexStringToByte(String hex) {
//		int len = (hex.length() / 2);
//		byte[] result = new byte[len];
//		char[] achar = hex.toCharArray();
//		for (int i = 0; i < len; i++) {
//			int pos = i * 2;
//			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
//		}
//		return result;
//	}
//
//	private static int toByte(char c) {
//		byte b = (byte) "0123456789ABCDEF".indexOf(c);
//		return b;
//	}
    public static String bytesToHexString(byte[] byteArray) {
	    if (byteArray == null) {
	        return null;
	    }
	    char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[byteArray.length * 2];
	    for (int j = 0; j < byteArray.length; j++) {
	        int v = byteArray[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	public static byte[] hexStringToByte(String str) {
	    if (str == null) {
	        return null;
	    }
	    if (str.length() == 0) {
	        return new byte[0];
	    }
	    byte[] byteArray = new byte[str.length() / 2];
	    for (int i = 0; i < byteArray.length; i++) {
	        String subStr = str.substring(2 * i, 2 * i + 2);
	        byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
	    }
	    return byteArray;
	}
}