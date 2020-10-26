package com.enliple.recom3.worker.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
@Slf4j
public class PcodeCateMap {
    @Getter
    private HashMap<String, String> cateMap = new HashMap<>();

    public void put( String pcode, String cate) {
        //불확실한 카테고리는 가중치 계산하지 않기 위해서.
        if(cateMap.containsKey(pcode) | pcode.equals("_unknown") | pcode.equals("nocate")){
            return;
        }

        if(pcode.equals("128647289")){
            log.info("safsaf");
        }
        cateMap.put(pcode,cate);
    }

    public String get(String pcode) {
        return cateMap.get(pcode);
    }
}
