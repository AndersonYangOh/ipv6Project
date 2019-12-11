package com.chinuaunicom.httpclient;

import java.util.Date;

public class FileDescription {
	String FileName;
	int FileNo;
	String FileMd5;
	Long FileSize;
	String StartTime;
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public int getFileNo() {
		return FileNo;
	}
	public void setFileNo(int fileNo) {
		FileNo = fileNo;
	}
	public String getFileMd5() {
		return FileMd5;
	}
	public void setFileMd5(String fileMd5) {
		FileMd5 = fileMd5;
	}
	public Long getFileSize() {
		return FileSize;
	}
	public void setFileSize(Long fileSize) {
		FileSize = fileSize;
	}
	public String getStartTime() {
		return StartTime;
	}
	public void setStartTime(String startTime) {
		StartTime = startTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	String EndTime;
}
