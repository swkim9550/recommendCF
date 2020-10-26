package com.enliple.recom3.jpa.db1.repository;

import com.enliple.recom3.jpa.db1.domain.RecomDailyDetailResult;
import com.enliple.recom3.jpa.db1.domain.RecomDailyDetailResultKey;
import org.springframework.data.repository.CrudRepository;

public interface RecomDailyDetailResultRepository extends CrudRepository<RecomDailyDetailResult, Long> {
    RecomDailyDetailResult findByKey(RecomDailyDetailResultKey recomDailyDetailResultKey);
}
