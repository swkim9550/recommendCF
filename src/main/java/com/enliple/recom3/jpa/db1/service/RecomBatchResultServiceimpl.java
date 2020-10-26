package com.enliple.recom3.jpa.db1.service;

import com.enliple.recom3.jpa.db1.domain.RecomBatchResult;
import com.enliple.recom3.jpa.db1.domain.RecomBatchResultKey;
import com.enliple.recom3.jpa.db1.repository.RecomBatchResultRepository;
import com.enliple.recom3.worker.dto.RecomBatchResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("RECOM_BATCH_RESULT_JPA")
public class RecomBatchResultServiceimpl implements RecomBatchResultService {

    @Autowired
    private RecomBatchResultRepository recomBatchResultRepository;

    @Override
    public void insertRecord(RecomBatchResultDto recomBatchResultDto) {

        RecomBatchResult recomBatchResult = RecomBatchResult.builder().key(new RecomBatchResultKey(
                recomBatchResultDto.getSTATS_DTTM(),recomBatchResultDto.getADVER_ID(),recomBatchResultDto.isIS_PC()?"Y":"N",recomBatchResultDto.getRECOM_TYPE()))
                .BATCH_STATUS(recomBatchResultDto.getBATCH_STATUS())
                .PCODE_AUID_MAP(recomBatchResultDto.getPCODE_AUID_MAP())
                .AUID_PCODE_MAP(recomBatchResultDto.getAUID_PCODE_MAP())
                .OCCURENCE_DATA_MAP(recomBatchResultDto.getOCCURENCE_DATA_MAP())
                .PRODUCT_MAP(recomBatchResultDto.getPRODUCT_MAP())
                .REG_DTTM(LocalDateTime.now())
                .ALT_DTTM(LocalDateTime.now()).build();
        recomBatchResultRepository.save(recomBatchResult);
    }
}
