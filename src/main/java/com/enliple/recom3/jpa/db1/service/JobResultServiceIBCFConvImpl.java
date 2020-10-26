package com.enliple.recom3.jpa.db1.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db1.domain.JobResultConv;
import com.enliple.recom3.jpa.db1.domain.JobResultKey;
import com.enliple.recom3.jpa.db1.repository.JobResultConvRepository;
import com.enliple.recom3.worker.dto.JobResultInfo;

@Service("IBCF_CONV_JOBRESULT_JPA")
public class JobResultServiceIBCFConvImpl implements JobResultService{

	@Autowired
	private JobResultConvRepository jobResultConvRepo;

	@Override
	public void insertRecord(JobResultInfo jobResultinfo) {
		JobResultConv jobResult = JobResultConv.builder().key(new JobResultKey(jobResultinfo.getAdvId(),(jobResultinfo.isPc()?"Y":"N"),jobResultinfo.getDateTime()))
				.auidCount(jobResultinfo.getAuidCount())
				.pcodeCount(jobResultinfo.getPcodeCount())
				.createTime(LocalDateTime.now()).build();	
		jobResultConvRepo.save(jobResult);					
	}
}
