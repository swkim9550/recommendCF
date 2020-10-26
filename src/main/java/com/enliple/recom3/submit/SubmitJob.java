package com.enliple.recom3.submit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.service.JobListServiceIBCFClickImpl;
import com.enliple.recom3.jpa.db1.service.JobListServiceIBCFConvImpl;
import com.enliple.recom3.jpa.db1.service.JobListServiceUBCFClickImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SubmitJob {

	@Autowired
	private Config config;

	@Autowired
	private JobListServiceIBCFClickImpl jobListServiceIbcf;
	
	@Autowired
	private JobListServiceIBCFConvImpl jobListServiceIbcfConv;
	
	@Autowired
	private JobListServiceUBCFClickImpl jobListServiceUbcf;
		
	public int run(boolean isClick) {
		// delete records
		if(isClick) {
			jobListServiceIbcf.deleteAll();
			jobListServiceUbcf.deleteAll();	
		}else {
			jobListServiceIbcfConv.deleteAll();
		}
		
		// delete previous output
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String year = String.format("%02d", calendar.get(Calendar.YEAR));
		String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
		String day = String.format("%02d", calendar.get(Calendar.DATE));

		if (isClick) {
			File file = new File(String.format("%s/%s", config.getOutClickPath(), day));
			if (file.exists()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
			file.mkdir();

			file = new File(String.format("%s/%s", config.getOutClickPathForAvgClick(), day));
			if (file.exists()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
			file.mkdir();
		} else {
			File file = new File(String.format("%s/%s", config.getOutConvPath(), day));
			if (file.exists()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
			file.mkdir();

			file = new File(String.format("%s/%s", config.getOutConvPathForAvgClick(), day));
			if (file.exists()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
			file.mkdir();
		}

		HashMap<String, JobInfo> map = new HashMap<>();

		for (int i = 0; i < config.getPeriod(); i++) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			year = String.format("%02d", calendar.get(Calendar.YEAR));
			month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
			day = String.format("%02d", calendar.get(Calendar.DATE));
			fetchAdvId(year+month+day, map, isClick);
		}

		// sort
		List<JobInfo> jobInfoList = new ArrayList<JobInfo>(map.values());
		Collections.sort(jobInfoList, new Comparator<JobInfo>() {
			public int compare(JobInfo o1, JobInfo o2) {
				return (o2.getSize() <= o1.getSize()) ? -1 : 1;
				// return (int)(o2.getSize() - o1.getSize());
			}
		});

		
//		if(isClick) {
//			DBManager.insertRecord(jobInfoList,"ibcf_click");
//			DBManager.insertRecord(jobInfoList,"ubcf_click");		
//		}else {
//			DBManager.insertRecord(jobInfoList,"ibcf_conv");
//		}
		
		if(isClick) {
			jobListServiceIbcf.insertRecord(jobInfoList);
			jobListServiceUbcf.insertRecord(jobInfoList);
		}else {
			jobListServiceIbcfConv.insertRecord(jobInfoList);
		}
		
		
		log.info("Insert " + jobInfoList.size() + " jobs on DB");

		return jobInfoList.size();
	}

	public void fetchAdvId(String day, HashMap<String, JobInfo> map, boolean isClick) {
		HashMap<String, String> advIdMap = new HashMap();
		for (String advId : config.getAdvIdList()) {
			if (advId.compareTo("*") == 0)
				continue;
			advIdMap.put(advId, advId);
		}

		try {
			boolean isPc = false;

			String filePath = String.format("%s/%s", isClick ? config.getRecomFilePath() : config.getConvFilePath(),
					day);
			File files = new File(filePath);
			if (files == null || files.listFiles() == null) {
				log.info("Not found " + filePath + " directory");
				return;
			}

			log.info("fetch files for " + filePath);

			for (File file : files.listFiles()) {
				String path = file.getCanonicalPath();

				int sidx = path.lastIndexOf(File.separator);
				int eidx = path.lastIndexOf('.');
				String advId = path.substring(sidx + 1, eidx);

				if (advIdMap.size() != 0 && advIdMap.get(advId) == null)
					continue;

				String suffix = path.substring(path.lastIndexOf(".") + 1);

				if (isClick) {
					if (suffix.compareTo("impp") == 0)
						isPc = true;
					else
						isPc = false;
				} else {
					if (suffix.compareTo("convp") == 0)
						isPc = true;
					else
						isPc = false;
				}

				// log.info("block size : " + fileStatus.getLen());

				String key = String.format("%s%s", advId, isPc);
				JobInfo jobInfo = map.get(key);
				if (jobInfo != null) {
					//jobInfo.addSize(file.length());
					jobInfo.setSize(file.length());
				} else {
					//jobInfo = new JobInfo(advId, isPc, file.length());					
					JobInfoKey jobInfoKey = new JobInfoKey(advId, isPc?"Y":"N");
					jobInfo = new JobInfo();
					jobInfo.setKey(jobInfoKey);
					jobInfo.setSize(file.length());
					jobInfo.setStatus("N");
					map.put(key, jobInfo);
				}
			}

		} catch (FileNotFoundException e) {
			log.error("", e);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
			System.exit(-1);
		}
	}

}
