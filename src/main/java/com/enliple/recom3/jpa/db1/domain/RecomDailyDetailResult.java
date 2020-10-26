package com.enliple.recom3.jpa.db1.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name="RECOM_DAILY_DATA_DETAIL_STATS")
public class RecomDailyDetailResult implements Serializable {

    @Builder
    public RecomDailyDetailResult(RecomDailyDetailResultKey key,String UBCF_CNT, String CFSC_CNT, String CFOC_CNT, LocalDateTime createTime) {
        this.key = key;
        this.UBCF_CNT = UBCF_CNT;
        this.CFSC_CNT = CFSC_CNT;
        this.CFOC_CNT = CFOC_CNT;
        this.REG_DTTM = createTime;
        this.ALT_DTTM = createTime;
    }
    public RecomDailyDetailResult() {

    }

    @EmbeddedId
    private RecomDailyDetailResultKey key;

    @Column(name="UBCF_CNT_DETAIL")
    private String UBCF_CNT;

    @Column(name="CFSC_CNT_DETAIL")
    private String CFSC_CNT;

    @Column(name="CFOC_CNT_DETAIL")
    private String CFOC_CNT;

    @Column(name="REG_DTTM")
    private LocalDateTime REG_DTTM;

    @Column(name="ALT_DTTM")
    private LocalDateTime ALT_DTTM;

}
