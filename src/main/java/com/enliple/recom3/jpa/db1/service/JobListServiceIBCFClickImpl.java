package com.enliple.recom3.jpa.db1.service;

import java.time.LocalDateTime;
import java.util.List;

import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import com.enliple.recom3.jpa.db1.repository.AdverIdTypeListRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobList;
import com.enliple.recom3.jpa.db1.repository.JobListRepository;

@Component("IBCF_CLICK_JOBLIST_JPA")
public class JobListServiceIBCFClickImpl implements JobListService{

	@Autowired
	private JobListRepository jobListRepo;

	@Autowired
	private AdverIdTypeListRepository adverIdTypeListRepository;

	@Override
	public List<JobInfo> getJobListAll() {
		return jobListRepo.findByStatusOrderBySize("N");
	}

	@Override
	public JobInfo getJobList() {
		// TODO Auto-generated method stub
		return jobListRepo.findTop1ByStatusOrderBySizeDesc("N");
	}

	@Override
	public List<AdverIdByType> getJobListV2All()  {
		return adverIdTypeListRepository.findByType("01");
	}

	@Override
	public List<AdverIdByType> getAIAdverIdList() {
		return adverIdTypeListRepository.findByType("02");
	}

	@Override
	public void updateStatus(JobInfo jobInfo, String status) {
		String advId = jobInfo.getAdvId();
		String isPc = jobInfo.isPc()?"Y":"N";
		JobInfoKey key = new JobInfoKey(advId,isPc);
		JobList jobList = jobListRepo.findByKey(key);

		if(jobList==null) {
			jobList = new JobList();
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
		jobListRepo.save(jobList);
	}

	public void deleteAll() {
		jobListRepo.deleteAll();
	}

	public void insertRecord(List<JobInfo> jobInfoList) {
		for(JobInfo jobInfo : jobInfoList) {
			JobList jobList = new JobList();
			jobList.setKey(jobInfo.getKey());
			jobList.setSize(jobInfo.getSize());
			jobList.setStatus(jobInfo.getStatus());
			jobListRepo.save(jobList);
		}
	}

}
