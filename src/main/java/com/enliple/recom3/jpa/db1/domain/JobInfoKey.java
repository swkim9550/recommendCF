package com.enliple.recom3.jpa.db1.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class JobInfoKey implements Serializable {
    @Column(name = "advId")
    private String advId;
 
    @Column(name = "is_pc")
    private String isPc;
    
    public JobInfoKey() {
    	
    }
    
    public JobInfoKey(String advId,String isPc) {
    	this.advId = advId;
    	this.isPc = isPc;
    }
}
