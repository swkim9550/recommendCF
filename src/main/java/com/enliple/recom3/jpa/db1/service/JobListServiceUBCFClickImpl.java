package com.enliple.recom3.jpa.db1.service;

import java.time.LocalDateTime;
import java.util.List;

import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobListUbcf;
import com.enliple.recom3.jpa.db1.repository.JobListUbcfRepository;

@Service("UBCF_CLICK_JOBLIST_JPA")
public class JobListServiceUBCFClickImpl implements JobListService{

	@Autowired
	private JobListUbcfRepository jobListUbcfRepo;

	@Override
	public List<JobInfo> getJobListAll() {
		return jobListUbcfRepo.findByStatusOrderBySizeDesc("N");
	}

	@Override
	public JobInfo getJobList() {
		// TODO Auto-generated method stub		
		return jobListUbcfRepo.findTop1ByStatusOrderBySize("N");
	}

	@Override
	public List<AdverIdByType> getJobListV2All() {
		return null;
	}

	@Override
	public List<AdverIdByType> getAIAdverIdList() {
		return null;
	}

	@Override
	public void updateStatus( JobInfo jobInfo, String status) {
		String advId = jobInfo.getAdvId();
		String isPc = jobInfo.isPc()?"Y":"N";
		JobInfoKey key = new JobInfoKey(advId,isPc);
		JobListUbcf jobList = jobListUbcfRepo.findByKey(key);

		if(jobList==null) {
			jobList = new JobListUbcf();
			jobList.setKey(key);
		}

		if(StringUtils.equals(status, "E") && jobList.getCreateDate()==null) {
			jobList.setCreateDate(LocalDateTime.now());
		}

		if(StringUtils.equals(status, "R") || StringUtils.equals(status, "Q")) {//실행중이거나 queue에 대기중이거나
			jobList.setCreateDate(LocalDateTime.now());
		}else {
			jobList.setUpdateDate(LocalDateTime.now());;
		}

		jobList.setStatus(status);
		jobListUbcfRepo.save(jobList);
	}

	public void deleteAll() {
		jobListUbcfRepo.deleteAll();
	}

	public void insertRecord(List<JobInfo> jobInfoList) {
		for(JobInfo jobInfo : jobInfoList) {
			JobListUbcf jobList = new JobListUbcf();
			jobList.setKey(jobInfo.getKey());
			jobList.setSize(jobInfo.getSize());
			jobList.setStatus(jobInfo.getStatus());
			jobListUbcfRepo.save(jobList);
		}
	}
}
