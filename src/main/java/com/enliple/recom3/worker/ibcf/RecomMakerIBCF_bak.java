package com.enliple.recom3.worker.ibcf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.enliple.recom3.common.utils.BatchUtils;
import com.enliple.recom3.common.constants.ConstantsCommon;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.elasticsearch.service.ElasticSearchService;
import com.enliple.recom3.jpa.db1.service.RecomDailyDetailResultService;
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
import com.enliple.recom3.jpa.db1.service.RecomDailyResultService;
import com.enliple.recom3.jpa.db2.service.ProductService;
import com.enliple.recom3.worker.dao.RecomData;

import lombok.extern.slf4j.Slf4j;

@Component("IBCF_CLICK")
@Slf4j
public class RecomMakerIBCF_bak implements RecomMaker {
	@Autowired
	private Config config;

	@Autowired
	private RedisCluster redisCluster;

	@Autowired
	private ProductService productService;

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Resource(name = "IBCF_CLICK_JOBLIST_JPA")
	private JobListService jobListService;

	@Resource(name = "IBCF_CLICK_JOBRESULT_JPA")
	private JobResultService jobResultService;

	@Resource(name = "RECOM_DAILY_DATA_STATS_JPA")
	private RecomDailyResultService rcomDailyResultService;

	@Resource(name = "RECOM_DAILY_DATA_DETAIL_STATS_JPA")
	private RecomDailyDetailResultService recomDailyDetailResultService;

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
		rcomDailyResultService.insertRecord(recomCount);
	}

	@Override
	public void insertRecomCountDetail(RecomCountDetail recomCountDetail) {
		recomDailyDetailResultService.insertRecord(recomCountDetail);
	}

	@Override
	public RecomData getBaseRecomData(RecomBatchConfig batchConfig, RecomData data, JobInfo jobInfo) {
		int period = 0;
		boolean click = batchConfig.isClick();
		String mode = batchConfig.getMode();
		int ibcfPeriod = jobInfo.isPc() ? config.getPeriod() : config.getMobilePeriod();
		long readFileDataCount = 0;
		String suffix = jobInfo.isPc() ? ConstantsIBCF.MOB_SUFFIX : ConstantsIBCF.WEB_SUFFIX;

		Map<String,Boolean> passMap = config.getDefaultIbcfPassAuidMap(); // 배치 미실행 광고주
		String adIdPassCheckKey =  jobInfo.getAdvId()+":"+jobInfo.isPc();
		if(passMap.get(adIdPassCheckKey)!=null && passMap.get(adIdPassCheckKey)) {
			log.info("mode={} advId={} isPc={} : default.ibcf.pass.auid pass", mode, jobInfo.getAdvId(), jobInfo.isPc());
			return null;
		}
		//if(config.getDefaultPeriodMap().get(jobInfo.getAdvId()) != null ) {
		//	period = config.getDefaultPeriodMap().get(jobInfo.getAdvId());
		//}

		//파일 읽기
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		for(period = 0; period < ibcfPeriod; ++period) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			String year = String.format("%02d", calendar.get(Calendar.YEAR));
			String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
			String day = String.format("%02d", calendar.get(Calendar.DATE));

			String filePath = String.format("%s/%s/%s.%s",
					batchConfig.isClick() ? config.getRecomFilePath() : config.getConvFilePath(),
					year+month+day,
					jobInfo.getAdvId(),
					suffix);
			try {
				readFileDataCount += data.readFile(filePath);

				if(readFileDataCount > ConstantsIBCF.MAX_READ_COUNT_IBCF) {//4,000,000
					log.info("{}, mode={}, period={} break",jobInfo.toString(), mode, period);
					break;
				}
			} catch( Exception e) {
				log.warn("", e);
			}
		}
		log.info("file read complate {}, mode={}, period={}",jobInfo.toString(), mode, period);
		jobInfo.setReadFileDataCount(readFileDataCount);

		PcodeAuidsMap pcodeAuidsMap   = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap   = data.getAuidPcodesMap();

		if(pcodeAuidsMap.getMap().size() !=0 && auidPcodesMap.getMap().size()!=0) {
			HashMap<String, ProductDto> productMap = new HashMap<String, ProductDto>();
			if(config.isAddCategoryWeight()) {
				BatchUtils.getExecutionTime(System.currentTimeMillis(),"getAllProductCategory");
				String value = config.getLargeAdvIdMap().get(jobInfo.getAdvId()+jobInfo.isPc());
				if(StringUtils.isNotEmpty(value)){
					boolean ispc = value.equals("true") ? true : false;
					productMap = elasticSearchService.getAllProductCategory(jobInfo.getAdvId(),ispc,pcodeAuidsMap.getMap()); // 엘라스틱에서 전체 상품가져오기
				}else {
					productMap = productService.fetchAllProductCategory(jobInfo.getAdvId(),jobInfo.isPc());//  DB에서 전체 상품가져오기
				}
				BatchUtils.getExecutionTime(System.currentTimeMillis(),"getAllProductCategory");
				log.info("GetCategoryInfo advId={}, isPc={} : {}",jobInfo.getAdvId(),jobInfo.isPc(),productMap.size());
			}
			data.setProductMap(productMap);
			return data;
		}else {
			return null;
		}
	}

	@Override
	public long makeRecom(RecomBatchConfig batchConfig, RecomData data) {
		Map<String,Integer> recomCountmap = new HashMap<>();
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String today = String.format("%02d", calendar.get(Calendar.DATE));
		boolean click = batchConfig.isClick();
		JobInfo jobInfo = data.getJobInfo();

		PcodeAuidsMap pcodeAuidsMap   = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap   = data.getAuidPcodesMap();

		boolean isPc = jobInfo.isPc();
		int pcodeCount = pcodeAuidsMap.getMap().size();
		int auidCount = auidPcodesMap.getMap().size();

		log.info("### IBCF run advId={}", jobInfo.getAdvId());

//		2020.07.24 김기영과장님 요청사항(대표님지시)으로 아래 조건 제거
//		String passCheckCountAdvId = config.getPassCheckCountAdvId();
//		if(pcodeCount > auidCount && !passCheckCountAdvId.contains("|" + jobInfo.getAdvId() + "|")) {
//			log.info("### advId={} isPc={} isClick={} product count={} .. auid count={} pcodeCount > auidCount"
//					, jobInfo.getAdvId(), isPc, click, pcodeCount, auidCount );
//			//jobInfo.setMessage(jobInfo.getAdvId()+MESSAGE_SEPERATE+ "pcodeCount:"+pcodeCount + "auidCount:" +auidCount + "is pcodeCount > auidCount");
//			return pcodeCount;
//		}else if(pcodeCount < 100 && !passCheckCountAdvId.contains("|" + jobInfo.getAdvId() + "|")) {
//			//jobInfo.setMessage(jobInfo.getAdvId()+MESSAGE_SEPERATE+ "pcodeCount:"+pcodeCount+ "is pcodeCount < 100");
//			return pcodeCount;
//		}

		int minOcuurenceAuidCount = config.getMinOcuurenceAuidCount() ; // 3
		int maxOcuurenceAuidCount = config.getMaxOcuurenceAuidCount() ;	// 10
		int maxSecondOcuurenceAuidCount = config.getMaxSecodeOcuurenceAuidCount() ; // 5

		Map<String, Integer> defaultOccurenceAuidCountMap = config.getDefaultOccurenceAuidCountMap();

		if(config.isAutoAuidCount()) {

			if(defaultOccurenceAuidCountMap.get(jobInfo.getAdvId()) != null) {
				int defaultOcuurenceAuidCount = defaultOccurenceAuidCountMap.get(jobInfo.getAdvId());

				maxSecondOcuurenceAuidCount = defaultOcuurenceAuidCount;
				maxOcuurenceAuidCount = defaultOcuurenceAuidCount;
				minOcuurenceAuidCount = defaultOcuurenceAuidCount;

			}else {
				int bigPcodeCountWeight =  (int)Math.round((float)pcodeCount / ConstantsIBCF.WEIGHT_CALCULATION_VALUE);
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

				if(minOcuurenceAuidCount > maxSecondOcuurenceAuidCount) {
					minOcuurenceAuidCount = maxSecondOcuurenceAuidCount;
				}
			}
		}

		//Map<String,Map<String,String>> pcodeCategoryMap = new HashMap<String,Map<String,String>>();

		log.info("### advId={} isPc={} isClick={} product count={} auid count={} minOcuurenceAuidCount={} maxOcuurenceAuidCount={}"
				, jobInfo.getAdvId(), isPc, click, pcodeCount, auidCount, minOcuurenceAuidCount, maxOcuurenceAuidCount );

		Map<String, Integer> occurenceDataMap = new HashMap<String, Integer>();

		HashMap<String, ProductDto> productMap = data.getProductMap(); // 광고주의 전체 상품수

		log.info("Make the co-occurence data" );
		int curPcodeCount = 0;
		int curOcuurencePcodeCount = 0;

		Runtime runtime = Runtime.getRuntime();
		long startMem = (runtime.totalMemory() - runtime.freeMemory()) / (ConstantsCommon.MEMUNIT);

		for( String pcode : pcodeAuidsMap.getMap().keySet()) {

			Set<Integer> auidSet = pcodeAuidsMap.getMap().get(pcode);

			++curPcodeCount;

			// 최소한의 인원이 확인한 상품
			if( minOcuurenceAuidCount > auidSet.size()) {
				continue;
			}

			// 동시 발생 데이터 수집
			for( Integer auid : auidSet) {
				Set<String> pcodeSet = auidPcodesMap.getMap().get(auid);

				if( pcodeSet == null || pcodeSet.size() == 0) {
					continue;
				}

				for( String recomPcode : pcodeSet) {

					if(
						//(occurenceDataMap.get(recomPcode + "_" + pcode) != null) //1018 djlee
							(occurenceDataMap.get(recomPcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + pcode) != null)
									|| ( StringUtils.equals(pcode, recomPcode))) {
						continue;
					}

					//String occurenceKey =  pcode + "_" + recomPcode; //1018 djlee
					String occurenceKey =  pcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + recomPcode;
					Integer occurenceValue = occurenceDataMap.get(occurenceKey);

					if(occurenceValue == null) {
						occurenceDataMap.put(occurenceKey, 1);
					}else {
						occurenceDataMap.put(occurenceKey, occurenceValue + 1);
					}
				}
			}

			curOcuurencePcodeCount++;

			long curMem = (runtime.totalMemory() - runtime.freeMemory()) / (ConstantsCommon.MEMUNIT);
			if((curMem/10)>(startMem/10)) {
				startMem = curMem;
				log.info("use memory : {}GB, current pcode count : {}, Co-occurence Pcode Count : {}",curMem, curPcodeCount+"/"+pcodeCount, curOcuurencePcodeCount);
			}
		}

		log.info("current count : {}, Co-occurence Pcode Count : {} - {}",curPcodeCount,curOcuurencePcodeCount,jobInfo.getAdvId());

		//////////////// 유사도 계산 추가 ///////////////


		log.info("Cosine Similarity");
		log.info("occurenceDataMap size is {}",occurenceDataMap.size());

		Map<String,List<RecomPcode>> recomPcodeMap =  new HashMap<String,List<RecomPcode>>();
		curOcuurencePcodeCount = 0;
		for(String occurenceKey :  occurenceDataMap.keySet()) {
			//if((++curOcuurencePcodeCount) % 10 ==0)
			//	log.info(getLogPreText() + "Cosine Similarity : "+(curOcuurencePcodeCount) +"/"+occurenceDataMap.keySet().size());
			String[] pcodes = occurenceKey.split(ConstantsIBCF.SPLIT_CODE_IBCF_CHAR);
			int pcode1 =0;
			int pcode2 =0;
			try{
				 pcode1 = pcodeAuidsMap.getMap().get(pcodes[0]).size();
				 pcode2 = pcodeAuidsMap.getMap().get(pcodes[1]).size();
			}catch (Exception e){
				log.info(e.toString());
			}


			Integer occurenceValue = occurenceDataMap.get(occurenceKey);

			// 최소한의 인원이 같이 확인한 상품
			if(occurenceValue == null) {
				continue;

				//동시에 본 유저수 3명 아래면 계산안함.
			}else if(( minOcuurenceAuidCount > occurenceValue)) {
				//occurenceDataMap.remove(occurenceKey);
				continue;
			}

			/////////
			float cosine = (float)((float)occurenceValue/Math.sqrt(((double)pcode1 *  (double)pcode2)));

			if( cosine <= config.getFirstMinSimilarity()) {
				//occurenceDataMap.remove(occurenceKey);
				continue;
			}

			// 같은 카테고리 가중치
			if(config.isAddCategoryWeight()) {

				String pcode1Category = productMap.get(pcodes[0])!=null?productMap.get(pcodes[0]).getCategory():"";
				String pcode2Category = productMap.get(pcodes[1])!=null?productMap.get(pcodes[1]).getCategory():"";

				if(StringUtils.isNotEmpty(pcode1Category) && StringUtils.isNotEmpty(pcode2Category)){
					if(StringUtils.equals(pcode1Category, pcode2Category)){
						cosine = cosine + 1;
					}
				}
			}

			// 0.12 이상만 사용이므로 '<' 로 변경해야 하지 않나?? 확인 필요. 위에서 체크해도 되지 않나?
			if( cosine <= config.getSecondMinSimilarity()) {
				continue;
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

		//광고주별 카운트 추천데이터 카운트 체크를 위하여 추가 20200702 swkim
		int recomTotalCount = 0;
		int advIdCrscCount = 0;
		int advIdCrocCount = 0;

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
			int cwSameCategoryCount = 0;
			int srSameCategoryCount = 0;

			int cwOtherCategoryCount = 0;
			int srOtherCategoryCount = 0;

			StringBuilder cwSameCategoryBuffer = new StringBuilder();
			StringBuilder cwOtherCategoryBuffer = new StringBuilder();

			StringBuilder srSameCategoryBuffer = new StringBuilder();
			StringBuilder srOtherCategoryBuffer = new StringBuilder();

			for (RecomPcode recomPcode : recomPcodeList) {

				// 같은 카테고리
				if(recomPcode.getCosine() > 1) {
					// 본상품용 (Second Count)
					if( ++srSameCategoryCount <= config.getMaxRecomPocde() ) {
						if( srSameCategoryBuffer.length() > 0) {
							srSameCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
						}
						srSameCategoryBuffer.append( recomPcode.getPcode());
					}
					// 장바구니용
					if(recomPcode.getOccurenceValue()  >= maxOcuurenceAuidCount) {
						if( ++cwSameCategoryCount <= config.getMaxRecomPocde() ) {
							if( cwSameCategoryBuffer.length() > 0) {
								cwSameCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
							}
							cwSameCategoryBuffer.append( recomPcode.getPcode());
						}
					}
					// 다른 카테고리
				}else {
					if( ++srOtherCategoryCount <= config.getMaxRecomPocde() ) {
						if( srOtherCategoryBuffer.length() > 0) {
							srOtherCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
						}
						srOtherCategoryBuffer.append( recomPcode.getPcode());
					}
					// 장바구니용
					if(recomPcode.getOccurenceValue()  >= maxOcuurenceAuidCount && recomPcode.getCosine() >= config.getThirdMinSimilarity() ) {
						if( ++cwOtherCategoryCount <= config.getMaxRecomPocde() ) {
							if( cwOtherCategoryBuffer.length() > 0) {
								cwOtherCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
							}
							cwOtherCategoryBuffer.append( recomPcode.getPcode());
						}
					}
				}

				if( (cwSameCategoryCount >= config.getMaxRecomPocde()) && (cwOtherCategoryCount >= config.getMaxRecomPocde())
						&& (srSameCategoryCount >= config.getMaxRecomPocde()) && (srOtherCategoryCount >= config.getMaxRecomPocde())
				) {
					break;
				}
			}

			if( srSameCategoryBuffer.length() == 0 && srOtherCategoryBuffer.length() == 0) {
				continue;
			}

			///////  저장
			String recommendProductList = srSameCategoryBuffer.toString();

			if(srOtherCategoryBuffer.length() != 0) {
				recommendProductList += "_" + srOtherCategoryBuffer.toString();
			}

			////// SR
			String code = redisCluster.getCodeForPlatform("sim", isPc, click);

			//2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
			//if(config.isSaveRedis()) saveRedis( "SR", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

			// 구매 데이터 조회후 합치기
			String COkey = String.format( "%s%s_%s_%s", "CO", code, jobInfo.getAdvId(), pcode);
			String COdata = redisCluster.getJc().get(COkey);


			// 구매 데이터가 있을경우(COSR)
			//recommendProductList = getMergeCOdatas(isPc, pcode, srSameCategoryBuffer, srOtherCategoryBuffer,recommendProductList, COdata);
			//2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
			//if(config.isSaveRedis()) saveRedis( "COSR", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

			////// CW
			recommendProductList = cwSameCategoryBuffer.toString();
			if(cwOtherCategoryBuffer.length() != 0) {
				recommendProductList += "_" + cwOtherCategoryBuffer.toString();
			}

			//2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
			//saveRedis( "CW", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

			// 구매 데이터가 있을경우(COCW)
			recommendProductList = getMergeCOdatas(isPc, pcode, cwSameCategoryBuffer, cwOtherCategoryBuffer,recommendProductList, COdata);

			if(recommendProductList==null || recommendProductList.equals(""))
				continue;

			String recommendProductListToFile = "";
			//운영에서는 쓰고있지 않음.
			if(config.isSaveFile()) {
				if (!recommendProductList.equals("")) {
					String strPcodeSameOther[] = recommendProductList.split("_");
					String strSame = "";
					String strOther = "";
					if (strPcodeSameOther.length > 0) {
						String pcodes[] = strPcodeSameOther[0].split("\\|");
						for (String _pcode : pcodes) {
							for (int i = 0; i < recomPcodeList.size(); i++) {
								if (_pcode.equals(recomPcodeList.get(i).getPcode())) {
									if (strSame.length() > 0)
										strSame += "|";
									//String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
									String pcode2Category = productMap.get(_pcode) != null ? "[" + productMap.get(_pcode).getCategory() + "]" : "";
									strSame += String.format("%s%s:%s:%.4f", _pcode, pcode2Category, recomPcodeList.get(i).getOccurenceValue(), recomPcodeList.get(i).getCosine());
								}
							}
						}
					}
					if (strPcodeSameOther.length > 1) {
						String pcodes[] = strPcodeSameOther[1].split("\\|");
						for (String _pcode : pcodes) {
							for (int i = 0; i < recomPcodeList.size(); i++) {
								if (_pcode.equals(recomPcodeList.get(i).getPcode())) {
									if (strOther.length() > 0)
										strOther += "|";
									//String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
									String pcode2Category = productMap.get(_pcode) != null ? "[" + productMap.get(_pcode).getCategory() + "]" : "[]";
									strOther += String.format("%s%s:%s:%.4f", _pcode, pcode2Category, recomPcodeList.get(i).getOccurenceValue(), recomPcodeList.get(i).getCosine());
								}
							}
						}
					}
					recommendProductListToFile = strSame + "_" + strOther;
				}
			}

			int recomSameCount = 0;
			int recomOtherCount = 0;
			int recomCount = 0;

			if(StringUtils.isNotEmpty(recommendProductList)){
				String recomData[] = recommendProductList.split("_");
				if(recomData[0].length() > 0) {
					String samePcodes[] = recomData[0].split("\\|");
					recomCount = samePcodes.length;
					insertRecomCountMap(recomCountmap,recomCount,"same");
					recomSameCount++;
				}
				if(recomData.length > 1) {
					String OtherPcodes[] = recomData[1].split("\\|");
					recomCount = OtherPcodes.length;
					insertRecomCountMap(recomCountmap,recomCount,"other");
					recomOtherCount++;
				}
			}

			if(recomSameCount > 0){
				advIdCrscCount += 1;
			}
			if(recomOtherCount > 0){
				advIdCrocCount += 1;
			}
			String pcode1Category = productMap.get(pcode)!=null?"["+productMap.get(pcode).getCategory()+"]":"[]";
			recomTotalCount += recomSameCount + recomOtherCount;  //전체 생성된 추천 개수(같은 카테고리 + 다른 카테고리)
			curOcuurencePcodeCount++;

			//운영에서는 쓰고있지 않음.
			if(config.isSaveFile()) {
				//saveFile( jobInfo.getAdvId(), pcode+pcode1Category, recommendProductListToFile, isPc, today, false);
				String line = String.format("%s_%s_%s", jobInfo.getAdvId(), pcode+pcode1Category, recommendProductListToFile);
				saveFileOut.info(line);
			}
			if(config.isSaveRedis()) {
				//saveRedis( "COCW", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");
				String key = redisCluster.getRedisKey("COCW", "sim", isPc, jobInfo.getAdvId(), click, pcode);
				redisCluster.saveRedis( key, recommendProductList,config.getRedisExpireTime());
				//log.info("totalCount=" + "sameCate = " + "" + "otherCate = " + "");
			}
		}
		jobInfo.setAdvIdCrscCount(advIdCrscCount);
		jobInfo.setAdvIdCrocCount(advIdCrocCount);
		jobInfo.setRecomTotalCount(recomTotalCount);
		jobInfo.setRecomCountmap(recomCountmap);

		return curOcuurencePcodeCount;

	}

	/**
	 * getMergeCOdatas
	 *
	 * @param isPc
	 * @param pcode
	 * @param sameCategoryBuffer
	 * @param otherCategoryBuffer
	 * @param recommendProductList
	 * @param COdata
	 * @return
	 */
	protected String getMergeCOdatas(boolean isPc, String pcode, StringBuilder sameCategoryBuffer,
									 StringBuilder otherCategoryBuffer, String recommendProductList, String COdata) {

		if(StringUtils.isBlank(COdata)) {
			return recommendProductList;
		}

		String[] COdatas  = COdata.split("_");

		if(StringUtils.isNotBlank(COdatas[0])) {

			List<String> sameCOList  = new ArrayList<String>(Arrays.asList(StringUtils.split(COdatas[0], "\\|"))) ;
			List<String> sameSRList  = new ArrayList<String>(Arrays.asList(StringUtils.split(sameCategoryBuffer.toString(), "\\|"))) ;

			sameCOList = mergeListAfterRemovePcode(sameCOList, sameSRList);

			recommendProductList = sameCOList.stream()
					.map(n -> String.valueOf(n))
					.collect(Collectors.joining("|"));

		}else if(sameCategoryBuffer.length() != 0) {

			recommendProductList = sameCategoryBuffer.toString();

		}

		//System.out.println("pcode : "+ pcode + " ,COdatas.length : " + COdatas.length );

		if(COdatas.length > 1) {

			List<String> otherSRList  = new ArrayList<String>(Arrays.asList(StringUtils.split(otherCategoryBuffer.toString(), "\\|"))) ;
			List<String> otherCOList  = new ArrayList<String>(Arrays.asList(StringUtils.split(COdatas[1], "\\|"))) ;

			otherCOList = mergeListAfterRemovePcode(otherCOList, otherSRList);

			recommendProductList += "_" + otherCOList.stream()
					.map(n -> String.valueOf(n))
					.collect(Collectors.joining("|"));

		}else if(otherCategoryBuffer.length() != 0) {

			recommendProductList += "_" + otherCategoryBuffer.toString();

		}


		return recommendProductList;
	}

	/**
	 * mergeList After Remove Pcode
	 *
	 * @param COList
	 * @param SRList
	 * @return
	 */
	private List<String> mergeListAfterRemovePcode(List<String> COList, List<String> SRList) {
		if(COList.size() > 0) {

			for(String sameCOpcode : COList) {
				SRList.remove(sameCOpcode);
			}

			COList.addAll(SRList);

			if(COList.size() > config.getMaxRecomPocde() ) {

				for(int i=config.getMaxRecomPocde()-1; i<COList.size(); i++) {
					COList.remove(i);
				}
			}
		}

		return COList;
	}

	private void insertRecomCountMap(Map<String ,Integer> recomCountmap, int pcodes, String types){
		switch(pcodes) {
			case 1:
				getCount(recomCountmap,pcodes,types);
				break;
			case 2:
				getCount(recomCountmap,pcodes,types);
				break;
			case 3:
				getCount(recomCountmap,pcodes,types);
				break;
			case 4:
				getCount(recomCountmap,pcodes,types);
				break;
			case 5:
				getCount(recomCountmap,pcodes,types);
				break;
			case 6:
				getCount(recomCountmap,pcodes,types);
				break;
			case 7:
				getCount(recomCountmap,pcodes,types);
				break;
			case 8:
				getCount(recomCountmap,pcodes,types);
				break;
			case 9:
				getCount(recomCountmap,pcodes,types);
				break;
			case 10:
				getCount(recomCountmap,pcodes,types);
				break;
			case 11:
				getCount(recomCountmap,pcodes,types);
				break;
			case 12:
				getCount(recomCountmap,pcodes,types);
				break;
			case 13:
				getCount(recomCountmap,pcodes,types);
				break;
			case 14:
				getCount(recomCountmap,pcodes,types);
				break;
			case 15:
				getCount(recomCountmap,pcodes,types);
				break;
			case 16:
				getCount(recomCountmap,pcodes,types);
				break;
			case 17:
				getCount(recomCountmap,pcodes,types);
				break;
			case 18:
				getCount(recomCountmap,pcodes,types);
				break;

			default:
		}
	}

	private void getCount(Map<String ,Integer> recomCountmap, int pcodes, String type){
		if(recomCountmap.containsKey(pcodes+"_"+type)){
			recomCountmap.put(pcodes+"_"+type,recomCountmap.get(pcodes+"_"+type)+1);
		}else{
			recomCountmap.put(pcodes+"_"+type,1);
		}
	}
}


