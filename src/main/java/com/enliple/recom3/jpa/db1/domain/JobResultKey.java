package com.enliple.recom3.jpa.db1.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Builder;
import lombok.Data;

@Data
@Embeddable
public class JobResultKey implements Serializable {
    @Column(name = "advId")
    private String advId;
 
    @Column(name = "is_pc")
    private String isPc;
    
    @Column(name = "datetime")
    private int datetime;
    
    public JobResultKey() {
    	
    }
    @Builder
    public JobResultKey(String advId,String isPc,int datetime) {
    	this.advId = advId;
    	this.isPc = isPc;
    	this.datetime = datetime;
    }
}
