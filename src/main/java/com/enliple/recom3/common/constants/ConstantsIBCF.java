package com.enliple.recom3.common.constants;

public class ConstantsIBCF {
    /** +
     *  IBCF
     */
    //LOG DATA SUFFIX
    public final static String MOB_SUFFIX = "impp";
    public final static String WEB_SUFFIX = "impm";

    public final static String IBCF_TYPE_VER1 = "IBCF_V1"; //코사인 유사도 계산방식
    public final static String IBCF_TYPE_VER2 = "IBCF_V2"; //통계형(상품 쌍 정렬)
    public final static String IBCF_TYPE_AI_CATE = "IBCF_AI"; //기존 코사인 유사도 + AI 카테고리 비교방식

    //SPLIT_CODE_IBCF
    public final static String SPLIT_CODE_IBCF_CHAR = "~~";
    public final static String APPEND_CODE = "|";
    public final static String SPLIT_CODE_IBCF_PCODE = "\\|";
    public final static String SPLIT_CODE = "_";
    public final static String SPLIT_CODE_IBCF_PCODE_LIST = "_";

    //IBCF_CATEGORY_CODE
    public final static String IBCF_SAME_CATEGORY = "same";
    public final static String IBCF_OTHER_CATEGORY = "other";

    //ETC IBCF
    public final static int MAX_READ_COUNT_IBCF = 4000000;
    //가중치(wt) = pCodeCount(제품수) / 200000.
    //1보다 크면 계산값 반올림 하여 사용 (1보다 작거나 같으면 1로 설정)
    public final static int WEIGHT_CALCULATION_VALUE = 200000;
    public final static int IBCF_PERIOD_V2 = 30; //통계형 원천데이터 사용 일수 웹 & 모바일 고정 30일

    //REDIS_KEY_SUFFIX
    public final static String REDIS_KEY_WEB_SUFFIX = "COCW1";
    public final static String REDIS_KEY_MOB_SUFFIX = "COCW2";
    public final static String REDIS_KEY_AI_WEB_SUFFIX = "AI_COCW1";
    public final static String REDIS_KEY_AI_MOB_SUFFIX = "AI_COCW2";
    public final static String REDIS_KEY_V2_WEB_SUFFIX = "V2_COCW1"; //통계형 웹
    public final static String REDIS_KEY_V2_MOB_SUFFIX = "V2_COCW2"; //통계형 모바일

}
