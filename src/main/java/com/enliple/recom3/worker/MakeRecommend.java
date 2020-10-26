package com.enliple.recom3.worker;

import com.enliple.recom3.common.constants.ConstantsCommon;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.messageserver.RecomMessageServer;
import com.enliple.recom3.worker.dao.RecomData;
import com.enliple.recom3.worker.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MakeRecommend {
    @Autowired
    private RecomMessageServer telegramServer;

    public void makeRecommendPool(RecomMaker cfMaker, RecomBatchConfig batchConfig, RecomData recomData) {
        JobInfo jobInfo = recomData.getJobInfo();
        long resultCount = 0;
        String status = ConstantsCommon.COMPLETED;
        try {
            cfMaker.updateStatus(jobInfo, ConstantsCommon.RUNNING);
            resultCount = cfMaker.makeRecom(batchConfig, recomData);

            //resultCount 는 UBCF 의 카운트이다 추후 수정해야 함.
            if(!jobInfo.getRecomeType().equals("UBCF")){
                resultCount = 0;
            }

            // AI Category 모드일때 recomeType 변경
            if(batchConfig.getMode().equals("ibcf_click_ai_category")) {
                jobInfo.setRecomeType(ConstantsIBCF.IBCF_TYPE_AI_CATE);
            }

            //광고주별 IBCF 카운트 로컬DB저장 (TABLENAME :RECOM_DAILY_DATA_STATS)
            cfMaker.insertRecomCount(RecomCount.builder().dateTime(Integer.valueOf(batchConfig.getStatDateTime()))
                    .advId(jobInfo.getAdvId())
                    .pc(jobInfo.isPc())
                    .recomType(jobInfo.getRecomeType())
                    .advIdUbcfCount(resultCount)
                    .advIdCrscCount(jobInfo.getAdvIdCrscCount())
                    .advIdCrocCount(jobInfo.getAdvIdCrocCount())
                    .createTime(LocalDateTime.now()).build());

            if(jobInfo.getAdvIdCrocCount() > 0 || jobInfo.getAdvIdCrscCount() > 0){
                //광고주별 IBCF 각각의 카운트 저장(TABLENAME: RECOM_DAILY_DATA_DETAIL_STATS)
                cfMaker.insertRecomCountDetail(RecomCountDetail.builder().dateTime(Integer.valueOf(batchConfig.getStatDateTime()))
                        .countMap(jobInfo.getRecomCountmap())
                        .advId(jobInfo.getAdvId())
                        .pc(jobInfo.isPc())
                        .recomType(jobInfo.getRecomeType())
                        .advIdUbcfDetailCount("")
                        .advIdCrscDetailCount("")
                        .advIdCrocDetailCount("")
                        .createTime(LocalDateTime.now()).build());
            }
            log.info("AdverID :{} , totalCount :{}",jobInfo.getAdvId(),jobInfo.getRecomTotalCount());
            log.info("commit : {}, {}, {}, {}, {}", batchConfig.getMode(), jobInfo.getAdvId(), jobInfo.isPc(), ConstantsCommon.COMPLETED, resultCount);
        } catch(ArithmeticException|IndexOutOfBoundsException|OutOfMemoryError e) {
            String message = getExceptionAlarmMessage(jobInfo,"\n "+batchConfig.getMode()+" Exception : "+e.getLocalizedMessage());
            log.error(message);
            status = ConstantsCommon.ERROR;
        } catch( Exception e) {
            String message = getExceptionAlarmMessage(jobInfo,"\n "+batchConfig.getMode()+" Exception : "+e.getLocalizedMessage());
            e.printStackTrace();
            log.error("", e);
            log.warn(message);
            telegramServer.sendTelegramExceptoinMessage(message);
            status = ConstantsCommon.ERROR;
        } finally {
            try {
                //RECOM_BATCH_RESULT INSET BATCH RESULT
                cfMaker.insertRecomResult(RecomBatchResultDto.builder().dateTime(Integer.valueOf(batchConfig.getStatDateTime()))
                        .adverId(jobInfo.getAdvId())
                        .is_pc(jobInfo.isPc())
                        .recomType(jobInfo.getRecomeType())
                        .batchStatus(status)
                        .pcodeAuidMap(jobInfo.getPcodeAuidMap())
                        .auidPcodeMap(jobInfo.getAuidPcodeMap())
                        .occurenceDataMap(jobInfo.getOccurenceDataMap())
                        .productMap(jobInfo.getPcodeMapSize())
                        .createTime(LocalDateTime.now()).build());

                //JOB_RESULT TABLE UPDATE
                cfMaker.updateStatus(jobInfo, status);
            }catch(Exception e) {
                String message = getExceptionAlarmMessage(jobInfo,"\n DBUpdate Exception("+status+") : "+e.getLocalizedMessage());
                e.printStackTrace();
                log.error("makeRecommendPool() error.. : detail = {}", e);
                log.warn(message);
                telegramServer.sendTelegramExceptoinMessage(message);
            }
            log.info("job destory : {} ,{}",jobInfo.getAdvId(),jobInfo.isPc());
//			if(batchConfig.isForceGc()) {
//				//메모리 초기화가 잘 안되서...
//				Runtime runtime = Runtime.getRuntime();
//				long curMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024*1024*1024);
//				log.warn("advid={} used memory before gc : {}GB",jobInfo.getAdvId(), curMem);
//				System.gc();
//				System.runFinalization();
//				curMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024*1024*1024);
//				log.warn("use memory after gc : {}GB", curMem);
//			}
        }
    }
    private String getExceptionAlarmMessage(JobInfo jobInfo, String ExceptionMessage) {
        String message = "Exception (advId="+jobInfo.getAdvId()+",isPc="+jobInfo.isPc()+")";
        return message+ExceptionMessage;
    }
}
