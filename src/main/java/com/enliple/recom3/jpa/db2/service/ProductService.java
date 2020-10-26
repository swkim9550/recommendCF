package com.enliple.recom3.jpa.db2.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enliple.recom3.jpa.db2.domain.AdverPrdtCateInfo;
import com.enliple.recom3.worker.dto.ProductDto;

public interface ProductService {

	public HashMap<String, ProductDto> fetchAllProductCategory(String advId);
	public HashMap<String, ProductDto> fetchAllProductCategory(String advId, boolean is_Pc);
	public HashMap<String, ProductDto> fetchAllProductAiCategory(String advId, boolean is_Pc);
}
