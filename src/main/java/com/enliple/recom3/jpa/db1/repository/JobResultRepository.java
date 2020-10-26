package com.enliple.recom3.jpa.db1.repository;

import org.springframework.data.repository.CrudRepository;

import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobResult;

public interface JobResultRepository extends CrudRepository<JobResult, Long>{

	JobResult findByKey(JobInfoKey jobListId);
}
