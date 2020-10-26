package com.enliple.recom3.worker.ibcf;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.common.constants.ConstantsIBCF;
import com.enliple.recom3.dao.RedisCluster;
import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import com.enliple.recom3.jpa.db1.domain.JobInfo;
import com.enliple.recom3.jpa.db2.domain.AdverPrdtStandardCate;
import com.enliple.recom3.worker.dto.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Getter
public class RecomCaculatorIBCFV1 {

    @Autowired
    private Config config;
    @Autowired
    private RedisCluster redisCluster;

    /** +
     *
     * @param pcodeAuidsMap
     * @param auidPcodesMap
     * @param minOcuurenceAuidCount
     * @param curOcuurencePcodeCount
     * @param adverID
     * @return
     */
    public Map<String, Integer> getOccurence(PcodeAuidsMap pcodeAuidsMap, AuidPcodesMap auidPcodesMap,
                                             int minOcuurenceAuidCount, int curOcuurencePcodeCount, String adverID){
        Map<String, Integer> occurenceDataMap = new HashMap<>();
        int curPcodeCount = 0;
        for( String pcode : pcodeAuidsMap.getMap().keySet()) {
            Set<Integer> auidSet = pcodeAuidsMap.getMap().get(pcode);
            ++curPcodeCount;

            // 최소한의 인원이 확인한 상품
            if( minOcuurenceAuidCount > auidSet.size()) {
                continue;
            }
            // 동시 발생 데이터 수집
            for( Integer auid : auidSet) {
                Set<String> pcodeSet = auidPcodesMap.getMap().get(auid);

                if( pcodeSet == null || pcodeSet.size() == 0) {
                    continue;
                }

                for( String recomPcode : pcodeSet) {
                    if(
                        //(occurenceDataMap.get(recomPcode + "_" + pcode) != null) //1018 djlee
                            (occurenceDataMap.get(recomPcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + pcode) != null)
                                    || ( StringUtils.equals(pcode, recomPcode))) {
                        continue;
                    }
                    //String occurenceKey =  pcode + "_" + recomPcode; //1018 djlee
                    String occurenceKey =  pcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + recomPcode;
                    Integer occurenceValue = occurenceDataMap.get(occurenceKey);

                    if(occurenceValue == null) {
                        occurenceDataMap.put(occurenceKey, 1);
                    }else {
                        occurenceDataMap.put(occurenceKey, occurenceValue + 1);
                    }
                }
            }
            curOcuurencePcodeCount++;
        }
        log.info("current count : {}, Co-occurence Pcode Count : {} - {}",curPcodeCount,curOcuurencePcodeCount,adverID);
        return occurenceDataMap;
    }

    public Map<String, Integer> getOccurence2(PcodeAuidsMap pcodeAuidsMap, AuidPcodesMap auidPcodesMap,
                                              int minOcuurenceAuidCount, int curOcuurencePcodeCount, String adverID, JobInfo jobInfo){
        Map<String, Integer> occurenceDataMap = new HashMap<>();

        log.info("-------pcodeAuidsMap{}--{}---------",pcodeAuidsMap.getMap().size(),adverID);
        log.info("-------auidPcodesMap{}--{}---------",auidPcodesMap.getMap().size(),adverID);

        int curPcodeCount = 0;
        for( String pcode : pcodeAuidsMap.getMap().keySet()) {
            Set<Integer> auidSet = pcodeAuidsMap.getMap().get(pcode);
            ++curPcodeCount;

            // 최소한의 인원이 확인한 상품
            if( minOcuurenceAuidCount > auidSet.size()) {
                continue;
            }
            // 동시 발생 데이터 수집
            for( Integer auid : auidSet) {
                Set<String> pcodeSet = auidPcodesMap.getMap().get(auid);

                if( pcodeSet == null || pcodeSet.size() == 0) {
                    continue;
                }

                for( String recomPcode : pcodeSet) {
                    if(
                        //(occurenceDataMap.get(recomPcode + "_" + pcode) != null) //1018 djlee
                            (occurenceDataMap.get(recomPcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + pcode) != null)
                                    || ( StringUtils.equals(pcode, recomPcode))) {
                        continue;
                    }
                    //String occurenceKey =  pcode + "_" + recomPcode; //1018 djlee
                    String occurenceKey =  pcode + ConstantsIBCF.SPLIT_CODE_IBCF_CHAR + recomPcode;
                    Integer occurenceValue = occurenceDataMap.get(occurenceKey);

                    if(occurenceValue == null) {
                        occurenceDataMap.put(occurenceKey, 1);
                    }else {
                        occurenceDataMap.put(occurenceKey, occurenceValue + 1);
                    }
                }
            }
            curOcuurencePcodeCount++;
        }
        log.info("current count : {}, Co-occurence Pcode Count : {} - {}",curPcodeCount,curOcuurencePcodeCount,adverID);
        jobInfo.setPcodeAuidMap(pcodeAuidsMap.getMap().size());
        jobInfo.setAuidPcodeMap(auidPcodesMap.getMap().size());
        return occurenceDataMap;
    }



    /** +
     *
     * @param occurenceDataMap
     * @param pcodeAuidsMap
     * @param minOcuurenceAuidCount
     * @param productMap
     * @param jobInfo
     * @return
     */
    public Map<String,List<RecomPcode>> getSimilarity(Map<String, Integer> occurenceDataMap, PcodeAuidsMap pcodeAuidsMap,
                                                      int minOcuurenceAuidCount, HashMap<String, ProductDto> productMap,
                                                      RecomBatchConfig batchConfig, JobInfo jobInfo){
        boolean aiCateMode = batchConfig.getMode().equals("ibcf_click_ai_category") ? true : false;
        Map<String,List<RecomPcode>> recomPcodeMap =  new HashMap<String,List<RecomPcode>>();
//        int count = 0;
        for(String occurenceKey :  occurenceDataMap.keySet()) {
//            count++;
//            log.info(String.valueOf(count));
            String[] pcodes = occurenceKey.split(ConstantsIBCF.SPLIT_CODE_IBCF_CHAR);
            int pcode1 = 0;
            int pcode2 = 0;
            try{
                pcode1 = pcodeAuidsMap.getMap().get(pcodes[0]).size();
                pcode2 = pcodeAuidsMap.getMap().get(pcodes[1]).size();
            }catch (Exception e){
                log.info(e.toString());
            }

            Integer occurenceValue = occurenceDataMap.get(occurenceKey);

            // 최소한의 인원이 같이 확인한 상품
            if(occurenceValue == null) {
                continue;
            }else if(( minOcuurenceAuidCount > occurenceValue)) {
                //occurenceDataMap.remove(occurenceKey);
                continue;
            }

            /////////
            float cosine = (float)((float)occurenceValue/Math.sqrt(((double)pcode1 *  (double)pcode2)));

            if( cosine <= config.getFirstMinSimilarity()) {
                //occurenceDataMap.remove(occurenceKey);
                continue;
            }

            // 같은 카테고리 가중치
            if(config.isAddCategoryWeight()) {
                String pcode1Category = productMap.get(pcodes[0])!=null?productMap.get(pcodes[0]).getCategory():"";
                String pcode2Category = productMap.get(pcodes[1])!=null?productMap.get(pcodes[1]).getCategory():"";

                if(StringUtils.isNotEmpty(pcode1Category) && StringUtils.isNotEmpty(pcode2Category) && !"nocate".equals(pcode1Category)){ // 비교가 안되는 카테고리는 패스
                    if(StringUtils.equals(pcode1Category, pcode2Category)) { // 카테고리가 같으면
                        cosine = cosine + (aiCateMode ? 2 : 1); // AI 카테고리 모드 시에는 2, 광고주 카테고리 모드 시에는 1을 더해줌
                    } else { // 카테고리가 같지 않을 경우
                        if(aiCateMode) { // AI 카테고리 모드 시에는 상위 카테고리를 확인하여 같으면 1을 더해준다
                            // category2(상위 카테고리) 비교
                            // 예 : pcode1(신발>여성신발>구두), pcode2(신발>여성신발>운동화)
                            // (신발>여성신발) 처럼 상위 카테고리를 확인해서 코사인 유사도 +1
                            pcode1Category = productMap.get(pcodes[0]).getUpperCategory();
                            pcode2Category = productMap.get(pcodes[1]).getUpperCategory();
                            if(pcode1Category.equals(pcode2Category)) {
                                cosine = cosine + 1;
                            }
                        }
                    }
                }
            }

            // 0.12 이상만 사용이므로 '<' 로 변경해야 하지 않나?? 확인 필요. 위에서 체크해도 되지 않나?
            if(cosine <= config.getSecondMinSimilarity()) {
                continue;
            }

            List<RecomPcode> recomPcode1List = recomPcodeMap.get(pcodes[0]);
            List<RecomPcode> recomPcode2List = recomPcodeMap.get(pcodes[1]);

            if(recomPcode1List == null) {
                recomPcode1List = new ArrayList<RecomPcode>();
                recomPcodeMap.put(pcodes[0], recomPcode1List);

            }

            if(recomPcode2List == null) {
                recomPcode2List = new ArrayList<RecomPcode>();
                recomPcodeMap.put(pcodes[1], recomPcode2List);

            }

            RecomPcode recomPcode1 = new RecomPcode(pcodes[0], occurenceValue, cosine);
            RecomPcode recomPcode2 = new RecomPcode(pcodes[1], occurenceValue, cosine);

            recomPcode1List.add(recomPcode2);
            recomPcode2List.add(recomPcode1);
        }
        jobInfo.setOccurenceDataMap(occurenceDataMap.size());
        jobInfo.setPcodeMapSize(productMap.size());
        return recomPcodeMap;
    }

    /** +
     *
     * @param recomPcodeMap
     * @param productMap
     * @param maxOcuurenceAuidCount
     * @param isPc
     * @param batchConfig
     * @param adverId
     * @param curOcuurencePcodeCount
     * @param jobInfo
     * @return
     */
    public int getSimilaritySort(Map<String, List<RecomPcode>> recomPcodeMap,
                                 HashMap<String, ProductDto> productMap, int maxOcuurenceAuidCount, boolean isPc, RecomBatchConfig batchConfig, String adverId,
                                 int curOcuurencePcodeCount, JobInfo jobInfo){
        int recomTotalCount = 0;
        int advIdCrscCount = 0;
        int advIdCrocCount = 0;
        Map<String, Integer> recomCountmap = new HashMap<>();
        for(String pcode: recomPcodeMap.keySet() ) {
            List<RecomPcode> recomPcodeList = recomPcodeMap.get(pcode);

            if(recomPcodeList.size() > 1) {
                recomPcodeList.sort(new Comparator<RecomPcode>() {
                    public int compare(RecomPcode o1, RecomPcode o2) {

                        double diffCosine =  o2.getCosine() - o1.getCosine();

                        if(diffCosine > 0) {
                            return 1;
                        }else if(diffCosine < 0) {
                            return -1;
                        }

                        return 0;
                    }
                });
            }

            // 카테고리별 (같은카테고리, 다른카테고리) 상품 정리
            int cwSameCategoryCount = 0;
            int srSameCategoryCount = 0;

            int cwOtherCategoryCount = 0;
            int srOtherCategoryCount = 0;

            StringBuilder cwSameCategoryBuffer = new StringBuilder();
            StringBuilder cwOtherCategoryBuffer = new StringBuilder();

            StringBuilder srSameCategoryBuffer = new StringBuilder();
            StringBuilder srOtherCategoryBuffer = new StringBuilder();

            for (RecomPcode recomPcode : recomPcodeList) {

                // 같은 카테고리
                if(recomPcode.getCosine() > 1) {

                    // 본상품용 (Second Count)
                    if( ++srSameCategoryCount <= config.getMaxRecomPocde() ) {

                        if( srSameCategoryBuffer.length() > 0) {
                            srSameCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
                        }

                        srSameCategoryBuffer.append( recomPcode.getPcode());
                    }

                    // 장바구니용
                    if(recomPcode.getOccurenceValue()  >= maxOcuurenceAuidCount) {

                        if( ++cwSameCategoryCount <= config.getMaxRecomPocde() ) {

                            if( cwSameCategoryBuffer.length() > 0) {
                                cwSameCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
                            }

                            cwSameCategoryBuffer.append( recomPcode.getPcode());
                        }
                    }
                    // 다른 카테고리
                }else {

                    if( ++srOtherCategoryCount <= config.getMaxRecomPocde() ) {

                        if( srOtherCategoryBuffer.length() > 0) {
                            srOtherCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
                        }

                        srOtherCategoryBuffer.append( recomPcode.getPcode());
                    }

                    // 장바구니용
                    if(recomPcode.getOccurenceValue()  >= maxOcuurenceAuidCount && recomPcode.getCosine() >= config.getThirdMinSimilarity() ) {
                        if( ++cwOtherCategoryCount <= config.getMaxRecomPocde() ) {

                            if( cwOtherCategoryBuffer.length() > 0) {
                                cwOtherCategoryBuffer.append(ConstantsIBCF.APPEND_CODE);
                            }

                            cwOtherCategoryBuffer.append( recomPcode.getPcode());
                        }
                    }
                }

                if( (cwSameCategoryCount >= config.getMaxRecomPocde()) && (cwOtherCategoryCount >= config.getMaxRecomPocde())
                        && (srSameCategoryCount >= config.getMaxRecomPocde()) && (srOtherCategoryCount >= config.getMaxRecomPocde())
                ) {
                    break;
                }
            }


            if( srSameCategoryBuffer.length() == 0 && srOtherCategoryBuffer.length() == 0) {
                continue;
            }

            ///////  저장
            String recommendProductList = srSameCategoryBuffer.toString();

            if(srOtherCategoryBuffer.length() != 0) {
                recommendProductList += ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST + srOtherCategoryBuffer.toString();
            }

            ////// SR
            String code = redisCluster.getCodeForPlatform("sim", isPc, batchConfig.isClick());

            //2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
            //if(config.isSaveRedis()) saveRedis( "SR", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

            // 구매 데이터 조회후 합치기
            String COkey = String.format( "%s%s_%s_%s", "CO", code, adverId, pcode);
            String COdata = redisCluster.getJc().get(COkey);


            // 구매 데이터가 있을경우(COSR)
            //recommendProductList = getMergeCOdatas(isPc, pcode, srSameCategoryBuffer, srOtherCategoryBuffer,recommendProductList, COdata);
            //2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
            //if(config.isSaveRedis()) saveRedis( "COSR", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

            ////// CW
            recommendProductList = cwSameCategoryBuffer.toString();
            if(cwOtherCategoryBuffer.length() != 0) {
                recommendProductList += "_" + cwOtherCategoryBuffer.toString();
            }

            //2019-09-24 메모리 부족으로 제거 and 사용안하는 코드
            //saveRedis( "CW", jobInfo.getAdvId(), pcode, recommendProductList, isPc, "sim");

            // 구매 데이터가 있을경우(COCW)
            recommendProductList = getMergeCOdatas(isPc, pcode, cwSameCategoryBuffer, cwOtherCategoryBuffer,recommendProductList, COdata);

            if(recommendProductList==null || recommendProductList.equals(""))
                continue;


            //운영에서는 쓰고있지 않음.
            if(config.isSaveFile()) {
                String recommendProductListToFile = "";
                if (!recommendProductList.equals("")) {
                    String strPcodeSameOther[] = recommendProductList.split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST);
                    String strSame = "";
                    String strOther = "";
                    if (strPcodeSameOther.length > 0) {
                        String pcodes[] = strPcodeSameOther[0].split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE);
                        for (String _pcode : pcodes) {
                            for (int i = 0; i < recomPcodeList.size(); i++) {
                                if (_pcode.equals(recomPcodeList.get(i).getPcode())) {
                                    if (strSame.length() > 0)
                                        strSame += "|";
                                    //String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
                                    String pcode2Category = productMap.get(_pcode) != null ? "[" + productMap.get(_pcode).getCategory() + "]" : "";
                                    strSame += String.format("%s%s:%s:%.4f", _pcode, pcode2Category, recomPcodeList.get(i).getOccurenceValue(), recomPcodeList.get(i).getCosine());
                                }
                            }
                        }
                    }
                    if (strPcodeSameOther.length > 1) {
                        String pcodes[] = strPcodeSameOther[1].split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE);
                        for (String _pcode : pcodes) {
                            for (int i = 0; i < recomPcodeList.size(); i++) {
                                if (_pcode.equals(recomPcodeList.get(i).getPcode())) {
                                    if (strOther.length() > 0)
                                        strOther += "|";
                                    //String pcode2Category = pcodeCategoryMap.get(_pcode).get("CATE").equals("")?"[]":"["+pcodeCategoryMap.get(_pcode).get("CATE")+"]";
                                    String pcode2Category = productMap.get(_pcode) != null ? "[" + productMap.get(_pcode).getCategory() + "]" : "[]";
                                    strOther += String.format("%s%s:%s:%.4f", _pcode, pcode2Category, recomPcodeList.get(i).getOccurenceValue(), recomPcodeList.get(i).getCosine());
                                }
                            }
                        }
                    }
                    recommendProductListToFile = strSame + "_" + strOther;
                }
            }

            int recomSameCount = 0;
            int recomOtherCount = 0;
            int recomCount = 0;

            if(StringUtils.isNotEmpty(recommendProductList)){
                String recomData[] = recommendProductList.split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST);
                if(recomData[0].length() > 0) {
                    String samePcodes[] = recomData[0].split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE);
                    recomCount = samePcodes.length;
                    insertRecomCountMap(recomCountmap,recomCount, ConstantsIBCF.IBCF_SAME_CATEGORY);
                    recomSameCount++;
                }
                if(recomData.length > 1) {
                    String OtherPcodes[] = recomData[1].split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE);
                    recomCount = OtherPcodes.length;
                    insertRecomCountMap(recomCountmap,recomCount, ConstantsIBCF.IBCF_OTHER_CATEGORY);
                    recomOtherCount++;
                }
            }
            if(recomSameCount > 0){
                advIdCrscCount += 1;
            }
            if(recomOtherCount > 0){
                advIdCrocCount += 1;
            }
            String pcode1Category = productMap.get(pcode)!=null?"["+productMap.get(pcode).getCategory()+"]":"[]";
            recomTotalCount += recomSameCount + recomOtherCount;  //전체 생성된 추천 개수(같은 카테고리 + 다른 카테고리)
            curOcuurencePcodeCount++;

            //운영에서는 쓰고있지 않음.
            if(config.isSaveFile()) {
                //saveFile( jobInfo.getAdvId(), pcode+pcode1Category, recommendProductListToFile, isPc, today, false);
                //String line = String.format("%s_%s_%s", adverId, pcode+pcode1Category, "");
                //saveFileOut.info(line);
            }
            if(config.isSaveRedis()) {
                String redisKey;
                if(batchConfig.getMode().equals("ibcf_click_ai_category")) { // AI 카테고리 모드일 경우
                    redisKey = isPc ? ConstantsIBCF.REDIS_KEY_AI_WEB_SUFFIX : ConstantsIBCF.REDIS_KEY_AI_MOB_SUFFIX;
                } else {
                    redisKey = isPc ? ConstantsIBCF.REDIS_KEY_WEB_SUFFIX : ConstantsIBCF.REDIS_KEY_MOB_SUFFIX;
                }
                String key = redisCluster.getRedisKey(redisKey, adverId, pcode);
                redisCluster.saveRedis( key, recommendProductList,config.getRedisExpireTime());
            }
        }
        jobInfo.setRecomeType(ConstantsIBCF.IBCF_TYPE_VER1);
        jobInfo.setAdvIdCrscCount(advIdCrscCount);
        jobInfo.setAdvIdCrocCount(advIdCrocCount);
        jobInfo.setRecomTotalCount(recomTotalCount);
        jobInfo.setRecomCountmap(recomCountmap);

        return curOcuurencePcodeCount;
    }

    /** +
     *
     * @param recomCountmap
     * @param pcodes
     * @param types
     */
    private void insertRecomCountMap(Map<String ,Integer> recomCountmap, int pcodes, String types){
        switch(pcodes) {
            case 1:
                getCount(recomCountmap,pcodes,types);
                break;
            case 2:
                getCount(recomCountmap,pcodes,types);
                break;
            case 3:
                getCount(recomCountmap,pcodes,types);
                break;
            case 4:
                getCount(recomCountmap,pcodes,types);
                break;
            case 5:
                getCount(recomCountmap,pcodes,types);
                break;
            case 6:
                getCount(recomCountmap,pcodes,types);
                break;
            case 7:
                getCount(recomCountmap,pcodes,types);
                break;
            case 8:
                getCount(recomCountmap,pcodes,types);
                break;
            case 9:
                getCount(recomCountmap,pcodes,types);
                break;
            case 10:
                getCount(recomCountmap,pcodes,types);
                break;
            case 11:
                getCount(recomCountmap,pcodes,types);
                break;
            case 12:
                getCount(recomCountmap,pcodes,types);
                break;
            case 13:
                getCount(recomCountmap,pcodes,types);
                break;
            case 14:
                getCount(recomCountmap,pcodes,types);
                break;
            case 15:
                getCount(recomCountmap,pcodes,types);
                break;
            case 16:
                getCount(recomCountmap,pcodes,types);
                break;
            case 17:
                getCount(recomCountmap,pcodes,types);
                break;
            case 18:
                getCount(recomCountmap,pcodes,types);
                break;

            default:
        }
    }

    /** +
     *
     * @param recomCountmap
     * @param pcodes
     * @param type
     */
    private void getCount(Map<String ,Integer> recomCountmap, int pcodes, String type){
        if(recomCountmap.containsKey(pcodes+"_"+type)){
            recomCountmap.put(pcodes+"_"+type,recomCountmap.get(pcodes+"_"+type)+1);
        }else{
            recomCountmap.put(pcodes+"_"+type,1);
        }
    }

    /** +
     *
     * @param isPc
     * @param pcode
     * @param sameCategoryBuffer
     * @param otherCategoryBuffer
     * @param recommendProductList
     * @param COdata
     * @return
     */
    private String getMergeCOdatas(boolean isPc, String pcode, StringBuilder sameCategoryBuffer,
                                   StringBuilder otherCategoryBuffer, String recommendProductList, String COdata) {
        if(StringUtils.isBlank(COdata)) {
            return recommendProductList;
        }
        String[] COdatas  = COdata.split(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST);
        if(StringUtils.isNotBlank(COdatas[0])) {
            List<String> sameCOList  = new ArrayList<String>(Arrays.asList(StringUtils.split(COdatas[0], ConstantsIBCF.SPLIT_CODE_IBCF_PCODE)));
            List<String> sameSRList  = new ArrayList<String>(Arrays.asList(StringUtils.split(sameCategoryBuffer.toString(), ConstantsIBCF.SPLIT_CODE_IBCF_PCODE)));
            sameCOList = mergeListAfterRemovePcode(sameCOList, sameSRList);
            recommendProductList = sameCOList.stream()
                    .map(n -> String.valueOf(n))
                    .collect(Collectors.joining(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE));
        }else if(sameCategoryBuffer.length() != 0) {
            recommendProductList = sameCategoryBuffer.toString();
        }
        if(COdatas.length > 1) {
            List<String> otherSRList  = new ArrayList<String>(Arrays.asList(StringUtils.split(otherCategoryBuffer.toString(), ConstantsIBCF.SPLIT_CODE_IBCF_PCODE)));
            List<String> otherCOList  = new ArrayList<String>(Arrays.asList(StringUtils.split(COdatas[1], ConstantsIBCF.SPLIT_CODE_IBCF_PCODE)));
            otherCOList = mergeListAfterRemovePcode(otherCOList, otherSRList);
            recommendProductList += ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST + otherCOList.stream()
                    .map(n -> String.valueOf(n))
                    .collect(Collectors.joining(ConstantsIBCF.SPLIT_CODE_IBCF_PCODE));
        }else if(otherCategoryBuffer.length() != 0) {
            recommendProductList += ConstantsIBCF.SPLIT_CODE_IBCF_PCODE_LIST + otherCategoryBuffer.toString();
        }
        return recommendProductList;
    }

    /** +
     *
     * @param COList
     * @param SRList
     * @return
     */
    private List<String> mergeListAfterRemovePcode(List<String> COList, List<String> SRList) {
        if(COList.size() > 0) {
            for(String sameCOpcode : COList) {
                SRList.remove(sameCOpcode);
            }
            COList.addAll(SRList);
            if(COList.size() > config.getMaxRecomPocde() ) {
                for(int i=config.getMaxRecomPocde()-1; i<COList.size(); i++) {
                    COList.remove(i);
                }
            }
        }
        return COList;
    }
}
