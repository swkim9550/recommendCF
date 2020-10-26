package com.enliple.recom3.worker.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.enliple.recom3.common.CRC32;
import lombok.Getter;

public class PcodeAuidsMap {
	//private Crc32Table crc32 = Crc32Table.getInstance();
	
	@Getter private HashMap<String, Set<Integer>> map = new HashMap<>();
	private HashMap<Integer, String> auidMap = new HashMap<>();

	public void put(String pcode, String auidStr) {
		int auid = CRC32.get(auidStr);
		if(auidMap.get(auid)==null) {
			auidMap.put(auid, auidStr);
		}

		Set<Integer> auidSet = map.get(pcode);
		if( auidSet != null) {
			if( auidSet.contains(auid)) {
				return;
			}
			auidSet.add(auid);
			return;
		}
		
		auidSet = new HashSet<Integer>();
		auidSet.add(auid);
		map.put(pcode,  auidSet);
	}

	public String getDeCRC32Auid(Integer auid) {
		return auidMap.get(auid);

	}

	public int getPcodeAuidCount(String pcode) {
		Set<Integer> pcodeSet = map.get(pcode);
		return pcodeSet==null?0:pcodeSet.size();
	}
	
	public int getSameView(String pcode_1, String pcode_2) {
		int sameView = 0;
		Set<Integer> userSet1 = this.map.get(pcode_1);
		Set<Integer> userSet2 = this.map.get(pcode_2);
		if(userSet1.size()<=userSet2.size()) {
			for(int userId : userSet1) {
				if(userSet2.contains(userId)) {
					sameView++;
				}
			}
		}else{
			for(int userId : userSet2) {
				if(userSet1.contains(userId)) {
					sameView++;
				}
			}			
		}
		return sameView;
	}		
}
