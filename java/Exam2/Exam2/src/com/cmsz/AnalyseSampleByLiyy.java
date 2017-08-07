package com.cmsz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志分析的简单样例
 */
public class AnalyseSampleByLiyy implements Analyse {

	@Override
	public void doAnalyse(String path) {

		File[] files = listDir(path);

		Map<String,String> sendResult = new HashMap<String,String>();
		Map<String,String> receiveResult = new HashMap<String,String>();
		Map<String,String> resultResult = new HashMap<String,String>();

//		根据文件名进行遍历，分类解析文件
		for (File file : files){
			if (file.getName().startsWith("send")){

//				System.out.println(file.getName());
				try {
					sendResult.putAll(parse(file));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (file.getName().startsWith("receive")) {
//				System.out.println(file.getName());
				try {
					receiveResult.putAll(parse(file));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

//		比对文件并进行结果输出
		resultResult = getResult(sendResult,receiveResult);

		List<String>  success = new ArrayList<String>();
		success.add("SUCCESS:");
		List<String>  fail = new ArrayList<String>();
		fail.add("FAIL:");

// 		按类型获取文件流水号
		for (String key :resultResult.keySet()){
			if ("success" == resultResult.get(key)){
				success.add(key);
			}else if ("fail" == resultResult.get(key)) {
				fail.add(key);
			}
		}

//		写入文件
		String resultFile = path + (path.endsWith(File.separator) ? "" : File.separator) + "result.txt.me.txt";;
		if (new File(resultFile).exists()){
			new File(resultFile).delete();
		}

		try {
			write(resultFile,success);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			write(resultFile,fail);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static File[] listDir(String dir){
		if (dir == ""){
			throw new IllegalArgumentException("dir is empety");
		}else if(dir == null){
			throw new IllegalArgumentException("dir is null");
		}

		File dirF = new File(dir);
		File[] files = dirF.listFiles();
		return files;
	}

	public static Map<String,String> parse(File sendLog) throws IOException{
		if (sendLog==null){
			throw new IllegalArgumentException("File sendlog is null");
		}else if ( ! sendLog.exists()){
			throw new IllegalArgumentException("File sendlog is not exists");
		}else if(sendLog.isDirectory()){
			throw new IllegalArgumentException("File sendlog is directory");
		}

		Map<String,String> sendResult =new HashMap<String,String>();
		BufferedReader reader = new BufferedReader(new FileReader(sendLog));
		String line="";
		while((line = reader.readLine())!=null){
			String[] line_list = line.split("#");

			String serial = line_list[3].split(":")[1];
			String status = line_list[4].split(":")[1].toLowerCase();
			sendResult.put(serial, status);
		}

		reader.close();
		return sendResult;
	}

	public static void write(String filename, List<String> strings) throws IOException{
		File out = new File(filename);
		if (filename==null){
			throw new IllegalArgumentException("File sendlog is null");
		}else if ( ! out.exists()){
			out.createNewFile();
		}

		System.out.println(out.getAbsolutePath());
		BufferedWriter writer =new BufferedWriter(new FileWriter(out,true));

		for (String str : strings){
			writer.write(str);
			writer.newLine();
		}

		writer.flush();
		writer.close();
	}

	public static Map<String, String> getResult(Map<String, String> send,Map<String, String> receive){
		if (send == null){
			throw new IllegalArgumentException("send is null");
		}else if (receive == null) {
			throw new IllegalArgumentException("receive is null");
		}

		Map<String, String> result = new HashMap<String, String>();


		for (String key:send.keySet()){
			if (send.get(key).matches("send")){
				if (receive.get(key).matches("receive")){
					result.put(key, "success");
				}else{
					result.put(key, "fail");
				}
			}else{
				result.put(key, "fail");
			}
		}

		return result;
	}

}
