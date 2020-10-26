package com.enliple.recom3.common;

import java.util.HashMap;

public class Crc32Table {
	private static Crc32Table instance = null;
	
	private HashMap<String, Integer> map = new HashMap<>();
	
	private Crc32Table() {
	}

	public static Crc32Table getInstance() {
		if( instance == null) {
			synchronized (java.lang.Object.class) {
				if( instance == null) instance = new Crc32Table();
			}
		}

		return instance;
	}
	
	public synchronized int get( String var) {
		Integer value = map.get(var);
		if( value != null) return value;
		
		value = CRC32.get(var);
		map.put( var,  value);
		return value;
	}
}
