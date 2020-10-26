package com.enliple.recom3.jpa.db1.domain;

import lombok.Builder;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
@Data
@Embeddable
public class AdverIdByTypeKey implements Serializable {
    @Column(name="ADVER_ID")
    private String ADVER_ID;

    @Column(name="ADVER_TYPE")
    private String ADVER_TYPE;

    public AdverIdByTypeKey() {

    }
    @Builder
    public AdverIdByTypeKey(String advId , String type) {
        this.ADVER_ID = advId;
        this.ADVER_TYPE = type;
    }
}
