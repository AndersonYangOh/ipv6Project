package com.chinuaunicom.httpclient;

import com.chinuaunicom.httpclient.FileDescription;

public class FileReport {
	String Identifier;
	String encryptMsg;
	public String getIdentifier() {
		return Identifier;
	}
	public void setIdentifier(String identifier) {
		Identifier = identifier;
	}
	public String getEncryptMsg() {
		return encryptMsg;
	}
	public void setEncryptMsg(String encryptMsg) {
		this.encryptMsg = encryptMsg;
	}
}
