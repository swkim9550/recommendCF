package com.enliple.recom3.jpa.db2.repository;

import com.enliple.recom3.jpa.db2.domain.AdverPrdtStandardCate;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AdverPrdtStandardCateRepository extends Repository<AdverPrdtStandardCate, Long>{
	
	List<AdverPrdtStandardCate> findAllBy();
	
}
