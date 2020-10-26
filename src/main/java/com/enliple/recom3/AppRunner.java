package com.enliple.recom3;

import java.util.*;
import com.enliple.recom3.common.config.EngineConfig;
import com.enliple.recom3.worker.AiCateMaker;
import com.enliple.recom3.worker.Rmsubmit;
import com.enliple.recom3.worker.Rmworker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import com.enliple.recom3.common.queue.JobQueueManager;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.messageserver.RecomMessageServer;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppRunner implements CommandLineRunner {
	Map<String,String> batchPropertiesMap = new HashMap<>();

	@Autowired
	private RecomMessageServer telegramServer;

	@Autowired
	JobQueueManager jobQueueManager;

	@Autowired
	RedisCluster redisCluster;

	@Autowired
	private EngineConfig engineConfig;

	@Autowired
	private Rmsubmit rmsubmit;

	@Autowired
	private Rmworker rmworker;

	@Autowired
	private AiCateMaker aiCateMaker;

	static boolean largeAdvIdSkip = true;

	@Override
	public void run(String... args) throws Exception {
		try{
			log.info("start cfEngine.");
			log.info("read to batch properties.");
			// TODO: 2020-09-15 배치 설정 정보 DB 불러오기 (미완성 -실제 쓰고 있지 않음.)
			batchPropertiesMap = engineConfig.loadbatchProperties();
			log.info("read to batch properties complete.");


			/**
			 * 1. rmsubmit - shopdata 의 was , consumer 원천데이터를 추천데이터를 만들기 위해서 가공하는 메소드
			 * 2. recom,cart,clk,conv 에 1차적으로 가공하여 데이터를 적재
			 */
			if(args[0].equals("rmsubmit")) {
				rmsubmit.startRmsubmit(args);
				rmworker.startRmworker(largeAdvIdSkip,args);
			}
			else if(args[0].equals("ubcf")) {
				rmworker.startRmworker(largeAdvIdSkip,args);
			}
			/**
			 * 대형광고주 전용 rmworker
			 */
			else if(args[0].equals("large")){
				largeAdvIdSkip = false;
				rmworker.startRmworker(largeAdvIdSkip, args);
			}
			/**
			 * 모든 광고주 rmworker
			 */
			else if(args[0].equals("all")){
				rmworker.startRmworker(largeAdvIdSkip, args);
			}
			/**
			 * AI 카테고리를 이용한 일부 광고주 배치 rmworker
			 */
			else if(args[0].equals("ai")){
				// AI용 카테고리 정보 Redis에 넣기(2020.10.20 사용안함. cwpark)
//				aiCateMaker.getAiCategory();
				rmworker.startRmworker(largeAdvIdSkip, args);
			}
			/**
			 * 통계형 IBCF Ver2 rmworker
			 */
			else if(args[0].equals("v2")){
				rmworker.startRmworker(largeAdvIdSkip, args);
			}
			/**
			 * 특정 광고주 rmworker
			 * 특정광고주의 배치 실행의 경우 args에 광고주 아이디가 포함되어 있음.
			 */
			else {
				rmworker.startRmworker(largeAdvIdSkip, args);
			}
		}
		catch (Exception e){
			log.error("batch error. error is {}",e.toString());
		}
		finally {
			this.close(); // 텔레그램서버,레디스클러스트 종료
			log.info("ended cfEngine.");
		}
	}
	private void close() {
		telegramServer.close();
		redisCluster.close();
	}
}
