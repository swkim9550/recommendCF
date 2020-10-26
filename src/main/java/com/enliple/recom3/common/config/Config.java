package com.enliple.recom3.common.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class Config {
	//@Getter private PropertiesConfiguration config;
	@Value("${in.file.path}")
	@Getter private String inFilePath;
	@Value("${recom.file.path}")
	@Getter private String recomFilePath = "/home/dmp/out.txt";
	@Value("${recomcatemap.file.path}")
	@Getter private String recomCateFilePath = "/home/dmp/out.txt";
	@Value("${conv.file.path}")
	@Getter private String convFilePath;
	@Value("${cart.file.path}")
	@Getter private String cartFilePath;
	@Value("${clk.file.path}")
	@Getter private String clkFilePath;

	@Value("${telegram.cfengine.local.server}")
	@Getter private String telegramServer = "127.0.0.1:26000";
	@Value("${telegram.cfengine.notice.resturl}")
	@Getter private String telegramSendUrlNotice = "http://127.0.0.1:8080/bot_cfengine";
	@Value("${telegram.cfengine.exception.resturl}")
	@Getter private String telegramSendUrlException = "http://127.0.0.1:8080/bot_cfengine_exception";
	@Value("${telegram.cfengine.send}")
	@Getter private boolean telegramSend = false;

	@Value("${pc.period}")
	@Getter private int period = 21;
	@Value("${mobile.period}")
	@Getter private int mobilePeriod  = 14;
	@Value("${ubcf.period}")
	@Getter private int ubcfPeriod = 7;

	@Value("${thread.basic.ubcf}")
	@Getter private int threadUbcf = 17;
	@Value("${thread.basic.ibcf}")
	@Getter private int threadIbcf = 17;

	@Value("${mysql.driver}")
	@Getter private String mysqlDriver;
	@Value("${mysql.url}")
	@Getter private String mysqlUrl;
	@Value("${mysql.user}")
	@Getter private String mysqlUser;
	@Value("${mysql.password}")
	@Getter private String mysqlPassword;

	@Value("${mysql.url.for.Product}")
	@Getter private String mysqlUrlForProduct;
	@Value("${mysql.user.for.Product}")
	@Getter private String mysqlUserForProduct;
	@Value("${mysql.password.for.Product}")
	@Getter private String mysqlPasswordForProduct;

	@Value("${executor.hosts}")
	@Getter private String[] executorHosts;
	@Value("${separate.file}")
	@Getter private boolean separateFile = true;
	@Value("${submit.job}")
	@Getter private boolean submitJob = true;
	@Value("${executor.number}")
	@Getter private int executorNumber = 1;
	@Value("${advid.list}")
	@Getter private String[] advIdList;

	@Value("${out.click.path}")
	@Getter private String outClickPath = "/home/dmp/out.txt";
	@Value("${out.avg.click.path}")
	@Getter private String outClickPathForAvgClick = "/home/dmp/out.txt";
	@Value("${out.conv.path}")
	@Getter private String outConvPath = "/home/dmp/out.txt";
	@Value("${out.avg.conv.path}")
	@Getter private String outConvPathForAvgClick = "/home/dmp/out.txt";
	@Value("${out.click.ubcf.path}")
	@Getter private String outClickUBCFPath = "/home/ubcf/out.txt";
//	2020.07.24 김기영과장님 요청사항(대표님지시)으로 제거
//	@Value("${pass.check.count.advid}")
//	@Getter private String passCheckCountAdvId = "";

	@Value("${max.recommend.pcode}")
	@Getter private int maxRecomPocde = 12;
	@Value("${min.auid.count:1}")
	@Getter private int minAuidCount = 1;
	@Value("${min.impression.count:3}")
	@Getter private int minImpressionCount = 3;

	@Value("${redis.hosts}")
	@Getter @Setter private String[] redisHosts;

	@Value("${save.file}")
	@Getter private boolean saveFile = false;
	@Value("${save.redis}")
	@Getter private boolean saveRedis = true;
	@Value("${save.ubcf.redis}")
	@Getter private boolean saveUBCFRedis = true;

	@Value("${redise.expire.time}")
	@Getter private int redisExpireTime;
	@Value("${redise.ubcf.expire.time}")
	@Getter private int redisUBCFExpireTime;

	@Value("${min.ocuurence.pcode.count}")
	@Getter private int minOcuurencePcodeCount = 3;
	@Value("${max.ocuurence.pcode.count}")
	@Getter private int maxOcuurencePcodeCount = 10;
	//@Value("${max.ocuurence.pcode.count}")
	@Getter private int ubcfMaxOcuurencePcodeCount = 20;

	@Value("${min.ocuurence.auid.count}")
	@Getter private int minOcuurenceAuidCount = 3;
	@Value("${max.ocuurence.auid.count}")
	@Getter private int maxOcuurenceAuidCount = 10;
	@Value("${max.second.ocuurence.auid.count}")
	@Getter private int maxSecodeOcuurenceAuidCount = 4;

	@Value("${min.conv.ocuurence.auid.count}")
	@Getter private int minConversionOcuurenceAuidCount = 1;
	@Value("${max.conv.ocuurence.auid.count:3}")
	@Getter private int maxConversionOcuurenceAuidCount = 3;

	@Value("${min.first.auid.similarity:0.2}")
	@Getter private double firstMinSimilarityForAuid=0.2;
	@Value("${min.first.similarity}")
	@Getter private double firstMinSimilarity=0;
	@Value("${min.second.similarity}")
	@Getter private double secondMinSimilarity=0;
	@Value("${min.third.similarity}")
	@Getter private double thirdMinSimilarity=0;

	@Value("${min.conv.similarity}")
	@Getter private double conversionMinSimilarity=0.01;
	@Value("${min.ubcf.click.similarity}")
	@Getter private double minUbcfClickSimilarity=0.1;
	@Value("${min.recom.auid.count}")
	@Getter private int minRecomAuidCount = 5;

	@Value("${auto.auid.count}")
	@Getter private boolean autoAuidCount = false;
	@Value("${add.category.weight}")
	@Getter private boolean addCategoryWeight = true;
	@Value("${run.count.recommend}")
	@Getter private boolean runCountRecommend=true;
	@Value("${run.similarity.recommend}")
	@Getter private boolean runSimilarityRecommend=true;
	@Value("${run.similarity.recommend.ubcf}")
	@Getter private boolean runSimilarityRecommendUBCF=true;

	@Getter private Map<String, Integer> defaultOccurenceAuidCountMap = new HashMap<String, Integer>();
	@Getter private Map<String, Integer> defaultUbcfOccurenceAuidCountMap = new HashMap<String, Integer>();
	@Getter private Map<String, Boolean> defaultUbcfPassAuidMap 		= new HashMap<String, Boolean>();
	@Getter private Map<String, Boolean> defaultIbcfPassAuidMap 		= new HashMap<String, Boolean>();
	@Getter private Map<String, String> largeAdvIdMap		= new HashMap<String, String>();

	@Value("${default.occurence.auid.count}")
	String[]  defaultOccurenceAuidCountArray;
	@Value("${default.ubcf.occurence.auid.count}")
	String[]  defaultUbcfOccurenceAuidCountArray;
	@Value("${default.ubcf.pass.auid}")
	String[]  defaultUbcfPassAuidArray;
	@Value("${default.ibcf.pass.auid}")
	String[]  defaultIbcfPassAuidArray;
	@Value("${large.adverid.list}")
	String[]  largeAdvIdArray;

	@PostConstruct
	private void init1() {
		for(String defaultOccurenceAuidCount : defaultOccurenceAuidCountArray ) {
			String[] datas = defaultOccurenceAuidCount.split(":");
			defaultOccurenceAuidCountMap.put(datas[0], Integer.parseInt(datas[1]));
		}
		if(defaultUbcfOccurenceAuidCountArray != null && defaultUbcfOccurenceAuidCountArray.length > 0) {
			for(String defaultOccurenceAuidCount : defaultUbcfOccurenceAuidCountArray ) {
				String[] datas = defaultOccurenceAuidCount.split(":");

				if(datas.length == 2) {
					defaultUbcfOccurenceAuidCountMap.put(datas[0], Integer.parseInt(datas[1]));
				}
			}
		}
		if(defaultUbcfPassAuidArray != null && defaultUbcfPassAuidArray.length > 0) {
			for(String defaultPassAuid : defaultUbcfPassAuidArray ) {
				defaultUbcfPassAuidMap.put(defaultPassAuid, true);
			}
		}
		if(defaultIbcfPassAuidArray != null && defaultIbcfPassAuidArray.length > 0) {
			for(String defaultPassAuid : defaultIbcfPassAuidArray ) {
				defaultIbcfPassAuidMap.put(defaultPassAuid, true);
			}
		}
		for(String largeAdvId : largeAdvIdArray ) {
			String[] datas = largeAdvId.split(",");
			String advId = datas[0].split(":")[0];
			String isPc = datas[0].split(":")[1];
			largeAdvIdMap.put(advId+isPc,isPc);
		}
	}
}
