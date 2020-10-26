package com.enliple.recom3.jpa.db1.domain;

import com.enliple.recom3.jpa.db1.domain.JobResultKey;
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
@Table(name="RECOM_BATCH_RESULT")
public class RecomBatchResult implements Serializable {
    @Builder
    public RecomBatchResult(RecomBatchResultKey key, String BATCH_STATUS, long PCODE_AUID_MAP, long AUID_PCODE_MAP, long OCCURENCE_DATA_MAP,long PRODUCT_MAP,LocalDateTime REG_DTTM,LocalDateTime ALT_DTTM) {
        this.key = key;
        this.BATCH_STATUS = BATCH_STATUS;
        this.PCODE_AUID_MAP = PCODE_AUID_MAP;
        this.AUID_PCODE_MAP = AUID_PCODE_MAP;
        this.OCCURENCE_DATA_MAP = OCCURENCE_DATA_MAP;
        this.PRODUCT_MAP = PRODUCT_MAP;
        this.REG_DTTM = REG_DTTM;
        this.ALT_DTTM = ALT_DTTM;
    }

    //이 생성자가 없으면 JPA에서 에러발생
    public RecomBatchResult() {

    }

    @EmbeddedId
    private RecomBatchResultKey key;

    @Column(name="BATCH_STATUS")
    private String BATCH_STATUS;

    @Column(name="PCODE_AUID_MAP")
    private long PCODE_AUID_MAP;

    @Column(name="AUID_PCODE_MAP")
    private long AUID_PCODE_MAP;

    @Column(name="OCCURENCE_DATA_MAP")
    private long OCCURENCE_DATA_MAP;

    @Column(name="PRODUCT_MAP")
    private long PRODUCT_MAP;

    @Column(name="REG_DTTM")
    private LocalDateTime REG_DTTM;

    @Column(name="ALT_DTTM")
    private LocalDateTime ALT_DTTM;

}
