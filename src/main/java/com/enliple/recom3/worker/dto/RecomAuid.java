package com.enliple.recom3.worker.dto;

import lombok.Data;

@Data
public class RecomAuid {
	private short count;
	private int   auid;
	
	private int occurenceValue;
	private double cosine;
	
	public RecomAuid( int auid, short count) {
		this.auid =  auid;
		this.count = count;
	}
	
	public RecomAuid( int auid, int occurenceValue, double cosine) {
		this.auid = auid;
		this.occurenceValue = occurenceValue;
		this.cosine = cosine;
	}
	public String toString() {
		return String.format("{auid=%s, co_cnt:%s, cosine=%.6f}",auid,occurenceValue,cosine); 
	}
	
/*	
	public void increase() {
		this.count++;
	}
*/	

}
