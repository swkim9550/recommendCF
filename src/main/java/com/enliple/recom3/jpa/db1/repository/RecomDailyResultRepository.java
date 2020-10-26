package com.enliple.recom3.jpa.db1.repository;

import com.enliple.recom3.jpa.db1.domain.RecomDailyResult;
import com.enliple.recom3.jpa.db1.domain.RecomDailyResultKey;
import org.springframework.data.repository.CrudRepository;

public interface RecomDailyResultRepository extends CrudRepository<RecomDailyResult, Long> {
    RecomDailyResult findByKey(RecomDailyResultKey recomDailyResultKey);
}
