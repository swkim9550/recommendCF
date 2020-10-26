package com.enliple.recom3.worker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
public class RecomCountDetail {
    public static final String SPLIT_CODE = ",";
    public static final String SEPERATE_CODE = "|";
    public static final String IBCF_SAME = "_same";
    public static final String IBCF_OTHER = "_other";
    private int STATS_DTTM;
    private String ADVER_ID;
    private boolean IS_PC;
    private String RECOM_TYPE;
    private String UBCF_CNT_DETAIL;
    private String CFSC_CNT_DETAIL;
    private String CFOC_CNT_DETAIL;
    private LocalDateTime REG_DTTM;
    private LocalDateTime ALT_DTTM;
    private Map<String,Integer> countMap;

    @Builder
    public RecomCountDetail(int dateTime, String advId, boolean pc, String recomType,String advIdUbcfDetailCount, String advIdCrscDetailCount, String advIdCrocDetailCount, LocalDateTime createTime, Map<String,Integer> countMap) {

        for(int i= 1 ; i < 19 ; i++){
            if(countMap.containsKey(i+IBCF_SAME)){
                    advIdCrscDetailCount += i +SPLIT_CODE + countMap.get(i+IBCF_SAME) + SEPERATE_CODE;
            }
            if(countMap.containsKey(i+IBCF_OTHER)){
                    advIdCrocDetailCount += i +SPLIT_CODE +countMap.get(i + IBCF_OTHER) + SEPERATE_CODE;
            }
         }

        this.STATS_DTTM = dateTime;
        this.ADVER_ID = advId;
        this.IS_PC = pc;
        this.RECOM_TYPE = recomType;
        this.UBCF_CNT_DETAIL = advIdUbcfDetailCount;
        this.CFSC_CNT_DETAIL = advIdCrscDetailCount;
        this.CFOC_CNT_DETAIL = advIdCrocDetailCount;
        this.REG_DTTM = createTime;
        this.ALT_DTTM = createTime;
    }
}
