package com.chinuaunicom.httpclient;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang.StringUtils;
import com.chinuaunicom.httpclient.CkeyCallback;
import com.chinuaunicom.httpclient.SkeyCallback;

import com.chinuaunicom.httpclient.AESC;
import com.chinuaunicom.httpclient.Ckey;
import com.chinuaunicom.httpclient.TxtIO;
import com.chinuaunicom.httpclient.FileDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.google.gson.Gson;
import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ipv6HttpClient {
	private static Logger logger = LoggerFactory.getLogger(Ipv6HttpClient.class);    //日志记录
    
    public static void main(String[] args) {
    	Gson gs = new Gson();
    	String result;
    	Date dt = new Date(); 
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");   
        String date=sdf.format(dt); 
    	String h="";  
    	String nh="";
    	Calendar cal=Calendar.getInstance();
    	int time=cal.get(Calendar.HOUR_OF_DAY);
    	switch(time){
    		case 0:h="1";break;
    		case 1:h="1";break;
    		case 2:h="1";break;
    		case 3:h="1";break;
    		case 4:h="2";break;
    		case 5:h="2";break;
    		case 6:h="2";break;
    		case 7:h="2";break;
    		case 8:h="3";break;
    		case 9:h="3";break;
    		case 10:h="3";break;
    		case 11:h="3";break;
    		case 12:h="4";break;
    		case 13:h="4";break;
    		case 14:h="4";break;
    		case 15:h="4";break;
    		case 16:h="5";break;
    		case 17:h="5";break;
    		case 18:h="5";break;
    		case 19:h="5";break;
    		case 20:h="6";break;
    		case 21:h="6";break;
    		case 22:h="6";break;
    		case 23:h="6";break;
    	}
    	String monitorLogName = "/root/ipv6/monitorLog.txt";
    	String pathname = "/root/ipv6/skey.txt";
    	String fileName="portalSiteIpv6"+"_"+date+"_"+h+".csv";
		String sourceFilePath = "/root/ipv6/fileLog/portalSiteIpv6"+"_"+date+"_"+h+".csv";
		String destFilePath = "/root/ipv6/encrypt/portalSiteIpv6"+"_"+date+"_"+h+".csv";
		try {
			TxtIO io = new TxtIO();
//			String monitorIoResult="";
//			monitorIoResult=io.readFile(monitorLogName,monitorIoResult);
	    	io.writeFile(monitorLogName,date+h+"-success");
	    	result = post(pathname);
			logger.info("result：" + result);
			//注册
			CkeyCallback ckb = gs.fromJson(result, CkeyCallback.class);
            logger.info("ckb：" + ckb);
	    	if(ckb.getCode().equals("0000")){
	    		//注册成功
	        	String skey=ckb.getSkey();
                logger.info("skey：" + skey);
	        	String identifier=ckb.getIdentifier();
	        	if(skey == null || skey.equals("")){
	        		logger.error("skey为空：");
	        	}else{
		        	io.writeFile(pathname,skey);
		        	System.out.println("Skey写入文件");
					System.out.println(ckb.getSkey());
	        	}
				//上传文件信息
				String uploadFileDescription = post2(sourceFilePath,fileName,identifier, skey);
				SkeyCallback skb = gs.fromJson(uploadFileDescription, SkeyCallback.class);
				if(skb.getCode().equals("0000")){
					//上传文件信息成功
					System.out.println(skb.getCode());
					//上传文件
					String upload = upload(sourceFilePath, destFilePath, identifier, skey);
					if(upload.equals("0000")){
						//上传文件成功
						System.out.println(upload);
						logger.info("文件上传成功");
					}else{
						//上传文件失败
						for(int i=0;i<10;i++){
							//重复10次上传
							Thread.sleep(180000);
							String upload2 = upload(sourceFilePath, destFilePath, identifier, skey);
							if(upload2.equals("0000")){
								System.out.println(upload2);
								logger.info("文件上传成功");
								break;
							}
							System.out.println("上传文件失败");
							logger.error("文件上传失败,Code:" + upload2);
						}
					}
				}else{
					//注册失败 重复20次文件信息上传
					for(int post=0;post<20;post++){
						Thread.sleep(10000);
						String uploadFileDescription2 = post2(sourceFilePath,fileName,identifier, skey);
						SkeyCallback skb2 = gs.fromJson(uploadFileDescription2, SkeyCallback.class);
						if(skb2.getCode().equals("0000")){
							//上传文件信息成功
							System.out.println(skb2.getCode());
							//上传文件
							String upload = upload(sourceFilePath, destFilePath, identifier, skey);
							if(upload.equals("0000")){
								//上传文件成功
								System.out.println(upload);
								logger.info("文件上传成功");
							}else{
								//上传文件失败
								for(int i=0;i<10;i++){
									//重复10次上传
									Thread.sleep(180000);
									String upload2 = upload(sourceFilePath, destFilePath, identifier, skey);
									if(upload2.equals("0000")){
										System.out.println(upload2);
										logger.info("文件上传成功");
										break;
									}
									System.out.println("上传文件失败");
									logger.error("文件上传失败,Code:" + upload2);
								}
							}
							break;
						}
						System.out.println(skb2.getCode());
						logger.error("文件信息传输失败,Code:" + skb2.getCode());
					}
				}
			}else{
				//注册失败 重复20次注册
				for(int register=0;register<20;register++){
					Thread.sleep(10000);
					result = post(pathname);
					logger.info("result2：" + result);
					CkeyCallback ckb2 = gs.fromJson(result, CkeyCallback.class);
					logger.info("ckb2：" + ckb2);
					if(ckb2.getCode().equals("0000")){
			        	String skey=ckb2.getSkey();
		                logger.info("skey：" + skey);
			        	String identifier=ckb2.getIdentifier();
			        	if(skey == null || skey.equals("")){
			        		logger.error("skey为空：");
			        	}else{
				        	io.writeFile(pathname,skey);
				        	System.out.println("Skey写入文件");
							System.out.println(ckb2.getSkey());
			        	}
						String uploadFileDescription = post2(sourceFilePath,fileName,identifier, skey);
						SkeyCallback skb = gs.fromJson(uploadFileDescription, SkeyCallback.class);
						if(skb.getCode().equals("0000")){
							System.out.println(skb.getCode());
							String upload = upload(sourceFilePath, destFilePath, identifier, skey);
							if(!upload.equals("0000")){
								for(int i=0;i<10;i++){
									Thread.sleep(180000);
									String upload2 = upload(sourceFilePath, destFilePath, identifier, skey);
									if(upload2.equals("0000")){
										System.out.println(upload2);
										logger.info("文件上传成功");
										break;
									}
									System.out.println("上传文件失败");
									logger.error("文件上传失败,Code:" + upload2);
								}
							}else{
								System.out.println(upload);
								logger.info("文件上传成功");
							}
						}else{
							//注册失败 重复20次文件信息上传
							for(int post=0;post<20;post++){
								Thread.sleep(10000);
								String uploadFileDescription2 = post2(sourceFilePath,fileName,identifier, skey);
								SkeyCallback skb2 = gs.fromJson(uploadFileDescription2, SkeyCallback.class);
								if(skb2.getCode().equals("0000")){
									//上传文件信息成功
									System.out.println(skb2.getCode());
									//上传文件
									String upload = upload(sourceFilePath, destFilePath, identifier, skey);
									if(upload.equals("0000")){
										//上传文件成功
										System.out.println(upload);
										logger.info("文件上传成功");
									}else{
										//上传文件失败
										for(int i=0;i<10;i++){
											//重复10次上传
											Thread.sleep(180000);
											String upload2 = upload(sourceFilePath, destFilePath, identifier, skey);
											if(upload2.equals("0000")){
												System.out.println(upload2);
												logger.info("文件上传成功");
												break;
											}
											System.out.println("上传文件失败");
											logger.error("文件上传失败,Code:" + upload2);
										}
									}
									break;
								}
								System.out.println(skb2.getCode());
								logger.error("文件信息传输失败,Code:" + skb2.getCode());
							}
						}
						break;
					}
					System.out.println(ckb2.getCode());
					logger.error("注册失败,Code:" + ckb2.getCode());
				}
			}
//        	if(!monitorIoResult.equals(date+h+"-success")){
//        		
//    		}else{
//    			logger.info("主机运行程序成功");
//    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    public static String upload(String sourceFilePath, String destFilePath, String identifier, String skey) throws Exception {
    	HttpsUtils httpsUtils = new HttpsUtils();
    	Gson gs = new Gson();
    	String upload = httpsUtils.upload(sourceFilePath,destFilePath,identifier, skey);
		SkeyCallback skb = gs.fromJson(upload, SkeyCallback.class);
		return skb.getCode();
    }
    public static String post(String pathname) throws Exception {
    	HttpsUtils httpsUtils = new HttpsUtils();
    	String result = httpsUtils.post(pathname);
    	return result;
    }
    public static String post2(String sourceFilePath, String fileName, String identifier, String skey) throws Exception {
    	HttpsUtils httpsUtils = new HttpsUtils();
    	String result = httpsUtils.post2(sourceFilePath,fileName,identifier, skey);
    	return result;
    }
}
