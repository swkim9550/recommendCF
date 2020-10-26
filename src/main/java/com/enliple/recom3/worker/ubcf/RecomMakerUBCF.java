package com.enliple.recom3.worker.ubcf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import com.enliple.recom3.common.constants.ConstantsUBCF;
import com.enliple.recom3.jpa.db1.service.RecomBatchResultService;
import com.enliple.recom3.jpa.db1.service.RecomDailyResultService;
import com.enliple.recom3.worker.RecomMaker;
import com.enliple.recom3.worker.dto.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.enliple.recom3.common.utils.BeanUtils;
import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.common.CustomLoggerFactory;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db1.service.JobListService;
import com.enliple.recom3.jpa.db1.service.JobResultService;
import com.enliple.recom3.worker.dao.RecomData;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("UBCF_CLICK")
public class RecomMakerUBCF implements RecomMaker {

	@Autowired
	private Config config;

	@Resource(name = "UBCF_CLICK_JOBLIST_JPA")
	private JobListService jobListService;

	@Resource(name = "UBCF_CLICK_JOBRESULT_JPA")
	private JobResultService jobResultService;

	@Resource(name = "RECOM_BATCH_RESULT_JPA")
	private RecomBatchResultService recomBatchResultService;

	@Resource(name = "RECOM_DAILY_DATA_STATS_JPA")
	private RecomDailyResultService rcomDailyResultService;

	int memUnit = 1024*1024*1024;

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
		//recomBatchResultService.insertRecord(recomBatchResult);
	}
	@Override
	public void insertRecomCount(RecomCount recomCount){
		rcomDailyResultService.insertRecord(recomCount);
	}

	@Override
	public void insertRecomCountDetail(RecomCountDetail recomCountDetail) {

	}

	@Override
	public RecomData getBaseRecomData(RecomBatchConfig batchConfig, RecomData data, JobInfo jobInfo) {
		boolean click = batchConfig.isClick();
		String mode = batchConfig.getMode();
		String suffix = jobInfo.isPc() ? ConstantsUBCF.MOB_SUFFIX : ConstantsUBCF.WEB_SUFFIX;

		int ubcfPeriod = config.getUbcfPeriod();
		int period = 0;
		/*
		int period = 0;
		if(config.getDefaultUbcfPeriodMap().get(jobInfo.getAdvId()) != null ) {
			period = config.getDefaultUbcfPeriodMap().get(jobInfo.getAdvId());
		}else if(jobInfo.getSize()<(400*10000)){
			period = 15;
		}else if(jobInfo.getSize()<(4000*10000)){
			period = 7;
		}else {
			period = ubcfPeriod;
		}
		*/

		//설정 광고주 제외
		Map<String,Boolean> passMap = config.getDefaultUbcfPassAuidMap();
		String adIdPassCheckKey =  jobInfo.getAdvId()+":"+jobInfo.isPc();
		if(passMap.get(adIdPassCheckKey)!=null && passMap.get(adIdPassCheckKey)) {
			log.info("mode={} advId={} isPc={} : default.ubcf.pass.auid pass", mode, jobInfo.getAdvId(), jobInfo.isPc());
			return null;
		}

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		long readFileDataCount = 0;
		for( period = 0; period < ubcfPeriod; ++period) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			String year = String.format("%02d", calendar.get(Calendar.YEAR));
			String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
			String day = String.format("%02d", calendar.get(Calendar.DATE));

			String filePath = String.format("%s/%s/%s.%s",
					click ? config.getRecomFilePath() : config.getConvFilePath(),
					year+month+day,
					jobInfo.getAdvId(),
					suffix);

			try {
				readFileDataCount += data.readFile(filePath);
				if(readFileDataCount > ConstantsUBCF.MAX_READ_COUNT_UBCF) {//300,000
					log.info("{}, mode={}, period={} break",jobInfo.toString(), mode, period);
					break;
				}
			} catch( Exception e) {
				log.warn("", e);
			}
		}

		log.info("file read complate {}, mode={}, period={}",jobInfo.toString(), mode, period);

		PcodeAuidsMap pcodeAuidsMap   = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap   = data.getAuidPcodesMap();

		int pcodeCount = pcodeAuidsMap.getMap().size();
		int auidCount = auidPcodesMap.getMap().size();
		if(auidCount!=0 && pcodeCount!=0) {
			return data;
		}else {
			return null;
		}
	}

	@Override
	public long makeRecom(RecomBatchConfig batchConfig, RecomData data) {
		boolean click = batchConfig.isClick();
		JobInfo jobInfo = data.getJobInfo();
		jobInfo.setRecomeType(ConstantsUBCF.UBCF_TYPE_VER1);
		String mode = batchConfig.getMode();
		String suffix = jobInfo.isPc() ? ConstantsUBCF.MOB_SUFFIX : ConstantsUBCF.WEB_SUFFIX;
		int period = config.getUbcfPeriod();
		long readFileDataCount = 0;
		/*
		int period = ubcfPeriod;
		if(config.getDefaultUbcfPeriodMap().get(jobInfo.getAdvId()) != null ) {
			period = config.getDefaultUbcfPeriodMap().get(jobInfo.getAdvId());
		}else if(jobInfo.getSize()<(400*10000)){
			period = 15;
		}else if(jobInfo.getSize()<(4000*10000)){
			period = 7;
		}else {
			period = ubcfPeriod;
		}
		*/

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		String today = String.format("%02d", calendar.get(Calendar.DATE));
		for( int i = 0; i < period; i++) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			String year = String.format("%02d", calendar.get(Calendar.YEAR));
			String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
			String day = String.format("%02d", calendar.get(Calendar.DATE));

			String filePath = String.format("%s/%s/%s.%s",
					click ? config.getRecomFilePath() : config.getConvFilePath(),
					year+month+day,
					jobInfo.getAdvId(),
					suffix);
			try {
				readFileDataCount += data.readFile(filePath);
				if(mode.equals("ubcf_click") && readFileDataCount > 400000) {//400,000
					period = i+1;
					log.info("{}, mode={}, period={} break",jobInfo.toString(), mode, i+1);
					break;
				}
			} catch( Exception e) {
				log.warn("", e);
			}
		}
		jobInfo.setReadFileDataCount(readFileDataCount);  //파일 크기에 따른 시간분석을 위해 임시추가.

		PcodeAuidsMap pcodeAuidsMap = data.getPcodeAuidsMap();
		AuidPcodesMap auidPcodesMap = data.getAuidPcodesMap();

		boolean isPc = jobInfo.isPc();
		//pcodeAuidsMap => key : pcode, value : pcode(상품)을 한 번이라도 본 auid(유저) list
		//auidPcodesMap => key : auid, value : auid(유저)을 한 번이라도 본 pcode(상품) list
		int pcodeCount = pcodeAuidsMap.getMap().size();
		int auidCount = auidPcodesMap.getMap().size();


		//if(auidCount < 100) {
		//	logger.info(getLogPreText() + String.format("advId=%s force return (auidCount=%s) ", jobInfo.getAdvId(), auidCount));
		//	return pcodeCount;
		//}
		// auid는 모두 count가 0으로 되어있음
		// auid 100미만 쇼핑몰이 몇개인지 확인해야함
		// 광고주 별 imp : 2,527개 => 광고주 ubcf 결과 : 1,665개 (ibcf : 1,475개 정도)

		// 10kb 이하 파일은 전체의 약 절반정도(2400/4800) => 1,200개 정도가  AUID 100 이하일 수 있음
		// 데이터가 부족한 이유  => 많은 쇼핑몰들이 cf로 추천 목록이 안만들어짐
		// 수정해야하는 부분으로 사료됨


		//유사도 분석을 수행하기 위해 요구되는 최소한의 클릭수
		int minOcuurencePcodeCount = config.getMinOcuurencePcodeCount(); // 3		확인결과 경험한 후, 수치를 정했다고 함
		int maxOcuurencePcodeCount = config.getUbcfMaxOcuurencePcodeCount(); //		추후 쇼핑몰 사이즈나 쇼핑몰 별로 컨피그값을 수정하거나 튜닝할 수 있어야함
		int tempMinOcuurencePcodeCount = 0;

		// 분석을 위해 요구되는 유저의 최소한 활동수를 분석한 맵으로 보임.. 유저 : 활동수로 보임. 맞는지는 모르겠음
		Map<String, Integer> defaultUbcfOccurenceAuidCountMap = config.getDefaultUbcfOccurenceAuidCountMap();

		if(config.isAutoAuidCount()) {

			//만약 데이터가 있다면
			if(defaultUbcfOccurenceAuidCountMap.get(jobInfo.getAdvId()) != null) {
				//jobInfo.getAvdId => /home/users/rpapp/home/data/rmsubmit.conf 파일내의 AvdId에 들어있는 광고주
				//default.ubcf.occurence.auid.count -> cjmall1004:20

				int defaultOcuurenceAuidCount = defaultUbcfOccurenceAuidCountMap.get(jobInfo.getAdvId());
				minOcuurencePcodeCount = defaultOcuurenceAuidCount;

				log.info("advId={} set minOcuurencePcodeCount = {} (default.ubcf.occurence.auid.count)", jobInfo.getAdvId(), minOcuurencePcodeCount);

			}else {
				//jobInfo.getAvdId => /home/users/rpapp/home/data/rmsubmit.conf 파일내의 AvdId에 들어있지 않은 광고주

				//가중치 반올림
				//처음보는 알고리즘
				int tmpBigAuidCountWeight =  (int)Math.round((float)auidCount / 150000); //경험적으로 정한 수치 15만이 근거가 있는 것은 아님 튜닝할필요있나 튜닝포인트
				int bigAuidCountWeight = tmpBigAuidCountWeight;
				// bigAuid에 속하지 않으면 가중치는 1
				// 크기에 따른 가중치
				if(bigAuidCountWeight < 1) {
					bigAuidCountWeight = 1;
				}
				log.info("advId={} set bigAuidCountWeight = ({} -> {})", jobInfo.getAdvId(), tmpBigAuidCountWeight, bigAuidCountWeight);
				//처음보는 알고리즘
				// root(auid개수/pcode개수) * 가중치를 줌
				// why???? ==> 해석해본 결과 최소 최대 발생수를 정하는데 아이템수와 유저수를 사용한 것으로 보임
				// 예를 들면 유저가 400,000 아이템이 110,000인 lf몰에서 계산을 위해 요구되는 최소한의 발생수는 40만/11만
				// => 약 4에 루트를 씌우면 2정도 => 2에다가 2.5(40만/15만)을 곱하면 5가 됨.
				// 이렇게 원래하는건지 모르겠지만, 아마도 큰 쇼핑몰의 경우, 행렬이 너무 크게 나오기 때문에 분석을 위한 최소수를 높이고자 사용한 것으로 보임
				// 나름 리즈너블하긴 한데 잘 모르겠음 => 공식에 있을수도 없을수도?
				int autoMinOcuurencePcodeCount = auidPcodesMap.getAutoMinOcuurencePcodeCount();
				tempMinOcuurencePcodeCount = (int)Math.round(Math.sqrt(auidCount/pcodeCount) * bigAuidCountWeight);
				if(autoMinOcuurencePcodeCount > minOcuurencePcodeCount) {
					minOcuurencePcodeCount = autoMinOcuurencePcodeCount;
					log.info("advId={} set minOcuurencePcodeCount = {} (autoMinOcuurencePcodeCount)", jobInfo.getAdvId(), autoMinOcuurencePcodeCount);
				}

				if(minOcuurencePcodeCount > maxOcuurencePcodeCount) {
					minOcuurencePcodeCount = maxOcuurencePcodeCount;
					log.info("advId={} set minOcuurencePcodeCount = {} (maxOcuurencePcodeCount)", jobInfo.getAdvId(), minOcuurencePcodeCount);
				}else {
					log.info("advId={} set minOcuurencePcodeCount = {} (temp={}, auto={})", jobInfo.getAdvId(), minOcuurencePcodeCount, tempMinOcuurencePcodeCount, autoMinOcuurencePcodeCount);
				}
			}

		}


		log.info("advId={} mode={} isPc={} isClick={} size={} product_count={} auid_count={} minOcuurencePcodeCount={} tempMinOcuurencePcodeCount={}"
				, jobInfo.getAdvId(), mode, isPc, click, jobInfo.getSize(), pcodeCount, auidCount, minOcuurencePcodeCount, tempMinOcuurencePcodeCount);


		log.info("Make the co-occurence data thread start (UBCF) : thread count {}",config.getThreadUbcf());

		//ExecutorService executorService = Executors.newCachedThreadPool();
		ExecutorService executorService = Executors.newFixedThreadPool(config.getThreadUbcf());

		String filePath = String.format("%s/%s/%s.%s", config.getOutClickPath()+"_ubcf", today, jobInfo.getAdvId(), isPc ? "outp" : "outm");
		String fileRedisPath = String.format("%s/%s/%s.redis.%s", config.getOutClickPath()+"_ubcf", today, jobInfo.getAdvId(), isPc ? "outp" : "outm");

		Logger saveFileOut = CustomLoggerFactory.createLoggerGivenFileName("LOGGER_1_"+jobInfo.getAdvId()+"_"+isPc, filePath, true);
		Logger saveRedisFileOut = CustomLoggerFactory.createLoggerGivenFileName("LOGGER_2_"+jobInfo.getAdvId()+"_"+isPc, fileRedisPath, true);
		OcuurenceResult ocuurenceResult = new OcuurenceResult();
		for( int auids_1 : auidPcodesMap.getMap().keySet()) {
			//App.checkCpuLoad();
			ThreadUbcf threadUbcf = new ThreadUbcf(batchConfig,data,ocuurenceResult,saveFileOut,saveRedisFileOut,auids_1,today,minOcuurencePcodeCount);
			//Thread t = new Thread(threadUbcf,"T:"+(isPc==true?"Y":"N")+"_"+jobInfo.getAdvId()+"_"+auids_1);
			executorService.execute(threadUbcf);
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination( 1, TimeUnit.DAYS );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ocuurenceResult.getCurOcuurenceCount();
	}
}

class OcuurenceResult{
	@Getter long curOcuurenceCount = 0;
	public long addCurOcuurenceCount() {
		return ++curOcuurenceCount;
	}
}

@Slf4j
class ThreadUbcf implements Runnable{
	private Logger saveFileOut;
	private Logger saveRedisFileOut;

	//@Autowired
	private Config config;
	private RedisCluster redisCluster;
	private PcodeAuidsMap pcodeAuidsMap   = null;//baseFile (imp or conv)
	private AuidPcodesMap auidPcodesMap   = null;//baseFile (imp or conv)
	private JobInfo jobInfo;
	private String today;
	private boolean click;
	private int auids_1;
	private int minOcuurencePcodeCount;
	private boolean isPc;
	private OcuurenceResult ocuurenceResult;

	public ThreadUbcf(RecomBatchConfig batchConfig, RecomData data, OcuurenceResult ocuurenceResult, Logger saveFileOut, Logger saveRedisFileOut, int baseAuid, String today, int minOcuurencePcodeCount) {
		this.click = batchConfig.isClick();
		this.auids_1 = baseAuid;
		this.today = today;
		this.ocuurenceResult = ocuurenceResult;
		this.saveFileOut = saveFileOut;
		this.saveRedisFileOut = saveRedisFileOut;
		this.auidPcodesMap = data.getAuidPcodesMap();
		this.pcodeAuidsMap = data.getPcodeAuidsMap();
		this.jobInfo = data.getJobInfo();
		this.isPc = jobInfo.isPc();
		this.minOcuurencePcodeCount = minOcuurencePcodeCount;

		this.redisCluster = BeanUtils.getBean(RedisCluster.class);
		this.config = BeanUtils.getBean(Config.class);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		List<RecomAuid> recomAuid1List =  new ArrayList<RecomAuid>();

		Set<String> pcodeSet = auidPcodesMap.getMap().get(auids_1);

		// 최소한의 상품을 확인한 유저
		if( minOcuurencePcodeCount > pcodeSet.size()) {
			return;
		}

		int auidCount = auidPcodesMap.getMap().size();
		for( int auids_2 : auidPcodesMap.getMap().keySet()) {
			if(auids_1==auids_2)
				continue;
			//baseID의 pcode 개수 즉 본 상품 수
			int auidsCount1 = auidPcodesMap.getMap().get(auids_1).size();
			//compareID의 pcode 개수 즉 본 상품 수
			int auidsCount2 = auidPcodesMap.getMap().get(auids_2).size();

			int occurenceValue = auidPcodesMap.getSameView(auids_1,auids_2);

			if(( 3 > occurenceValue)) { //이것도 우리가 설정하는 숫자에 따라 달라짐 => 분석대상에 들어가는 최소 행동횟수는 쇼핑몰별로 지정이 되어있는데
				// 같이 본 상품수에 대한 설정은 모든 쇼핑몰에 공통으로 적용이 되어 있음 ==> 튜닝할 부분
				continue;
			}

			float cosine = (float)((float)occurenceValue/Math.sqrt(((double)auidsCount1 *  (double)auidsCount2)));
			//             같이본 상품수/루트((기준유저가 본 상품수 * 비교유저가 본 상품수))

			// 기준 유사도보다 작으면 pass
			if( cosine <= config.getFirstMinSimilarityForAuid()) {
				continue;
			}

			if(cosine < config.getMinUbcfClickSimilarity() ) { //코사인유사도가 셋팅해놓은 유사도보다 낮으면 더이상 유사유저를 찾지 않는다.
				continue;
			}

			RecomAuid recomPcode1 = new RecomAuid(auids_2, occurenceValue, cosine);
			recomAuid1List.add(recomPcode1);

			if(recomAuid1List.size() > 1) {
				recomAuid1List.sort(new Comparator<RecomAuid>() {
					public int compare(RecomAuid o1, RecomAuid o2) {

						double diffCosine =  o2.getCosine() - o1.getCosine();

						if(diffCosine > 0) {
							return 1;
						}else if(diffCosine < 0) {
							return -1;
						}else if(diffCosine==0) {
							try {
								if(auidPcodesMap.getMap().get(o1.getAuid()).size() > auidPcodesMap.getMap().get(o2.getAuid()).size())
									return 1;
							}catch(Exception e) {
								return 0;
							}
						}

						return 0;
					}
				});
			}

			if(recomAuid1List.size() > config.getMinRecomAuidCount()) {
				recomAuid1List.remove(recomAuid1List.size()-1);
			}
		}

		//Set<String> recomAuidSet          = new HashSet<String>();
		//Set<String> recomPcodeSet         = new HashSet<String>();
		UbcfRecomPcode recomPcodeSet         = new UbcfRecomPcode();
		String strRecomAuid  = "";
		String userSeePcode = "";
		for (RecomAuid recomAuid : recomAuid1List) {
			//recomAuidSet.add(pcodeAuidsMap.getDeCRC32Auid(recomAuid.getAuid()));
			strRecomAuid +=(strRecomAuid.equals("")?"":",")+pcodeAuidsMap.getDeCRC32Auid(recomAuid.getAuid())+":"+recomAuid.getCosine();
			userSeePcode += String.join(",",auidPcodesMap.getMap().get(recomAuid.getAuid())) + "|";
			recomPcodeSet.addAll(auidPcodesMap.getMap().get(recomAuid.getAuid()));
		}

		recomPcodeSet.removeAll(auidPcodesMap.getMap().get(auids_1)); //추천해줄 상품인데 기준 유저가 본 상품과 중복인 상품코드 제거
		//a유저가 1,2,3,4,5를 봄
		//광고가 나가는데 1기반으로 나감
		//근데 추천목록에 1이 들어가있어서 광고가 나갔을 때 똑같은 상품이 위 아래로 배치되어 보일 수 있음
		//상품셋만드는방법 3개 있음

		//추천이 0 이면 제거
		if(recomPcodeSet.size()==0) {
			return;
		}

		//recomPcodeSet.procSelectionPer();


		//그런데 유저기반추천시스템은 상품기반아님.... =>어떻게 보여줄 것인지 고민하기
		//String strRecomAuid  = String.join(",", recomAuidSet);
		//String strRecomPcode = String.join(",", recomPcodeSet);
		String strRecomPcode = recomPcodeSet.toString();
		String pcodes        = String.join(",", auidPcodesMap.getMap().get(auids_1));

		if(jobInfo.getAdvId().equals("nike1")){
			String seeMap = pcodes + "|" + userSeePcode;
			String seeMapredisKey = redisCluster.getRedisKey("MAP", "sim", isPc, jobInfo.getAdvId(), click, pcodeAuidsMap.getDeCRC32Auid(auids_1));
			try{
				redisCluster.saveRedis(seeMapredisKey,seeMap,config.getRedisExpireTime());
			}catch (Exception e){
				log.info(e.toString());
			}
		}

		///////  저장
		if( config.isSaveFile()) {
			//electionPcode.saveEtcFile( jobInfo.getAdvId(), pcodeAuidsMap.getDeCRC32Auid(auids_1), "{auidSize:"+recomAuid1List.size()+",auid:{"+strRecomAuid+"}}", strRecomPcode, pcodes, isPc, today, "_ubcf");
			String line = String.format("%s_%s_%s_%s_%s", jobInfo.getAdvId() , pcodeAuidsMap.getDeCRC32Auid(auids_1)
					, "{auidSize:"+recomAuid1List.size()+",auid:{"+strRecomAuid+"}}"
					, pcodes
					, strRecomPcode);
			saveFileOut.info(line);

			//electionPcode.saveRedisFile("UBCO",jobInfo.getAdvId(),pcodeAuidsMap.getDeCRC32Auid(auids_1),strRecomPcode, isPc, "sim", today, "_ubcf");
			String redisKey = redisCluster.getRedisKey("UBCO", "sim", isPc, jobInfo.getAdvId(), click, pcodeAuidsMap.getDeCRC32Auid(auids_1));
			String lineRedis = String.format("%s=>%s", redisKey , strRecomPcode);
			saveRedisFileOut.info(lineRedis);
		}

		if( config.isSaveUBCFRedis()) {
			//electionPcode.saveRedisMap("UBCO",jobInfo.getAdvId(),pcodeAuidsMap.getDeCRC32Auid(auids_1),recomPcodeSet.getRedisMap(), isPc, "sim", ElectionPcode.REDIS_EXPIRE_MODE_UBCF);
			String redisKey = redisCluster.getRedisKey("UBCO", "sim", isPc, jobInfo.getAdvId(), click, pcodeAuidsMap.getDeCRC32Auid(auids_1));
			redisCluster.saveRedisMap(redisKey,recomPcodeSet.getRedisMap(),config.getRedisUBCFExpireTime());
		}

		if(ocuurenceResult.addCurOcuurenceCount() % 10000 == 0) {
			log.info("current auid count : {}",(ocuurenceResult.getCurOcuurenceCount() + "/" + auidCount) );
		}
	}
}

