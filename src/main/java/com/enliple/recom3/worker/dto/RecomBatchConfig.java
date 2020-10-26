package com.enliple.recom3.worker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecomBatchConfig {
	String statDateTime;
	String mode;
	String suffix;
	String advId;
	boolean click;
	boolean forceGc;
	boolean skip;
}
