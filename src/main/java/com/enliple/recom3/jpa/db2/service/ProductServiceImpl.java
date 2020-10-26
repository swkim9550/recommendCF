package com.enliple.recom3.jpa.db2.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enliple.recom3.common.constants.ConstantsCommon;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.jpa.db2.domain.AdverPrdtCateInfo;
import com.enliple.recom3.jpa.db2.repository.AdverPrdtCateInfoRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enliple.recom3.jpa.db2.domain.ComShopData;
import com.enliple.recom3.jpa.db2.repository.MobShopDataRepository;
import com.enliple.recom3.jpa.db2.repository.ShopDataRepository;
import com.enliple.recom3.worker.dto.ProductDto;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private MobShopDataRepository mobShopDataRepository;
	@Autowired
	private ShopDataRepository shopDataRepository;
	@Autowired
	private AdverPrdtCateInfoRepository adverPrdtCateInfoRepository;
	@Autowired
	private RedisCluster redisCluster;

	@Override
	public HashMap<String, ProductDto> fetchAllProductCategory(String advId){
		List<ComShopData> mobShopDataList = mobShopDataRepository.findByUserId(advId);
		HashMap<String, ProductDto> productMap = getProductDtoMap(mobShopDataList, 2);

		List<ComShopData> shopDataList = shopDataRepository.findByUserId(advId);
		productMap.putAll(getProductDtoMap(shopDataList, 1));

		return productMap;
	}

	@Override
	public HashMap<String, ProductDto> fetchAllProductCategory(String advId, boolean isPc){
		List<ComShopData> shopDataList = new ArrayList<ComShopData>();

		if(isPc) {
			shopDataList = shopDataRepository.findByUserId(advId);
		}else {
			shopDataList = mobShopDataRepository.findByUserId(advId);
		}

		int is_Pc = isPc ? 1 : 2;
		HashMap<String, ProductDto> productMap = getProductDtoMap(shopDataList, is_Pc);

		return productMap;
	}

	private HashMap<String, ProductDto> getProductDtoMap(List<ComShopData> comShopDataList, int isPc) {
		HashMap<String, ProductDto> productDtoMap = new HashMap<String, ProductDto>();
		for (ComShopData comShopData : comShopDataList) {
			String category = comShopData.getCate4();
			String CATE1 = comShopData.getCate1();
			String CATE2 = comShopData.getCate2();
			String CATE3 = comShopData.getCate3();

			if("".equals(category)) {
				category = CATE3;
			}
			if("".equals(category)) {
				category = CATE2;
			}
			if("".equals(category)) {
				category = CATE1;
			}
			ProductDto productDto = new ProductDto(category, isPc);
			productDtoMap.put(comShopData.getPcode(), productDto);
		}

		return productDtoMap;
	}

	@Override
	public HashMap<String, ProductDto> fetchAllProductAiCategory(String advId, boolean isPc){
		List<AdverPrdtCateInfo> adverPrdtCateInfoList = new ArrayList<AdverPrdtCateInfo>();

		adverPrdtCateInfoList = adverPrdtCateInfoRepository.findAdverPrdtCateInfoList(advId);

		int is_Pc = isPc ? 1 : 2;
		HashMap<String, ProductDto> productMap = getAiProductDtoMap(adverPrdtCateInfoList, is_Pc);

		return productMap;
	}

	private HashMap<String, ProductDto> getAiProductDtoMap(List<AdverPrdtCateInfo> adverPrdtCateInfoList, int isPc) {
		HashMap<String, ProductDto> productDtoMap = new HashMap<String, ProductDto>();
		for (AdverPrdtCateInfo adverPrdtCateInfo : adverPrdtCateInfoList) {
			String category = adverPrdtCateInfo.getAdverCateNo();
			ProductDto productDto = new ProductDto(category, isPc);
			productDto.setUpperCategory(makeUpperCategory(adverPrdtCateInfo));
			productDtoMap.put(adverPrdtCateInfo.getProductCode(), productDto);
		}
		return productDtoMap;
	}

	private String makeUpperCategory(AdverPrdtCateInfo adverPrdtCateInfo) {
		String cate1 = adverPrdtCateInfo.getCate1().replaceAll(ConstantsCommon.SPACE_REGEX, ConstantsCommon.EMPTY_STRING);
		String cate2 = adverPrdtCateInfo.getCate2().replaceAll(ConstantsCommon.SPACE_REGEX, ConstantsCommon.EMPTY_STRING);
		String cate3 = adverPrdtCateInfo.getCate3().replaceAll(ConstantsCommon.SPACE_REGEX, ConstantsCommon.EMPTY_STRING);
		String cate4 = adverPrdtCateInfo.getCate4().replaceAll(ConstantsCommon.SPACE_REGEX, ConstantsCommon.EMPTY_STRING);

		// AI에서 만드는 카테고리는 카테고리3까지는 필수로 만들어짐
		if(ConstantsCommon.EMPTY_STRING.equals(cate4)) { // 카테고리4가 빈값이면 카테고리3을 제외한 값만을 상위카테고리로 판단
			return cate1+cate2;
		} else {
			return cate1+cate2+cate3;
		}
	}
}
