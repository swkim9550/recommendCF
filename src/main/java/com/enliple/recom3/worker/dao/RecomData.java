package com.enliple.recom3.worker.dao;


import com.enliple.recom3.worker.dto.AuidPcodesMap;
import com.enliple.recom3.worker.dto.PcodeAuidsMap;
import com.enliple.recom3.worker.dto.PcodeCateMap;
import com.enliple.recom3.worker.dto.ProductDto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.enliple.recom3.jpa.db1.domain.JobInfo;

@Slf4j
public class RecomData {
	@Getter protected PcodeAuidsMap pcodeAuidsMap   = new PcodeAuidsMap();//baseFile (imp or conv)
	@Getter protected AuidPcodesMap auidPcodesMap   = new AuidPcodesMap();//baseFile (imp or conv)
	//상품카테고리관련 보류중.
	//@Getter protected PcodeCateMap pcodeCateMap   = new PcodeCateMap();//baseFile (imp or conv)
	@Getter @Setter protected JobInfo jobInfo = null;
	@Getter @Setter protected HashMap<String, ProductDto> productMap = null;
	public long readFile(String filePath) {
		log.info("read file name is {}.",filePath);
		long readFileDataCount = 0;	
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			
			String line;
			line = br.readLine();
			for ( ; line != null ; line = br.readLine()){
				String[] logs = line.split(",");
				if( logs.length > 3 ) {
					continue;
				}
				if(logs[0].compareTo("1") == 0 ) {
					continue;
				}
				readFileDataCount++;

				pcodeAuidsMap.put( logs[0], logs[1]);
				auidPcodesMap.put( logs[1], logs[0]);
				//pcodeCateMap.put(e[0],e[2]); 상품카테고리관련 보류중.
			}
		} 
		catch (Exception e) {
			log.error("read file error.. content : {}",e.toString());
		}
		finally {
			try {
				if (br != null) br.close();
				if (fr != null) fr.close();
			} catch( IOException e) {
				log.error("read file error.. content : {}",e.toString());
			}
		}
		return readFileDataCount;
	}		
}
