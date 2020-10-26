package com.enliple.recom3.jpa.db1.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class JobInfo {
	protected JobInfoKey key;
	protected String status;
	protected String recomeType; //배치 타입
	protected long pcodeAuidMap; // 상품을 본 유저 수
	protected long auidPcodeMap; //유저가 본 상품 수
	protected long occurenceDataMap; //계산을 위해 발생한 데이터의 총 수
	protected long pcodeMapSize; //광고주 전체 상품 수
	protected long size;
	protected long readFileDataCount;
	protected LocalDateTime createDate;
	protected LocalDateTime updateDate;
	protected int recomTotalCount; // 전체 추천 데이터 카운트  ex) 전체 생성된 추천 가운트 같은카테고리 + 다른카테고리 전체 카운트
	protected int advIdCrscCount; // 광고주의 같은 카테고리 카운트  ex) advId : shopbot2 의 같은 카테고리 추천데이터가 하나라도 있으면 1개
	protected int advIdCrocCount; // 광고주의 다른 카테고리 카운트
	protected Map<String,Integer> recomCountmap = new HashMap<>();//광고주의 같은 카테고리 ,다른 카테고리 각각 카운트에 카운트 ex)18개의 다른 카테고리가 만들어진 카운트 , 17개의 다른 카테고리가 만들어진 카운트
	
	public String getAdvId() {
		return this.key.getAdvId();
	}
	
	public boolean isPc() {
		return StringUtils.equals("Y", this.key.getIsPc());
	}
}
