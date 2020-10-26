package com.enliple.recom3.worker.dto;

import lombok.Data;

@Data
public class RecomPcode {
	private short count;
	private String pcode;
	
	private int occurenceValue;
	private double cosine;
	
	public RecomPcode( String pcode, short count) {
		this.pcode = pcode;
		this.count = count;
	}
	
	public RecomPcode( String pcode, int occurenceValue, double cosine) {
		this.pcode = pcode;
		this.occurenceValue = occurenceValue;
		this.cosine = cosine;
	}
	public String toString() {
		return String.format("{pcode:%s, co_cnt:%s, cosine:%.6f}",pcode,occurenceValue,cosine);
	}

}
