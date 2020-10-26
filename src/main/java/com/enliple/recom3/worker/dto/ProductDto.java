package com.enliple.recom3.worker.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ProductDto {
	public ProductDto(String category, int isPc) {
		this.category = category;
		this.isPc = isPc;
	}
	int isPc = 0;
	//String pcode;
	String category;
	String upperCategory;
}
