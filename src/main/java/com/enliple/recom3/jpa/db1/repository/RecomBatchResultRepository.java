package com.enliple.recom3.jpa.db1.repository;

import com.enliple.recom3.jpa.db1.domain.RecomBatchResult;
import com.enliple.recom3.jpa.db1.domain.RecomBatchResultKey;
import org.springframework.data.repository.CrudRepository;

public interface RecomBatchResultRepository extends CrudRepository<RecomBatchResult, Long> {
    RecomBatchResult findByKey(RecomBatchResultKey recomBatchResultKey);
}
