package com.enliple.recom3.common.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchUtils {
    private static long START_TIME = 0;
    private static long END_TIME = 0;

    public static void getExecutionTime(long time ,String methodName){
        if(time >0 && END_TIME == 0){
            START_TIME = time;
            //log.info("methodName:{}, start time : {} second", methodName, START_TIME);
            log.info("methodName : {}, start",methodName);
            END_TIME = time;
        }

        if(time > 0 && START_TIME != time){
            log.info( "methodName : {}, end time : {} second" ,methodName, (( time - START_TIME )/1000.0));
            END_TIME = 0;
        }
    }
}
