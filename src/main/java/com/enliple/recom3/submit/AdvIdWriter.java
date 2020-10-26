package com.enliple.recom3.submit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.TimeZone;

import com.enliple.recom3.common.utils.BeanUtils;
import com.enliple.recom3.common.config.Config;


public class AdvIdWriter {
	private String advId;
	private File file;
	private BufferedWriter bw;
	private FileWriter fw;
	private Config config;
	
	public AdvIdWriter( String advId, String day, String strFileSuffix, String basePath) throws IOException, URISyntaxException {
		this.advId = advId;
		this.config = BeanUtils.getBean(Config.class);

		newFileWrite(basePath,day,strFileSuffix);
		//oldFileWrite(basePath,day,strFileSuffix);
	}

	public void oldFileWrite(String basePath, String day,String strFileSuffix) throws IOException {
		// advId directory
		String path = String.format("%s/%s", basePath, day);
		file = new File(path);
		if( !file.exists()) file.mkdirs();

		path = String.format("%s/%s/%s.%s", basePath, day, advId, strFileSuffix);
		//if( isClick)
		//	path = String.format("%s/%s/%s.%s", basePath, day, advId, isPc ? "impp" : "impm");
		//else
		//	path = String.format("%s/%s/%s.%s", basePath, day, advId, isPc ? "convp" : "convm");

		//logger.warn("new AdvIdWriter : "+path);

		file = new File(path);
		if( file.exists()) file.delete();

		fw = new FileWriter(path);
		bw = new BufferedWriter(fw);

	}
	public void newFileWrite(String basePath, String day,String strFileSuffix) throws IOException {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String year = String.format("%02d", calendar.get(Calendar.YEAR));
		String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
		String folderName = year+month+day;

		// advId directory
		String path = String.format("%s/%s", basePath, folderName);
		file = new File(path);
		if( !file.exists()) file.mkdirs();

		path = String.format("%s/%s/%s.%s", basePath, folderName, advId, strFileSuffix);

		file = new File(path);
		if( file.exists()) file.delete();

		fw = new FileWriter(path);
		bw = new BufferedWriter(fw);
	}
	
	public void write( String line) throws IOException {
		bw.write( line + "\n");
	}
	
	public static String key(String advId, String strFileSuffix) {
		return String.format("%s%s", advId, strFileSuffix);
	}
	
	public void close() {
		try {
			if( bw != null) {
				bw.flush();
				bw.close();
			}
			if( fw != null) fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
