package com.enliple.recom3.worker.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UbcfRecomPcode {
	private final String PERCENT_FORMAT = "%.4f";
	private Map<String, String> mapPcodeCount = new HashMap<String, String>();
	private Map<String, String> mapPcodeSelection = new HashMap<String, String>();

	public void addAll(Set<String> set) {
		// TODO Auto-generated method stub
		for (String pcode : set) {
			if (mapPcodeCount.get(pcode) == null) {
				mapPcodeCount.put(pcode, String.valueOf(1));
			} else {
				//1을 더한다.
				mapPcodeCount.put(pcode, String.valueOf(Integer.parseInt(mapPcodeCount.get(pcode)) + 1));
			}
		}
	}

	public void removeAll(Set<String> set) {
		// TODO Auto-generated method stub
		for (String pcode : set) {
			if (mapPcodeCount.get(pcode) != null) {
				mapPcodeCount.remove(pcode);
			}
		}
	}
	public Map<String,String> getRedisMap(){
		return mapPcodeCount;
	}
	public void procSelectionPer() {
		Set<String> keySet = mapPcodeCount.keySet();
		Map<String, Double> mapDouble = new HashMap<String, Double>();
		
		//ppt 1. A유저에게 추천될 상품 수를 센다
		int pcodeCount = keySet.size();
		
		//ppt 3. 상품이 독립적으로 추천될 확률을 구한다.
		double firstSelectPercent = ((double)1)/((double)pcodeCount);
		double weightSelectionPercentSum = 0d;
		for(String key : keySet) {
			//ppt 2. 추천될 상품별 CO-VIEW-CNT를 구한다.
			int pcodeViewCount = Integer.parseInt(mapPcodeCount.get(key));
			double weight = 0.4d;
			if(pcodeViewCount>=4) {
				//co-view-cnt 가 4이상이면 0.6 ELSE 0.4
				weight = 0.6d;
			}
			//ppt 4. CO-VIEW-CNT 별 가중 확률을 구한다
			double weightPercent = firstSelectPercent * weight;
			
			//ppt 5. 최초 뽑힐 확률과 가중 확률을 더한다.
			double weightSelectionPercent = firstSelectPercent + weightPercent;
			
			//System.out.println(String.format("pcode : %s , viewCount : %s, firstSelectPercent : %.8f, weight : %.1f, weightPercent : %.8f, weightSelectionPercent : %.8f "
			//		, key, pcodeViewCount,firstSelectPercent,weight,weightPercent,weightSelectionPercent));
			mapDouble.put(key, weightSelectionPercent);
			
			weightSelectionPercentSum += weightSelectionPercent;
		}

		//System.out.println(String.format("                                                                                                   "
		//		+"SumWeightSelectionPercent : %.8f "
		//		, weightSelectionPercentSum));
		
		//double check = 0;
		for(String key : mapDouble.keySet()) {
			double weightSelectionPercent = mapDouble.get(key);
			double finalPercent = (weightSelectionPercent) / (weightSelectionPercentSum);
			//System.out.println(String.format("pcode : %s , weightSelectionPercent : %.8f, finalPercent : %.8f"
			//		, key, weightSelectionPercent, finalPercent));
			//check += finalPercent;
			
			mapPcodeSelection.put(key, String.format(PERCENT_FORMAT, finalPercent));
		}		
	}
	public int size(){
		return mapPcodeCount.size();
	}
	public String toString() {
		String strListValue = "";
		List<String> list = new ArrayList<String>();

		list.addAll(mapPcodeCount.keySet());		
		Collections.sort(list, new Comparator<String>() {
			public int compare(String o1, String o2) {
				int v1 = Integer.parseInt(mapPcodeCount.get(o1));
				int v2 = Integer.parseInt(mapPcodeCount.get(o2));
				return ((Comparable) v2).compareTo(v1);
			}
		});	
		//Collections.reverse(list); // 주석시 오름차순
		double totalPercent = 0;
		for(String pcode : list) {
			int pcodeCount = Integer.parseInt(mapPcodeCount.get(pcode));
			strListValue += (strListValue.equals("")?"":",")+pcode+":"+pcodeCount;
			if(mapPcodeSelection.get(pcode)!=null) {
				double selectPer = Double.parseDouble(mapPcodeSelection.get(pcode));
				strListValue += "^"+selectPer;
				totalPercent+=selectPer;
			}
		}
		strListValue = "[Pcode]count:"+mapPcodeCount.size()+",sum:"+String.format(PERCENT_FORMAT,totalPercent)+"=>"+strListValue;
		return strListValue;
	}
}
