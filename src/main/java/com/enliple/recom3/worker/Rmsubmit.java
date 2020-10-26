package com.enliple.recom3.worker;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.submit.SeperateFile;
import com.enliple.recom3.submit.SubmitJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.TimeZone;

@Slf4j
@Component
public class Rmsubmit {
    @Autowired
    private Config config;

    @Autowired
    private SeperateFile seperateFile;

    @Autowired
    private SubmitJob submitJob;

    public void startRmsubmit( String[] args )
    {
        log.info("Starting rmsubmit");
        String yesterday = null;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String today =  String.format("%02d", calendar.get(Calendar.DATE));

        /**
         * rmsubmit 오류로 인하여 특정 날짜의 recom을 생성할때 사용
         */
        if( args.length == 2) {
            log.info("startWith date : "+args[1]);
            yesterday = args[1];
        }
        else {
            yesterday = today;
        }

        // click & Conversion 데이터를 광고주별 파일로 생성
        if( config.isSeparateFile()) {
            log.info("Start to seperate files");
            seperateFile.run( yesterday, "impp");
            seperateFile.run( yesterday, "impm");
            seperateFile.run( yesterday, "cartp");
            seperateFile.run( yesterday, "cartm");
            seperateFile.run( yesterday, "clkp");
            seperateFile.run( yesterday, "clkm");
            seperateFile.run( yesterday, "convp");
            seperateFile.run( yesterday, "convm");
            log.info("End to seperate files");
        }
        // rmworker 프로세스 실행 유무 -1
        if(!config.isSubmitJob())return;

        /**
         * rmsubmit 오류로 인하여 특정 날짜의 recom을 생성할때 사용
         */
        if(args.length == 2){
            log.info("this batch is rmsubmit only.");
            return;
        }
        int jobCount = 0;
        int convJobCount = 0;

        // 광고주에 해당하는 Job list등록
        jobCount = submitJob.run(true);
        convJobCount = submitJob.run(false);

//			for( int i=0; i < config.getExecutorNumber(); i++) {
//				for( String host : config.getExecutorHosts()) {
//					String cmd = String.format("ssh rpapp@%s rmworker &> /dev/null ", host);
//					try {
//						Runtime.getRuntime().exec(cmd);
//						log.info(cmd);
//					} catch (IOException e) {
//						 // TODO Auto-generated catch block
//						log.error("", e);
//						System.exit(-1);
//					}
//				}
//			}
    }
}
