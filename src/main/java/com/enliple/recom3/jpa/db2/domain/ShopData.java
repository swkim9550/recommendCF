package com.enliple.recom3.jpa.db2.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="SHOP_DATA")
public class ShopData extends ComShopData implements Serializable {
	
	@Id
	@Column(name="NO",updatable=false)
	private String no;
	
	@Column(name="USERID",updatable=false)
	private String userId;
	
	@Column(name="PCODE",updatable=false)
	private String pcode;
	
	@Column(name="CATE1",updatable=false)
	private String cate1;
	
	@Column(name="CATE2",updatable=false)
	private String cate2;
	
	@Column(name="CATE3",updatable=false)
	private String cate3;
	
	@Column(name="CATE4",updatable=false)
	private String cate4;
	
}
