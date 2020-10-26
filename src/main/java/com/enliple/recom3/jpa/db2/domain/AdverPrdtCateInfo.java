package com.enliple.recom3.jpa.db2.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@ToString
@Entity
@Table(name="ADVER_PRDT_CATE_INFO")
public class AdverPrdtCateInfo implements Serializable {
	
	@Id
	@Column(name="NO",updatable=false)
	private String no;
	
	@Column(name="ADVER_ID",updatable=false)
	private String adverId;
	
	@Column(name="PRODUCT_CODE",updatable=false)
	private String productCode;
	
	@Column(name="ADVER_CATE_NO",updatable=false)
	private String adverCateNo;

	@Column(name="FIRST_CATE",updatable=false)
	private String cate1;

	@Column(name="SECOND_CATE",updatable=false)
	private String cate2;

	@Column(name="THIRD_CATE",updatable=false)
	private String cate3;

	@Column(name="FOURTH_CATE",updatable=false)
	private String cate4;
}
