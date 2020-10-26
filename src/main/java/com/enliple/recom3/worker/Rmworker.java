package com.enliple.recom3.worker;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.common.queue.JobQueueManager;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.messageserver.RecomMessageServer;
import com.enliple.recom3.worker.dao.RecomData;
import com.enliple.recom3.worker.dto.RecomBatchConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class Rmworker {
    @Autowired
    private RecomMessageServer telegramServer;

    @Resource(name = "IBCF_CLICK_V1")
    private RecomMaker cfMakerIBCFV1;

    @Resource(name = "IBCF_CLICK_V2")
    private RecomMaker cfMakerIBCFV2;

    @Resource(name = "IBCF_CONV")
    private RecomMaker cfMakerIBCFConv;

    @Resource(name = "UBCF_CLICK")
    private RecomMaker cfMakerUBCF;

    @Autowired
    JobQueueManager jobQueueManager;

    @Autowired
    private MakeRecommend makeRecommend;

    @Autowired
    private Config config;

    public void startRmworker(boolean skip, String[] args)
    {
        try {
            log.info("Starting rmworker");
            String batchMode = args[0];
            telegramServer.sendTelegramMessage("---CF Engine Batch Start---"+batchMode);
            SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
            String statDateTime = transFormat.format( new Date());

            //새벽 rmsubmit 은 UBCF만 동작.
            if(batchMode.equals("rmsubmit") || batchMode.equals("ubcf")){
                //UBCF 클릭 데이터 => click(true)
                this.run(cfMakerUBCF, RecomBatchConfig.builder()
                        .mode("ubcf_click")
                        .statDateTime(statDateTime)
                        .click(true)    //click(imp)데이터 여부
                        .forceGc(false)//강제 GC여부
                        .skip(false)  //UBCF의 경우 가중치 게산이 없기 때문에 true
                        .build());
            }

            //전체 배치 or 대형광고주
            else if(batchMode.equals("all") || batchMode.equals("large") ){
//                //IBCF 컨버젼 데이터 => click(false)
                this.run(cfMakerIBCFConv, RecomBatchConfig.builder()
                        .mode("ibcf_conv")
                        .statDateTime(statDateTime)
                        .click(false)  //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());

                //IBCF 클릭 데이터 => click(true)
                this.run(cfMakerIBCFV1, RecomBatchConfig.builder()
                        .mode("ibcf_click_"+batchMode)
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());

                //AI 카테고리
                this.run(cfMakerIBCFV1, RecomBatchConfig.builder()
                        .mode("ibcf_click_ai_category")
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());

                //통계형
                this.run(cfMakerIBCFV2, RecomBatchConfig.builder()
                        .mode("ibcf_click_"+batchMode)
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());
            }
            //AI 카테고리
            else if(batchMode.equals("ai")){
                this.run(cfMakerIBCFV1, RecomBatchConfig.builder()
                        .mode("ibcf_click_ai_category")
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());
            } //통계형
            else if(batchMode.equals("v2")){
                this.run(cfMakerIBCFV2, RecomBatchConfig.builder()
                        .mode("ibcf_click_"+batchMode)
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .build());
            }
            //특정 광고주 아이디 배치
            else{
                //IBCF 클릭 데이터 => click(true)
                this.run(cfMakerIBCFV1, RecomBatchConfig.builder()
                        .mode("ibcf_click_"+batchMode)
                        .statDateTime(statDateTime)
                        .click(true)   //click(imp)데이터 여부
                        .forceGc(true) //강제 GC여부
                        .skip(skip)
                        .advId(args[0]) //특정광고주 지정.
                        .build());
            }
            telegramServer.sendTelegramMessage("---CF Engine Batch End---");
        }catch(Exception e) {
            e.printStackTrace();
            telegramServer.sendTelegramExceptoinMessage(e.getLocalizedMessage()+"\n"+e.toString());
        }
    }

    private void run(RecomMaker cfMaker, RecomBatchConfig batchConfig) {

        log.info("{} start", batchConfig.getMode());
        telegramServer.sendTelegramMessage("CF Engine "+batchConfig.getMode()+" start");

        List<JobInfo> jobArray = new ArrayList<JobInfo>();
        try {
            //추천데이터를 생성할 광고주 리스트를 가져온다.
            if(batchConfig.getMode().equals("ibcf_click_ai_category")) {
                jobArray = cfMaker.getAICateJobListAll();
            } else {
                jobArray = cfMaker.getJobListAll();
            }
        }catch(Exception e) {
            telegramServer.sendTelegramExceptoinMessage("DB Exception ("+e.getLocalizedMessage()+")");
            e.printStackTrace();
            return;
        }
        //Async로 Queue 리스트에 데이터를 넣는것을 시작한다.
        jobQueueManager.setList(cfMaker,jobArray,batchConfig,batchConfig.isSkip());

        //Queue에 하나씩 빼서 추천데이터를 생성한다.
        while(true) {
            RecomData recomData = null;
            try {
                //큐에 하나씩 데이터를 뺀다.
                recomData = jobQueueManager.poll();
                if(recomData!=null)
                /**
                 *  실제 추천데이터를 생성하는 메소드
                 *  makeRecommendPool()
                 */
                    makeRecommend.makeRecommendPool(cfMaker, batchConfig, recomData);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(recomData==null)
                break;
        }
    }
}
