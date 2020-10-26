package com.enliple.recom3.common.queue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import com.enliple.recom3.common.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.worker.RecomMaker;
import com.enliple.recom3.worker.dao.RecomData;
import com.enliple.recom3.worker.dto.RecomBatchConfig;

@Slf4j
@Component
public class JobQueueManager {
	@Getter @Setter
	private boolean finish = false;
	
	private int QueueSize = 4;
	
	private BlockingQueue<RecomData> queue;

	@Autowired
	private Config config;
	
	@PostConstruct
	private void init() {
		queue = new ArrayBlockingQueue<RecomData>(QueueSize);
	}
	
	private void put(RecomData obj) throws InterruptedException {
		queue.put(obj);
	}
	
	public RecomData poll() throws InterruptedException {
		RecomData recomData = null;
		while(true) {
			recomData = queue.poll(3, TimeUnit.SECONDS);
			if(recomData!=null) {
				log.info("JobQueue polled : {}", recomData.getJobInfo().toString());
				break;
			}
			if(finish)
				break;			
		}
		return recomData;
	}

	@Async
	public void setList(RecomMaker cfMaker, List<JobInfo> list, RecomBatchConfig batchConfig,boolean largeAdvIdSkip) {
		String batchAdvId = batchConfig.getAdvId(); //특정 광고주
		String batchMode = batchConfig.getMode();
		this.setFinish(false);
		log.info("JobQueueManager {} start",batchConfig.getMode());

		for(int i=0; i<list.size();i++) {
			JobInfo jobInfo = list.get(i);
			try {
				String advId = jobInfo.getAdvId(); //광고주 아이디

				// TODO: 2020-10-06 대형 광고주 스킵은 현재 사용안함
//				String skipAdvId = config.getLargeAdvIdMap().get(jobInfo.getAdvId()+jobInfo.isPc());
//				if(largeAdvIdSkip && StringUtils.isNotEmpty(skipAdvId)){
//					log.info("large AdvId Skip.. Skip advId is {}.",jobInfo.getAdvId());
//					continue;
//				}

				// TODO: 2020-10-06 특정 광고주만 큐에 넣음.
				//특정광고주 배치 실행의 경우 다른광고주들은 SKIP.
				if(StringUtils.isNotEmpty(batchAdvId)){
					if(!jobInfo.getAdvId().equals(batchAdvId)){
						continue;
					}
				}

				RecomData recomData = new RecomData();
				//cfMaker.updateStatus(jobInfo, "Q");//대기상태(큐)로 상태값을 입력
				recomData = cfMaker.getBaseRecomData(batchConfig, recomData, jobInfo);
				if(recomData!=null) {
					recomData.setJobInfo(jobInfo);					
					this.put(recomData);
					log.info("JobQueue puted size={} : {}",queue.size(), jobInfo.toString());
				}else {
					cfMaker.updateStatus(jobInfo, "E");;//E(자료없음)로 상태값을 입력
					log.info("JobQueue put passed : {}",jobInfo.toString());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("JobQueueManager {} end",batchConfig.getMode());
		this.setFinish(true);
	}
}
