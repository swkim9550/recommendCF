package com.enliple.recom3.jpa.db1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db1.domain.JobResultKey;
import com.enliple.recom3.jpa.db1.domain.JobResultUbcf;
import com.enliple.recom3.jpa.db1.repository.JobResultUbcfRepository;
import com.enliple.recom3.worker.dto.JobResultInfo;

@Service("UBCF_CLICK_JOBRESULT_JPA")
public class JobResultServiceUBCFClickImpl implements JobResultService{

	@Autowired
	private JobResultUbcfRepository jobResultUbcfRepo;

	@Override
	public void insertRecord(JobResultInfo jobResultinfo) {
		JobResultUbcf jobResult = JobResultUbcf.builder().key(new JobResultKey(jobResultinfo.getAdvId(),(jobResultinfo.isPc()?"Y":"N"),jobResultinfo.getDateTime()))
				.auidCount(jobResultinfo.getAuidCount())
				.pcodeCount(jobResultinfo.getPcodeCount())
				.createTime(LocalDateTime.now()).build();	
		jobResultUbcfRepo.save(jobResult);			
	}
}
