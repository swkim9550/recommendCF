package com.enliple.recom3.jpa.db1.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobListUbcf;

public interface JobListUbcfRepository extends CrudRepository<JobListUbcf, Long>{

	JobListUbcf findByKey(JobInfoKey jobListId);
	
	JobInfo findTop1ByStatusOrderBySize(String status);
	
	List<JobInfo> findByStatusOrderBySize(String status);
	
	List<JobInfo> findByStatusOrderBySizeDesc(String status);
}
