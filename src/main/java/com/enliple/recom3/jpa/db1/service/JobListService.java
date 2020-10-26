package com.enliple.recom3.jpa.db1.service;

import java.util.List;

import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import com.enliple.recom3.jpa.db1.domain.JobInfo;

public interface JobListService {
	//rmworker에서 jobInfo list를 가져올때 사용
	public List<JobInfo> getJobListAll();

	//rmworker에서 joblist 한개 가져올때 사용 //사용하지 않음	
	public JobInfo getJobList();

	public List<AdverIdByType> getJobListV2All(); // 통계형 광고주리스트 가져오기

	public List<AdverIdByType> getAIAdverIdList(); // AI 카테고리 광고주리스트 가져오기

	//rmworker상태값을 update할때 사용 (N:대기 , R:실행중  E:예외, D:종료)
	void updateStatus(JobInfo jobInfo, String status);

	//rmsubmit에서 joblist를 다지울때 사용
	public void deleteAll();

	//rmsubmit에서 joblist를 하나씩 대기상태(N)으로 입력할때 사용
	public void insertRecord(List<JobInfo> jobInfoList);
}
