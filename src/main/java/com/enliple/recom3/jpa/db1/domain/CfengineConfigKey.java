package com.enliple.recom3.jpa.db1.domain;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class CfengineConfigKey implements Serializable {
    @Column(name = "PROPERTIES_NAME")
    private String propertiesName;

    @Column(name = "PROPERTIES_VALUE")
    private String propertiesValue;

    @Column(name = "ADVER_ID")
    private String adverId;

    public CfengineConfigKey(){

    }
    @Builder
    public CfengineConfigKey(String propertiesName,String propertiesValue,String adverId) {
        this.propertiesName = propertiesName;
        this.propertiesValue = propertiesValue;
        this.adverId = adverId;
    }
}
