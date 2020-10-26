package com.enliple.recom3.jpa.db1.service;

import com.enliple.recom3.jpa.db1.domain.RecomDailyDetailResultKey;
import com.enliple.recom3.jpa.db1.domain.RecomDailyDetailResult;
import com.enliple.recom3.jpa.db1.repository.RecomDailyDetailResultRepository;
import com.enliple.recom3.worker.dto.RecomCountDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("RECOM_DAILY_DATA_DETAIL_STATS_JPA")
public class RecomDailyDetailResultServiceIBCFClickimpl implements RecomDailyDetailResultService {


    @Autowired
    private RecomDailyDetailResultRepository recomDailyDetailResultRepo;

    @Override
    public void insertRecord(RecomCountDetail recomCountDetail) {
        RecomDailyDetailResult recomDailyDetailResultKey = RecomDailyDetailResult.builder().key(new RecomDailyDetailResultKey(recomCountDetail.getADVER_ID(),recomCountDetail.getSTATS_DTTM(),recomCountDetail.isIS_PC()?"Y":"N",recomCountDetail.getRECOM_TYPE()))
                .UBCF_CNT(recomCountDetail.getUBCF_CNT_DETAIL())
                .CFSC_CNT(recomCountDetail.getCFSC_CNT_DETAIL())
                .CFOC_CNT(recomCountDetail.getCFOC_CNT_DETAIL())
                .createTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();

        recomDailyDetailResultRepo.save(recomDailyDetailResultKey);
    }
}
