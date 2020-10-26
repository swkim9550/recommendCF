/*
 * COPYRIGHT (c) Enliple 2019
 * This software is the proprietary of Enliple
 *
 * @author <a href=“mailto:cwpark@enliple.com“>cwpark</a>
 * @since 2020-10-13
 */
package com.enliple.recom3.jpa.db2.service;

import com.enliple.recom3.jpa.db2.domain.AdverPrdtStandardCate;
import com.enliple.recom3.jpa.db2.repository.AdverPrdtStandardCateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * create on 2020-10-13.
 * <p> AI Category 정보를 가져오는 서비스 </p>
 * <p> {@link } and {@link }관련 클래스 </p>
 *
 * @author cwpark
 * @version 1.0
 * @see
 * @since 지원하는 자바버전 8
 */
@Service
public class AiCategoryServiceImpl implements AiCategoryService {

    @Autowired
    private AdverPrdtStandardCateRepository adverPrdtStandardCateRepository;

    @Override
    public List<AdverPrdtStandardCate> findAdverPrdtStandardCateList() {
        return adverPrdtStandardCateRepository.findAllBy();
    }
}