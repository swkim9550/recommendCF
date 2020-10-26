package com.enliple.recom3.jpa.db1.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.StringUtils;

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
@Table(name="RECOM_DAILY_DATA_STATS")
public class RecomDailyResult implements Serializable {

    @Builder
    public RecomDailyResult(RecomDailyResultKey key,long UBCF_CNT, long CFSC_CNT, long CFOC_CNT, LocalDateTime createTime) {
        this.key = key;
        this.UBCF_CNT = UBCF_CNT;
        this.CFSC_CNT = CFSC_CNT;
        this.CFOC_CNT = CFOC_CNT;
        this.REG_DTTM = createTime;
        this.ALT_DTTM = createTime;
    }
    public RecomDailyResult() {

    }

    @EmbeddedId
    private RecomDailyResultKey key;

    @Column(name="UBCF_CNT")
    private long UBCF_CNT;

    @Column(name="CFSC_CNT")
    private long CFSC_CNT;

    @Column(name="CFOC_CNT")
    private long CFOC_CNT;

    @Column(name="REG_DTTM")
    private LocalDateTime REG_DTTM;

    @Column(name="ALT_DTTM")
    private LocalDateTime ALT_DTTM;
}
