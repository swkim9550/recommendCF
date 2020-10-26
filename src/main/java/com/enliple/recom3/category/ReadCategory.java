package com.enliple.recom3.category;

import com.enliple.recom3.common.config.Config;
import com.enliple.recom3.worker.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;


@Service
@Slf4j
public class ReadCategory {
    @Autowired
    private Config config;
    public HashMap<String, ProductDto> getAllProductCategory(String advId) {
        HashMap<String, ProductDto> productDtoMap = new HashMap<String, ProductDto>();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String year = String.format("%02d", calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
        String day = String.format("%02d", calendar.get(Calendar.DATE));

        BufferedReader br = null;
        FileReader fr = null;
        boolean fileRead = true;
        int fileCount = 1;
        try{
           while(fileRead){
               String filePath = String.format("/%s/%s/%s", config.getRecomCateFilePath(), year+month+day , advId+"_"+fileCount+".web");
               File file = new File(filePath);
               if(file.exists()){
                   log.info("read file catemap {}",filePath);
                   fr = new FileReader(filePath);
                   br = new BufferedReader(fr);

                   String line;
                   line = br.readLine();

                   for (int i = 0; line != null; line = br.readLine()) {
                       productDtoMap = getProductDtoMap(productDtoMap, line, 0);
                   }
                   fileCount++;
               }else{
                   fileRead = false;
               }
            }
        }catch (Exception e){

        }finally {
            try{
                if (br != null) br.close();
                if (fr != null) fr.close();
            }catch (IOException e){
                log.error("getAllProductCategory is error {}", e);
            }
        }
        return productDtoMap;
    }

    private HashMap<String, ProductDto> getProductDtoMap(HashMap<String, ProductDto> productDtoMap, String line, int is_Pc) {
        String[] array = line.split(",");
        String pcode = "";
        String CATE1 = "";
        String CATE2 = "";
        String CATE3 = "";
        String category = "";

        if(array.length == 2) {
            pcode = array[0] == null ? "":array[0];
            CATE1 = array[1] == null ? "":array[1];

        }else if(array.length == 3){
            pcode = array[0] == null ? "":array[0];
            CATE1 = array[1] == null ? "":array[1];
            CATE2 = array[2] == null ? "":array[2];

        }else if(array.length == 4){
            pcode = array[0] == null ? "":array[0];
            CATE1 = array[1] == null ? "":array[1];
            CATE2 = array[2] == null ? "":array[2];
            CATE3 = array[3] == null ? "":array[3];
        }else if(array.length == 5){
            pcode = array[0] == null ? "":array[0];
            category = array[4] == null ? "":array[4];
            CATE1 = array[1] == null ? "":array[1];
            CATE2 = array[2] == null ? "":array[2];
            CATE3 = array[3] == null ? "":array[3];
        }

        if("".equals(category)) {
            category = CATE3;
        }
        if("".equals(category)) {
            category = CATE2;
        }
        if("".equals(category)) {
            category = CATE1;
        }

        ProductDto productDto = new ProductDto(category, is_Pc);
        productDtoMap.put(pcode, productDto);

        return productDtoMap;
    }
}
