package com.enliple.recom3.worker.ibcfconv;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.Resource;

import com.enliple.recom3.category.ReadCategory;
import com.enliple.recom3.common.utils.BatchUtils;
import com.enliple.recom3.elasticsearch.service.ElasticSearchService;
import com.enliple.recom3.worker.RecomMaker;
import com.enliple.recom3.worker.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.common.CustomLoggerFactory;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.service.JobListService;
import com.enliple.recom3.jpa.db1.service.JobResultService;
import com.enliple.recom3.jpa.db2.service.ProductService;
import com.enliple.recom3.worker.dao.RecomData;

import lombok.extern.slf4j.Slf4j;

@Component("IBCF_CONV")
@Slf4j
public class RecomMakerIBCFConv implements RecomMaker {
	
	@Autowired
	private Config config;
	
	@Autowired
	private RedisCluster redisCluster;

	@Autowired
	private ReadCategory readCategory;

	@Autowired
	private ElasticSearchService elasticSearchService;
	
	@Autowired
	private ProductService productService;
	
	@Resource(name = "IBCF_CONV_JOBLIST_JPA")
	private JobListService jobListService;
	
	@Resource(name = "IBCF_CONV_JOBRESULT_JPA")
	private JobResultService jobResultService;
	
	int memUnit = 1024*1024*1024;	
	
	@Override
	public List<JobInfo> getJobListAll() {
		return jobListService.getJobListAll();
	}

	@Override
	public JobInfo getJobList() {
		return jobListService.getJobList();
	}

	@Override
	public List<JobInfo> getAICateJobListAll() {
		return null;
	}

	@Override
	public void updateStatus(JobInfo jobInfo, String status) {
		jobListService.updateStatus(jobInfo, status);
	}
	
	@Override
	public void insertRecomResult(RecomBatchResultDto recomBatchResult) {
	}
	@Override
	public void insertRecomCount(RecomCount recomCount){
		//jobResultinfo.setPcodeCount(resultCount);
		//jobResultService.insertAuidCountRecord(jobResultinfo);
	}

	@Override
	public void insertRecomCountDetail(RecomCountDetail recomCountDetail) {

	}

	@Override
	public RecomData getBaseRecomData(RecomBatchConfig batchConfig, RecomData data, JobInfo jobInfo) {
		boolean click = batchConfig.isClick();
		String mode = batchConfig.getMode();
		int pcPeriod = config.getPeriod();		
		int mobilePeriod = config.getMobilePeriod();
		int period = 0;
		int ibcfPeriod = jobInfo.isPc() ? pcPeriod : mobilePeriod;
		
		String suffix = jobInfo.isPc() ? "convp" : "convm";
		//if(config.getDefaultPeriodMap().get(jobInfo.getAdvId()) != null ) {
		//	period = config.getDefaultPeriodMap().get(jobInfo.getAdvId());
		//}
		
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		for( period = 0; period < ibcfPeriod; ++period) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			String year = String.format("%02d", calendar.get(Calendar.YEAR));
			String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
			String day = String.format("%02d", calendar.get(Calendar.DATE));
			
			String filePath = String.format("%s/%s/%s.%s",
					 click ? config.getRecomFilePath() : config.getConvFilePath(),
					 year+month+day,
					 jobInfo.getAdvId(), 
					 suffix);
			try {
				data.readFile(filePath);
			} catch( Exception e) {
				log.warn("", e);
			}
		}

		log.info("file read complate {}, mode={}, period={}",jobInfo.toString(), mode, period);
		
		PcodeAuidsMap pcodeAuidsMap   = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap   = data.getAuidPcodesMap();
		
		int pcodeCount = pcodeAuidsMap.getMap().size();
		int auidCount = auidPcodesMap.getMap().size();
		
		if(auidCount!=0 && pcodeCount!=0) {
			HashMap<String, ProductDto> productMap = new HashMap<String, ProductDto>();
			if(config.isAddCategoryWeight()) {
				BatchUtils.getExecutionTime(System.currentTimeMillis(),"getAllProductCategory");
				//TODO: 2020-09-14 config 대형광고주 리스트의 경우 엘라스틱 서치에서 카테고리정보 조회해오도록 수정.
				// 1. rmworker 동작시 large argument 타입으로 던져줘야 실행 가능.
				String value = config.getLargeAdvIdMap().get(jobInfo.getAdvId()+jobInfo.isPc());
				if(StringUtils.isNotEmpty(value)){
					productMap = readCategory.getAllProductCategory(jobInfo.getAdvId()); //미리만들어놓은 FILE 사용. VER3
				}
				else {
					productMap = elasticSearchService.getAllProductCategory(jobInfo.getAdvId(),jobInfo.isPc(),pcodeAuidsMap.getMap()); // 엘라스틱에서 전체 상품가져오기 VER2
					//productMap = productService.fetchAllProductCategory(jobInfo.getAdvId(),jobInfo.isPc());//  DB에서 전체 상품가져오기 VER1
				}
				BatchUtils.getExecutionTime(System.currentTimeMillis(),"getAllProductCategory");
				log.info("GetCategoryInfo advId={}, isPc={} : productMap size={}",jobInfo.getAdvId(),jobInfo.isPc(),productMap.size());
			}
			data.setProductMap(productMap);
			return data;
		}else {
			return null;
		}
	}
	
	@Override
	public long makeRecom(RecomBatchConfig batchConfig, RecomData data) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String today = String.format("%02d", calendar.get(Calendar.DATE));
		boolean click = batchConfig.isClick();
		JobInfo jobInfo = data.getJobInfo();
		
		PcodeAuidsMap pcodeAuidsMap   = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap   = data.getAuidPcodesMap();
		
		boolean isPc = jobInfo.isPc();
		int pcodeCount = pcodeAuidsMap.getMap().size();
		int auidCount = auidPcodesMap.getMap().size();
		
		log.info("### IBCF CONV run advId={}", jobInfo.getAdvId());
		
		int minOcuurenceAuidCount = config.getMinConversionOcuurenceAuidCount();
		int maxOcuurenceAuidCount = config.getMaxConversionOcuurenceAuidCount();
		
		Map<String, Integer> defaultOccurenceAuidCountMap = config.getDefaultOccurenceAuidCountMap();
		
		if(config.isAutoAuidCount()) {
			
			if(defaultOccurenceAuidCountMap.get(jobInfo.getAdvId()) != null) {
				
				int defaultOcuurenceAuidCount = defaultOccurenceAuidCountMap.get(jobInfo.getAdvId());
				
				minOcuurenceAuidCount = defaultOcuurenceAuidCount;
				
			}else {
			
				int bigPcodeCountWeight =  (int)Math.round((float)pcodeCount / 200000); 
				
				if(bigPcodeCountWeight < 1) {
					bigPcodeCountWeight = 1;
				}
				
				int tempMinOcuurenceAuidCount = (int)Math.round(Math.sqrt(auidCount/pcodeCount) * bigPcodeCountWeight);
				                                     
				
				if(tempMinOcuurenceAuidCount > minOcuurenceAuidCount) {
					minOcuurenceAuidCount = tempMinOcuurenceAuidCount;
				}
				
				if(minOcuurenceAuidCount <= maxOcuurenceAuidCount) {
					maxOcuurenceAuidCount = minOcuurenceAuidCount;
				}
				
			}
		}
		
		//Map<String,Map<String,String>> pcodeCategoryMap = new HashMap<String,Map<String,String>>();
		
		log.info("### advId={} isPc={} isClick={} product count={} auid count={} minOcuurenceAuidCount={} maxOcuurenceAuidCount={}"
				   , jobInfo.getAdvId(), isPc, click, pcodeCount, auidCount, minOcuurenceAuidCount, maxOcuurenceAuidCount) ;
				
		Map<String, Integer> occurenceDataMap = new HashMap<String, Integer>();

		HashMap<String, ProductDto> productMap = data.getProductMap();
		
		log.info("Make the co-occurence data" );
		//int curPcodeCount = 0;
		int curOcuurencePcodeCount = 0;
		
        Runtime runtime = Runtime.getRuntime();	
		long startMem = (runtime.totalMemory() - runtime.freeMemory()) / (memUnit);
		
		for( String pcode : pcodeAuidsMap.getMap().keySet()) {
			
			Set<Integer> pcodeMap = pcodeAuidsMap.getMap().get(pcode);			
			
			//++curPcodeCount;
			
			// 최소한의 인원이 확인한 상품
			if( minOcuurenceAuidCount > pcodeMap.size()) {
				continue;
			}
			
			// 동시 발생 데이터 수집
			for( Integer auid : pcodeMap) {
				Set<String> auidMap = auidPcodesMap.getMap().get(auid);
				
				if( auidMap == null || auidMap.size() == 0) {
					continue;
				}
				
				for( String recomPcode : auidMap) {
				    
				    if((occurenceDataMap.get(recomPcode + "_" + pcode) != null) || ( StringUtils.equals(pcode, recomPcode))) {
						continue;
					}
					
					String occurenceKey =  pcode + "_" + recomPcode;
					Integer occurenceValue = occurenceDataMap.get(occurenceKey);
					
					if(occurenceValue == null) {
						occurenceDataMap.put(occurenceKey, 1);
					}else {
						occurenceDataMap.put(occurenceKey, occurenceValue + 1);
					}
				}
			}
			
			long curMem = (runtime.totalMemory() - runtime.freeMemory()) / (memUnit);
			if((curMem/10)>(startMem/10)) {
				startMem = curMem;
				log.info("use memory : {}GB",curMem);
			}
			//curOcuurencePcodeCount++;
			
		}
		
		//logger.info(getLogPreText() + "current count : "+ curPcodeCount + "/" + pcodeCount + ", Co-occurence Pcode Count : " + curOcuurencePcodeCount);
		
		//////////////// 유사도 계산 추가 ///////////////
		
		log.info("Cosine Similarity" );
		
		Map<String,List<RecomPcode>> recomPcodeMap =  new HashMap<String,List<RecomPcode>>();
		
		for(String occurenceKey :  occurenceDataMap.keySet()) {
			
			String[] pcodes = occurenceKey.split("_");
			
			int pcode1 =  pcodeAuidsMap.getMap().get(pcodes[0]).size();
			int pcode2 =  pcodeAuidsMap.getMap().get(pcodes[1]).size();
			
			Integer occurenceValue = occurenceDataMap.get(occurenceKey);
			
			// 최소한의 인원이 같이 확인한 상품
			if(occurenceValue == null) {
				continue;
			}else if(( minOcuurenceAuidCount > occurenceValue)) {				
				//occurenceDataMap.remove(occurenceKey);
				continue;
			}
			
			/////////
			float cosine = (float)((float)occurenceValue/Math.sqrt(((double)pcode1 *  (double)pcode2)));	
			
			if( cosine <= config.getConversionMinSimilarity()) {
				continue;
			}
			
			// 같은 카테고리 가중치
			if(config.isAddCategoryWeight()) {
				
				String pcode1Category = productMap.get(pcodes[0])!=null?productMap.get(pcodes[0]).getCategory():"";
				String pcode2Category = productMap.get(pcodes[1])!=null?productMap.get(pcodes[1]).getCategory():"";
				
				if(StringUtils.isNotEmpty(pcode1Category) && StringUtils.isNotEmpty(pcode2Category)){
					
//					if(StringUtils.equals(pcodeCategoryMap.get(pcodes[0]).get("IMGPATH"), pcodeCategoryMap.get(pcodes[1]).get("IMGPATH"))) {
//						continue;
//					}
					
					
					if(StringUtils.equals(pcode1Category, pcode2Category)){
						cosine = cosine + 1;
					}
				}
			}
			
			// 소수점 4째자리에서 반올림
			//cosine =  Double.parseDouble(String.format("%.4f", cosine));
			/////////
			
			List<RecomPcode> recomPcode1List = recomPcodeMap.get(pcodes[0]);
			List<RecomPcode> recomPcode2List = recomPcodeMap.get(pcodes[1]);
			
			if(recomPcode1List == null) {
				recomPcode1List = new ArrayList<RecomPcode>();
				recomPcodeMap.put(pcodes[0], recomPcode1List);
			}
			
//			else {
//				
//				boolean isContinue = false;
//				
//				for(RecomPcode recomPcode : recomPcode1List) {
//					if(StringUtils.equals(pcodeCategoryMap.get(pcodes[0]).get("IMGPATH"), recomPcode.getImagePath())) {
//						isContinue = true;
//						break;
//					}
//				}
//				
//				if(isContinue) {
//					continue;
//				}
//				
//			}
			
			
			if(recomPcode2List == null) {
				recomPcode2List = new ArrayList<RecomPcode>();
				recomPcodeMap.put(pcodes[1], recomPcode2List);
			}
			
//			else {
//				
//				boolean isContinue = false;
//				
//				for(RecomPcode recomPcode : recomPcode2List) {
//					if(StringUtils.equals(pcodeCategoryMap.get(pcodes[1]).get("IMGPATH"), recomPcode.getImagePath())) {
//						isContinue = true;
//						break;
//					}
//				}
//				
//				if(isContinue) {
//					continue;
//				}
//				
//			}
			
			
			RecomPcode recomPcode1 = new RecomPcode(pcodes[0], occurenceValue, cosine);
			RecomPcode recomPcode2 = new RecomPcode(pcodes[1], occurenceValue, cosine);
			
			recomPcode1List.add(recomPcode2);
			recomPcode2List.add(recomPcode1);
			
			// occurenceDataMap.remove(occurenceKey);
			
		}
		
		log.info("Order by Cosine and insert at the redis and write the file." );
		///////////// 유사도 순으로 정렬 /////////////	
		curOcuurencePcodeCount = 0;
		
		String filePath = String.format("%s/%s/%s.%s", click?config.getOutClickPath():config.getOutConvPath(), today, jobInfo.getAdvId(), isPc ? "outp" : "outm");
		Logger saveFileOut = CustomLoggerFactory.createLoggerGivenFileName("LOGGER_1_"+jobInfo.getAdvId()+"_"+isPc, filePath, true);
		
		for(String pcode: recomPcodeMap.keySet() ) {
			
			List<RecomPcode> recomPcodeList = recomPcodeMap.get(pcode);
		
			if(recomPcodeList.size() > 1) {
				
				recomPcodeList.sort(new Comparator<RecomPcode>() {
					public int compare(RecomPcode o1, RecomPcode o2) {
						
						double diffCosine =  o2.getCosine() - o1.getCosine();
						
						if(diffCosine > 0) {
							return 1;
						}else if(diffCosine < 0) {
							return -1;
						}
						
						return 0;
					}
				});						
			  }
			
			// 카테고리별 (같은카테고리, 다른카테고리) 상품 정리			
			
			int sameCategoryCount  = 0;
			int otherCategoryCount = 0;			
			
			StringBuilder sameCategoryBuffer = new StringBuilder();
			StringBuilder otherCategoryBuffer = new StringBuilder();
			
			for (RecomPcode recomPcode : recomPcodeList) {
				
				// 같은 카테고리
				if(recomPcode.getCosine() > 1) {
					
					// 본상품용 (Second Count)					
					if( ++sameCategoryCount <= config.getMaxRecomPocde() ) {
						
						if( sameCategoryBuffer.length() > 0) {
							sameCategoryBuffer.append("|");
						}
						
						sameCategoryBuffer.append( recomPcode.getPcode());
					}
					
					
				// 다른 카테고리
				}else {

					if( ++otherCategoryCount <= config.getMaxRecomPocde() ) {
						
						if( otherCategoryBuffer.length() > 0) {
							otherCategoryBuffer.append("|");
						}
						
						otherCategoryBuffer.append( recomPcode.getPcode());
					}
				}
				
				if((sameCategoryCount >= config.getMaxRecomPocde()) && (otherCategoryCount >= config.getMaxRecomPocde())) {
					break;
				}
			}

			
			if( sameCategoryBuffer.length() == 0 && otherCategoryBuffer.length() == 0) {
				continue;
			}
			
			
			///////  저장
			
			String recommendProductList = sameCategoryBuffer.toString();
			
			if(otherCategoryBuffer.length() != 0) {				
				recommendProductList += "_" + otherCategoryBuffer.toString();
			}

			String recommendProductListToFile = "";
			if(!recommendProductList.equals("")) {
				String strPcodeSameOther[] = recommendProductList.split("_");
				String strSame = "";
				String strOther = "";
				if(strPcodeSameOther.length>0) {
					String pcodes[] = strPcodeSameOther[0].split("\\|");
					for(String _pcode : pcodes) {
						for(int i=0; i<recomPcodeList.size(); i++) {
							if(_pcode.equals(recomPcodeList.get(i).getPcode())) {
								if(strSame.length()>0)
									strSame += "|";
								//String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
								String pcode2Category = productMap.get(_pcode)!=null?"["+productMap.get(_pcode).getCategory()+"]":"";
								strSame += String.format("%s%s:%s:%.4f", _pcode,pcode2Category,recomPcodeList.get(i).getOccurenceValue(),recomPcodeList.get(i).getCosine());
							}
						}
					}
				}
				if(strPcodeSameOther.length>1) {
					String pcodes[] = strPcodeSameOther[1].split("\\|");
					for(String _pcode : pcodes) {
						for(int i=0; i<recomPcodeList.size(); i++) {
							if(_pcode.equals(recomPcodeList.get(i).getPcode())) {
								if(strOther.length()>0)
									strOther += "|";
								//String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
								String pcode2Category = productMap.get(_pcode)!=null?"["+productMap.get(_pcode).getCategory()+"]":"";
								strOther += String.format("%s%s:%s:%.4f", _pcode,pcode2Category,recomPcodeList.get(i).getOccurenceValue(),recomPcodeList.get(i).getCosine());
							}
						}
					}
				}
				recommendProductListToFile = strSame+"_"+strOther;
			}
			
			curOcuurencePcodeCount ++;
			
			if( config.isSaveFile()) {
				//String pcode1Category = pcodeCategoryMap.get(pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(pcode).get("CATE")+"]";
				String pcode1Category = productMap.get(pcode)!=null?"["+productMap.get(pcode).getCategory()+"]":"[]";
				//saveFile( jobInfo.getAdvId(), pcode+pcode1Category, recommendProductListToFile, isPc, today, false);
				String line = String.format("%s_%s_%s", jobInfo.getAdvId(), pcode+pcode1Category, recommendProductListToFile);
				saveFileOut.info(line);
			}
			
			if( config.isSaveRedis()) {			
				//saveRedis( "CO", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");
				String key = redisCluster.getRedisKey("CO", "sim", isPc, jobInfo.getAdvId(), click, pcode);
				redisCluster.saveRedis( key, recommendProductList, config.getRedisExpireTime());
			}
		}
		////////////
		
		return curOcuurencePcodeCount;

	}
		
}


