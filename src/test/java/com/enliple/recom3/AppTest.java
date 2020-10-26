package com.enliple.recom3;


import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.enliple.recom3.common.queue.JobQueueManager;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JobQueueManager.class})
//@EnableAsync
//@SpringBootTest//전체
public class AppTest {
	
	@Autowired
	JobQueueManager jobQueueManager;
	@Test
	public void cpuload() {
		OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runbean = (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();

	    double bfprocesstime = osbean.getSystemLoadAverage();
	    log.info("bfprocesstime : {}",bfprocesstime);
	}
	@Test
	public void threadpoolTest() {
		class ThreadA implements Runnable{
			String name;
			public ThreadA(String name) {
				this.name = name;
			}
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
					log.info("thread end : {}",name);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		
		class ThreadB implements Callable<String>{
			String name;
			public ThreadB(String name) {
				this.name = name;
			}
			@Override
			public String call() throws Exception {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
					log.info("thread end : {}",name);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return name;
			}
		}		
		log.info("thread test start 1");
		ExecutorService executorService1 = Executors.newFixedThreadPool(2);
		
		for(int i=0;i<10;i++) {
			String threadName = "thread"+i;
			ThreadA th = new ThreadA(threadName);
			log.info("thread start : {}",threadName);
			executorService1.execute(th);
		}
		log.info("shutdowning...");
		executorService1.shutdown();
		try {
			executorService1.awaitTermination( 1, TimeUnit.DAYS );
			log.info("shutdowned");			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Test
	public void contextLoads() {

		//log.info("start");
//		RecomBatchConfig batchConfig = RecomBatchConfig.builder().
//		List<JobInfo> list = new ArrayList<JobInfo>();
//		for(int i=0; i<11;i++) {
//			String advId = "advId_"+i;
//			
//			JobInfoKey key = new JobInfoKey();
//			key.setAdvId(advId);
//			key.setIsPc("Y");			
//			
//			JobInfo job = new JobInfo();
//			job.setKey(key);
//			job.setSize(0);
//			job.setStatus("N");
//			
//			list.add(job);
//		}
//		jobQueueManager.setList(list);
//		
//		
//		while(true) {
//			log.info("Get wating");				
//			JobInfo jobInfo = null;
//			try {
//				jobInfo = jobQueueManager.poll();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(jobInfo==null)
//				break;
//			log.info("Get end " + jobInfo.getAdvId());
//		}
//		
//		log.info("end");
	}
	
	
}