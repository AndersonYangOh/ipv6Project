package com.chinuaunicom.httpclient;

import java.io.*;

public class TxtIO {
	/**
     * 读入TXT文件
     */
    public static String readFile(String pathname, String result) {
    	File file = new File(pathname);
        FileReader fr=null;
 
        try {
            fr = new FileReader(file);
            char[] temp=new char[(int) file.length()];
            fr.read(temp);
            fr.close();
            result=new String(temp);
            System.out.println(result);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//    	try{
//			String encoding = "Unicode";
//			String tmpLineVal;
//			InputStreamReader read = new InputStreamReader(new FileInputStream(pathname), encoding);
//			BufferedReader bufread = new BufferedReader(read);
//			
//			while((tmpLineVal = bufread.readLine())!=null)
//			{
//				result=tmpLineVal;
//				System.out.println(tmpLineVal);		
//			}
//			bufread.close();
//			read.close();
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
        return result;
    }

    /**
     * 写入TXT文件
     */
    public void writeFile(String pathname, String content) {
        try {
            File writeName = new File(pathname); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(content); // \r\n即为换行
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
