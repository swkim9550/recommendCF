/*
 * COPYRIGHT (c) Enliple 2019
 * This software is the proprietary of Enliple
 *
 * @author <a href=“mailto:cwpark@enliple.com“>cwpark</a>
 * @since 2020-10-13
 */
package com.enliple.recom3.worker;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.jpa.db2.domain.AdverPrdtStandardCate;
import com.enliple.recom3.jpa.db2.service.AiCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * create on 2020-10-13.
 * <p> 클래스 설명 </p>
 * <p> {@link } and {@link }관련 클래스 </p>
 *
 * @author cwpark
 * @version 1.0
 * @see
 * @since 지원하는 자바버전 (ex : 5+ 5이상)
 */
@Component
@Slf4j
public class AiCateMaker {

    private final int EXPIRE_TIME = 60 * 60 * 24 * 2; // 2일

    @Autowired
    private Config config;
    @Autowired
    private AiCategoryService aiCategoryService;
    @Autowired
    private RedisCluster redisCluster;

    public void getAiCategory() {
        log.info("Get AI Category Start.");
        Long startTime = System.currentTimeMillis();
        List<AdverPrdtStandardCate> adverPrdtStandardCateList = aiCategoryService.findAdverPrdtStandardCateList();
        String redisKeyPreFix = ConstantsIBCF.IBCF_TYPE_AI_CATE + ConstantsIBCF.SPLIT_CODE;
        for (AdverPrdtStandardCate adverPrdtStandardCate : adverPrdtStandardCateList) {
            StringBuilder saveValue = new StringBuilder();
            saveValue.append(adverPrdtStandardCate.getCate1());
            saveValue.append(appendValue(adverPrdtStandardCate.getCate2()));
            saveValue.append(appendValue(adverPrdtStandardCate.getCate3()));
            saveValue.append(appendValue(adverPrdtStandardCate.getCate4()));
            redisCluster.saveRedis(redisKeyPreFix + adverPrdtStandardCate.getNo(), saveValue.toString(), EXPIRE_TIME);
            log.info("AI Category {} key saved.", adverPrdtStandardCate.getNo());
        }
        log.info("Get AI Category End. ({}sec)", (System.currentTimeMillis()-startTime)/1000);
    }

    private String appendValue(String value) {
        if(StringUtils.isNotEmpty(value)) {
            return ConstantsIBCF.APPEND_CODE + value.replaceAll(" ", "");
        } else {
            return value;
        }
    }

}