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
@Table(name="CFENGINE_PROPERTIES")
public class CfengineConfig implements Serializable {
    @EmbeddedId
    private CfengineConfigKey key;

    @Column(name="PROPERTIES_COMENT")
    private String propertiesComents;

    @Column(name="REG_DTTM")
    private LocalDateTime REG_DTTM;

    @Column(name="ALT_DTTM")
    private LocalDateTime ALT_DTTM;

    public CfengineConfig() {

    }
    @Builder
    public CfengineConfig(CfengineConfigKey key, String propertiesComents, LocalDateTime REG_DTTM, LocalDateTime ALT_DTTM) {
        this.key = key;
        this.propertiesComents = propertiesComents;
        this.REG_DTTM = REG_DTTM;
        this.ALT_DTTM = ALT_DTTM;
    }
}
