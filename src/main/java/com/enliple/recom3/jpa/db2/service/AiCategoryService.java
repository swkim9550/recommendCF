package com.enliple.recom3.jpa.db2.service;

import com.enliple.recom3.jpa.db2.domain.AdverPrdtCateInfo;
import com.enliple.recom3.jpa.db2.domain.AdverPrdtStandardCate;

import java.util.List;

public interface AiCategoryService {

    public List<AdverPrdtStandardCate> findAdverPrdtStandardCateList();
}
