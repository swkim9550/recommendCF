package com.enliple.recom3.jpa.db1.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobList;

public interface JobListRepository extends CrudRepository<JobList, Long>{

	JobList findByKey(JobInfoKey jobListId);
	
	JobInfo findTop1ByStatusOrderBySizeDesc(String status);
	
	List<JobInfo> findByStatusOrderBySize(String status);

	List<JobInfo> findByStatusOrderBySizeDesc(String status);
}
