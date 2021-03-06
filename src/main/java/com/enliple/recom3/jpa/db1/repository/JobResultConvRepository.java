package com.enliple.recom3.jpa.db1.repository;

import org.springframework.data.repository.CrudRepository;

import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.domain.JobResult;
import com.enliple.recom3.jpa.db1.domain.JobResultConv;

public interface JobResultConvRepository extends CrudRepository<JobResultConv, Long>{

	JobResult findByKey(JobInfoKey jobListId);
}
