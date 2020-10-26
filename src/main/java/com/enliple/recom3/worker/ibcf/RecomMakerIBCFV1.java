package com.enliple.recom3.worker.ibcf;

import java.util.*;

import javax.annotation.Resource;

import com.enliple.recom3.category.ReadCategory;
import com.enliple.recom3.common.utils.BatchUtils;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.elasticsearch.service.ElasticSearchService;
import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import com.enliple.recom3.jpa.db1.domain.JobInfoKey;
import com.enliple.recom3.jpa.db1.service.*;
import com.enliple.recom3.jpa.db2.service.ProductService;
import com.enliple.recom3.worker.RecomMaker;
import com.enliple.recom3.worker.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.worker.dao.RecomData;

import lombok.extern.slf4j.Slf4j;

@Component("IBCF_CLICK_V1")
@Slf4j
public class RecomMakerIBCFV1 implements RecomMaker {

    @Autowired
    private Config config;
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private ReadCategory readCategory;
    @Autowired
    private ProductService productService;
    @Autowired
    private RecomCaculatorIBCFV1 recomCaculatorIBCFV1;

    @Resource(name = "IBCF_CLICK_JOBLIST_JPA")
    private JobListService jobListService;
    @Resource(name = "IBCF_CLICK_JOBRESULT_JPA")
    private JobResultService jobResultService;
    @Resource(name = "RECOM_BATCH_RESULT_JPA")
    private RecomBatchResultService recomBatchResultService;
    @Resource(name = "RECOM_DAILY_DATA_STATS_JPA")
    private RecomDailyResultService rcomDailyResultService;
    @Resource(name = "RECOM_DAILY_DATA_DETAIL_STATS_JPA")
    private RecomDailyDetailResultService recomDailyDetailResultService;

    private Map<String, Integer> occurrenceDataMap = null;
    private Map<String,List<RecomPcode>> recomPcodeMap = null;

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
        List<JobInfo> jobInfoList = new ArrayList<>();
        List<AdverIdByType> AdverIdByType = new ArrayList<>();
        AdverIdByType = jobListService.getAIAdverIdList();

        for(int i=0; i<AdverIdByType.size(); i++){
            JobInfo jobInfo = new JobInfo();
            JobInfoKey jobInfoKey = new JobInfoKey();
            jobInfoKey.setAdvId(AdverIdByType.get(i).getKey().getADVER_ID());
            jobInfoKey.setIsPc("N");
            jobInfo.setKey(jobInfoKey);
            jobInfoList.add(jobInfo);

            jobInfo = new JobInfo();
            jobInfoKey = new JobInfoKey();
            jobInfoKey.setAdvId(AdverIdByType.get(i).getKey().getADVER_ID());
            jobInfoKey.setIsPc("Y");
            jobInfo.setKey(jobInfoKey);
            jobInfoList.add(jobInfo);
        }
        return jobInfoList;
    }

    @Override
    public void updateStatus(JobInfo jobInfo, String status) {
        jobListService.updateStatus(jobInfo, status);
    }

    @Override
    public void insertRecomResult(RecomBatchResultDto recomBatchResult) {
        recomBatchResultService.insertRecord(recomBatchResult);
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
                //TODO: 2020-09-14 config 대형광고주 리스트의 경우 엘라스틱 서치에서 카테고리정보 조회해오도록 수정.
                // 1. rmworker 동작시 large argument 타입으로 던져줘야 실행 가능.
                String value = config.getLargeAdvIdMap().get(jobInfo.getAdvId()+jobInfo.isPc());
                if(StringUtils.isNotEmpty(value)){
                    productMap = readCategory.getAllProductCategory(jobInfo.getAdvId()); //미리만들어놓은 FILE 사용. VER3
                }
                else {
                    if("ibcf_click_ai_category".equals(mode)) {
                        productMap = productService.fetchAllProductAiCategory(jobInfo.getAdvId(),jobInfo.isPc());//  DB에서 전체 상품가져오기 VER1
                    } else {
                        productMap = elasticSearchService.getAllProductCategoryV2(jobInfo.getAdvId(),jobInfo.isPc()); // 엘라스틱에서 전체 상품가져오기 VER3 --rest version
                    }
                    //productMap = elasticSearchService.getAllProductCategory(jobInfo.getAdvId(),jobInfo.isPc(),null); // 엘라스틱에서 전체 상품가져오기 VER2 --tcp version
//                    productMap = productService.fetchAllProductCategory(jobInfo.getAdvId(),jobInfo.isPc());//  DB에서 전체 상품가져오기 VER1
                }
                BatchUtils.getExecutionTime(System.currentTimeMillis(),"getAllProductCategory");
                log.info("GetCategoryInfo advId={}, isPc={} : productMap size={}",jobInfo.getAdvId(),jobInfo.isPc(),productMap.size());
            }
            data.setProductMap(productMap);
            return data;
        }else {
            log.info("pcodeAuidsMap:{} or auidPcodesMap:{} size is zero.",pcodeAuidsMap.getMap().size(),auidPcodesMap.getMap().size());
            return null;
        }
    }

    @Override
    public long makeRecom(RecomBatchConfig batchConfig, RecomData data) {
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

        //1. 제약조건 설정
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

        log.info("### advId={} isPc={} isClick={} product count={} auid count={} minOcuurenceAuidCount={} maxOcuurenceAuidCount={}"
                , jobInfo.getAdvId(), isPc, batchConfig.isClick(), pcodeCount, auidCount, minOcuurenceAuidCount, maxOcuurenceAuidCount );

        try{
            //0. 계산 로직 시작
            HashMap<String, ProductDto> productMap = data.getProductMap(); // 광고주의 전체 상품 같은 카테고리 가중치 계산을 위한 사용.
            int curOcuurencePcodeCount = 0;

            //1. Occurence 계산.
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getOccurence-"+jobInfo.getAdvId());
            occurrenceDataMap = recomCaculatorIBCFV1.getOccurence2(pcodeAuidsMap,auidPcodesMap,minOcuurenceAuidCount,curOcuurencePcodeCount,jobInfo.getAdvId(),jobInfo);
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getOccurence-"+jobInfo.getAdvId());

            //2. Similarity 계산.
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getSimilarity-"+jobInfo.getAdvId());
            log.info("occurenceDataMap size is {}.", occurrenceDataMap.size());
            recomPcodeMap = recomCaculatorIBCFV1.getSimilarity(occurrenceDataMap,pcodeAuidsMap,minOcuurenceAuidCount,productMap, batchConfig,jobInfo);
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getSimilarity-"+jobInfo.getAdvId());

            //3. Similarity 정렬 및 레디스 적재.
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getSimilaritySort-"+jobInfo.getAdvId());
            curOcuurencePcodeCount = recomCaculatorIBCFV1.getSimilaritySort(recomPcodeMap,productMap,maxOcuurenceAuidCount,isPc,batchConfig,jobInfo.getAdvId(),curOcuurencePcodeCount,jobInfo);
            BatchUtils.getExecutionTime(System.currentTimeMillis(),"getSimilaritySort-"+jobInfo.getAdvId());

            return curOcuurencePcodeCount;

        }catch (Exception e){
            log.info("Error {}" ,e.toString());
            return 0;
        }
    }
}



