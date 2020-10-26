package com.enliple.recom3.submit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.recom3.common.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SeperateFile {

	@Autowired
	Config config;

	public void run( String yesterday, String strFileSuffix) {

		HashMap<String, AdvIdWriter> map = new HashMap<String, AdvIdWriter>();
		try {
			String filePath = String.format("%s/%s",config.getInFilePath(), yesterday);
			log.info("yesterday="+yesterday+", strFileSuffix="+strFileSuffix+", filePath="+filePath);
			boolean isPc = false;
			File files = new File(filePath);
			for( File file : files.listFiles()) {
				String path = file.getCanonicalPath();
				String suffix = path.substring( path.lastIndexOf(".") + 1);
				if(suffix.compareTo(strFileSuffix) !=  0) continue;
				if(suffix.endsWith("p")) {
					isPc = true;
				}else {
					isPc = false;
				}
				//if( isClick) {
				//	if( suffix.compareTo("impp") !=  0 && suffix.compareTo("impm") !=  0) continue;
				//	if(  suffix.compareTo("impp") ==  0) isPc = true;
				//	else isPc = false;
				//}
				//else {
				//	if( suffix.compareTo("convp") !=  0 && suffix.compareTo("convm") !=  0) continue;
				//
				//	if(  suffix.compareTo("convp") ==  0) isPc = true;
				//	else isPc = false;
				//}


				log.info("file : " +  path);

				BufferedReader br = null;
				FileReader fr = null;

				try {
					fr = new FileReader( file.getCanonicalPath());
					br = new BufferedReader(fr);


					String line;
					line = br.readLine();
					for ( ; line != null ; line = br.readLine()){
						// yyyyMMdd-hhmmss,rfshop|idrc|drc|sdrc| ,광고주 아이디, pcode, auid, price, adid(optional)

							String[] logArray = line.split(",");
							if( logArray.length < 5) continue;

							//if( isClick) {
							if(suffix.startsWith("imp")) {
								if( logArray[1].compareTo("rfshop") != 0) continue;
							}
							else if(suffix.startsWith("conv")) {
								if( logArray[1].compareTo("conv") != 0) continue;
							}

							if( logArray[2].compareTo("") == 0 || logArray[3].compareTo("") == 0 || logArray[4].compareTo("") == 0 ) {
								continue;
							}
							if( logArray[2].compareTo("null") == 0 || logArray[3].compareTo("null") == 0 || logArray[4].compareTo("null") == 0 ) {
								continue;
							}

							//if( !isClick && logArray[3].compareTo("1") == 0) continue; // there are no pcode(1) in conversion file

						String key = AdvIdWriter.key( logArray[2], strFileSuffix);
						AdvIdWriter advIdWriter = map.get( key);
						if( advIdWriter == null) {
							String basePath = "";
							if(strFileSuffix.startsWith("imp")){
								basePath =  config.getRecomFilePath();
							}else if(strFileSuffix.startsWith("conv")){
								basePath =  config.getConvFilePath();
							}else if(strFileSuffix.startsWith("cart")){
								basePath =  config.getCartFilePath();
							}else if(strFileSuffix.startsWith("clk")){
								basePath =  config.getClkFilePath();
							}

							advIdWriter = new AdvIdWriter(logArray[2], yesterday,  strFileSuffix, basePath);
							map.put( key, advIdWriter);
						}

						//if( isClick) {
						if(suffix.startsWith("imp")) {
							advIdWriter.write(String.format("%s,%s", logArray[3], logArray[4]));
						}else {
							advIdWriter.write(String.format("%s,%s,%s", logArray[3], logArray[4], logArray[0]));
						}
						//if(e[2].equals("davich2")) {
						//	logger.debug(">>>>>>>>>>" + line);
						//}
					}
				}
				catch (Exception e) {
					log.error("", e);
				}
				finally {
					if (br != null) br.close();
					if (fr != null) fr.close();
				}

			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
			System.exit(-1);
		} finally {
			Iterator<AdvIdWriter> it = map.values().iterator();
			while( it.hasNext()) it.next().close();
		}
	}
}
