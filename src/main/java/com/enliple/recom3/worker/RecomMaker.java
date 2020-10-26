package com.enliple.recom3.worker;

import java.util.List;

import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.worker.dao.RecomData;
import com.enliple.recom3.worker.dto.*;

public interface RecomMaker {
	//추천데이터 생성 결과를 저장하는 메소드 -> 실제활용 하고 있지 않아서 테이블을 새로 만들고 분석을 위한 더 많은 정보를 저장함.
	//public void insertRecomResult(JobResultInfo jobResultinfo, long resultCount);

	//추천데이터 생성 결과 저장 메소드
	public void insertRecomResult(RecomBatchResultDto recomBatchResult);

	//추천데이터 생성할 광고주정보들를 가져오는 메소드
	public List<JobInfo> getJobListAll();

	public List<JobInfo> getAICateJobListAll();

	//추천데이터 생성할 광고주정보를 가져오는 메소드//***쓰이진 않음***
	public JobInfo getJobList();

	//추천데이터 생성 프로세스 상태정보를 update시키는 메소드 	
	public void updateStatus(JobInfo jobInfo, String status);

	//추천데이터 생성을 위한 기본 데이터를 수집하기 위한 메소드 ex) 로그파일 읽어오기, 상품 카테고리정보
	public <T extends RecomData> T getBaseRecomData(RecomBatchConfig batchConfig, T data, JobInfo jobInfo);

	//추천데이터 생성
	public <T extends RecomData> long makeRecom(RecomBatchConfig batchConfig, T data);

	//추천데이터 광고주별 카운트 저장
	public void insertRecomCount(RecomCount recomCount);

	//추천데이터 광고주별 카운트맵 저장  ex : 1^4,2^5,3^6 ..... 18^0
	public void insertRecomCountDetail(RecomCountDetail recomCountDetail);

}
