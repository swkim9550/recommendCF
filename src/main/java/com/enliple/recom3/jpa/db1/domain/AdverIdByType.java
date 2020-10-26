package com.enliple.recom3.jpa.db1.domain;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="RECOM_ADVER_ID_TYPE_LIST")
public class AdverIdByType implements Serializable {
    //이 생성자가 없으면 JPA에서 에러발생
    public AdverIdByType() {

    }

    @EmbeddedId
    private AdverIdByTypeKey key;

    @Column(name="REG_DTTM")
    private LocalDateTime REG_DTTM;

    @Column(name="ALT_DTTM")
    private LocalDateTime ALT_DTTM;
}
