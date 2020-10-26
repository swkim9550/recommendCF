package com.enliple.recom3.jpa.db1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db1.domain.JobResult;
import com.enliple.recom3.jpa.db1.domain.JobResultKey;
import com.enliple.recom3.jpa.db1.repository.JobResultRepository;
import com.enliple.recom3.worker.dto.JobResultInfo;

@Service("IBCF_CLICK_JOBRESULT_JPA")
public class JobResultServiceIBCFClickImpl implements JobResultService{

	@Autowired
	private JobResultRepository jobResultRepo;
	
	@Override
	public void insertRecord(JobResultInfo jobResultinfo) {
		JobResult jobResult = JobResult.builder().key(new JobResultKey(jobResultinfo.getAdvId(),(jobResultinfo.isPc()?"Y":"N"),jobResultinfo.getDateTime()))
										.auidCount(jobResultinfo.getAuidCount())
										.pcodeCount(jobResultinfo.getPcodeCount())
										.createTime(LocalDateTime.now()).build();
		jobResultRepo.save(jobResult);		
	}
}
