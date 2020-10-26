package com.enliple.recom3.jpa.db1.service;

import com.enliple.recom3.jpa.db1.domain.RecomDailyResultKey;
import com.enliple.recom3.jpa.db1.repository.RecomDailyResultRepository;
import com.enliple.recom3.worker.dto.RecomCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db1.domain.RecomDailyResult;

import java.time.LocalDateTime;

@Service("RECOM_DAILY_DATA_STATS_JPA")
public class RecomDailyResultServiceIBCFClickimpl implements RecomDailyResultService {

    @Autowired
    private RecomDailyResultRepository recomDailyResultRepo;

    @Override
    public void insertRecord(RecomCount auidRecomCountInfo) {
        RecomDailyResult recomDailyResult = RecomDailyResult.builder().key(new RecomDailyResultKey(auidRecomCountInfo.getADVER_ID(),auidRecomCountInfo.getSTATS_DTTM(),auidRecomCountInfo.isIS_PC()?"Y":"N",auidRecomCountInfo.getRECOM_TYPE()))
                                                                        .UBCF_CNT(auidRecomCountInfo.getUBCF_CNT())
                                                                        .CFSC_CNT(auidRecomCountInfo.getCFSC_CNT())
                                                                        .CFOC_CNT(auidRecomCountInfo.getCFOC_CNT())
                                                                        .createTime(LocalDateTime.now())
                                                                        .createTime(LocalDateTime.now()).build();
        recomDailyResultRepo.save(recomDailyResult);
    }
}
