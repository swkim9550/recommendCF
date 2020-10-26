package com.enliple.recom3.worker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RecomBatchResultDto {
    private int STATS_DTTM;
    private String ADVER_ID;
    private boolean IS_PC;
    private String RECOM_TYPE;
    private String BATCH_STATUS;
    private long PCODE_AUID_MAP;
    private long AUID_PCODE_MAP;
    private long OCCURENCE_DATA_MAP;
    private long PRODUCT_MAP;
    private LocalDateTime REG_DTTM;
    private LocalDateTime ALT_DTTM;

    @Builder
    public RecomBatchResultDto(int dateTime, String adverId, boolean is_pc, String recomType, String batchStatus, long pcodeAuidMap, long auidPcodeMap, long occurenceDataMap,long productMap, LocalDateTime createTime) {
        this.STATS_DTTM = dateTime;
        this.ADVER_ID = adverId;
        this.IS_PC = is_pc;
        this.RECOM_TYPE = recomType;
        this.BATCH_STATUS = batchStatus;
        this.PCODE_AUID_MAP = pcodeAuidMap;
        this.AUID_PCODE_MAP = auidPcodeMap;
        this.OCCURENCE_DATA_MAP = occurenceDataMap;
        this.PRODUCT_MAP = productMap;
        this.REG_DTTM = createTime;
        this.ALT_DTTM = createTime;



    }
}
