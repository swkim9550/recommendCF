package com.enliple.recom3.worker.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RecomCount {
    private int STATS_DTTM;
    private String ADVER_ID;
    private boolean IS_PC;
    private String RECOM_TYPE;
    private long UBCF_CNT;
    private long CFSC_CNT;
    private long CFOC_CNT;
    private LocalDateTime REG_DTTM;
    private LocalDateTime ALT_DTTM;

    @Builder
    public RecomCount(int dateTime, String advId, boolean pc,String recomType, long advIdUbcfCount, long advIdCrscCount, long advIdCrocCount, LocalDateTime createTime) {
        this.STATS_DTTM = dateTime;
        this.ADVER_ID = advId;
        this.IS_PC = pc;
        this.RECOM_TYPE = recomType;
        this.UBCF_CNT = advIdUbcfCount;
        this.CFSC_CNT = advIdCrscCount;
        this.CFOC_CNT = advIdCrocCount;
        this.REG_DTTM = createTime;
        this.ALT_DTTM = createTime;
    }
}
