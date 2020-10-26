package com.enliple.recom3.jpa.db1.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.codec.binary.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="job_result")
public class JobResult implements Serializable {
	@Builder
	public JobResult(JobResultKey key, long pcodeCount, long auidCount, LocalDateTime createTime) {
		this.key = key;
		this.pcodeCount = pcodeCount;
		this.auidCount = auidCount;
		this.createTime = createTime;
	}
	
	//이 생성자가 없으면 JPA에서 에러발생
	public JobResult() {
		
	}

	@EmbeddedId
	private JobResultKey key;
		
	@Column(name="pcode_count")
	private long pcodeCount;
	
	@Column(name="auid_count")
	private long auidCount;
	
	@Column(name="create_time")
	private LocalDateTime createTime;
	
	public String getAdvId() {
		return this.key.getAdvId();
	}
	
	public boolean isPc() {
		return StringUtils.equals("Y", this.key.getIsPc());
	}
}
