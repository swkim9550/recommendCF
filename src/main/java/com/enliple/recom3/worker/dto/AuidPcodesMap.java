package com.enliple.recom3.worker.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.enliple.recom3.common.CRC32;

import lombok.Getter;

public class AuidPcodesMap {
	@Getter private HashMap<Integer, Set<String>> map = new HashMap<>();
	
	public void put( String auidStr, String pcode) {
		int auid = CRC32.get( auidStr);
		
		Set<String> pcodeSet = map.get( auid);
		
		if( pcodeSet != null) {
			if(pcodeSet.contains(pcode)) return;
			pcodeSet.add( pcode );
			return;
		}
		
		pcodeSet = new HashSet<String>();
		pcodeSet.add( pcode );
		map.put( auid,  pcodeSet);
	}
	public int getAutoMinOcuurencePcodeCount() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int auid : map.keySet()) {
			list.add(map.get(auid).size());
		}
		Collections.sort(list, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return ((Comparable) o2).compareTo(o1);
			}
		});
		int index = (int) (list.size()*0.75);
		return list.size()==0?0:list.get(index);
	}
	public int getSameView(int auid_1, int auid_2) {
		int sameView = 0;
		Set<String> pcodeSet1 = this.map.get(auid_1);
		Set<String> pcodeSet2 = this.map.get(auid_2);
		if(pcodeSet1.size()<=pcodeSet2.size()) {
			for(String pcode : pcodeSet1) {
				if(pcodeSet2.contains(pcode)) {
					sameView++;
				}
			}
		}else{
			for(String pcode : pcodeSet2) {
				if(pcodeSet1.contains(pcode)) {
					sameView++;
				}
			}			
		}
		return sameView;
	}
}
