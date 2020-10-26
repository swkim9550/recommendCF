package com.enliple.recom3.jpa.db1.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.codec.binary.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="job_list")
public class JobList extends JobInfo implements Serializable {
	
	@EmbeddedId
	private JobInfoKey key;
	
	@Column(name="status",updatable=true)
	private String status;
	
	@Column(name="size",updatable=false)
	private long size;
	
	@Column(name="create_date",updatable=true)
	private LocalDateTime createDate;
	
	@Column(name="update_date",updatable=true)
	private LocalDateTime updateDate;

	public String getAdvId() {
		return this.key.getAdvId();
	}
	
	public boolean isPc() {
		return StringUtils.equals("Y", this.key.getIsPc());
	}
	
	public String toString() {
		return String.format("JobList(advId=%s, isPc=%s, size=%s)", key.getAdvId(), this.isPc(), this.size);
	}	
}
