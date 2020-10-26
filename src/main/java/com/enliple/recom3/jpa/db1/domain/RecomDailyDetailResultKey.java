package com.enliple.recom3.jpa.db1.domain;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class RecomDailyDetailResultKey implements Serializable {
    @Column(name = "STATS_DTTM")
    private int STATS_DTTM;

    @Column(name = "ADVER_ID")
    private String ADVER_ID;

    @Column(name = "IS_PC")
    private String IS_PC;

    @Column(name = "RECOM_TYPE")
    private String RECOM_TYPE;

    public RecomDailyDetailResultKey() {

    }
    @Builder
    public RecomDailyDetailResultKey(String advId, int datetime, String ispc,String recomType) {
        this.STATS_DTTM = datetime;
        this.ADVER_ID = advId;
        this.IS_PC = ispc;
        this.RECOM_TYPE = recomType;
    }
}
