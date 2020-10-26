package com.enliple.recom3.common.config;

import com.enliple.recom3.jpa.db1.domain.CfengineConfig;
import com.enliple.recom3.jpa.db1.domain.CfengineConfigKey;
import com.enliple.recom3.jpa.db1.service.CfengineConfigServiceimpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EngineConfig {
    @Autowired
    private CfengineConfigServiceimpl cfengineConfigServiceimpl;

    public Map<String,String> loadbatchProperties() {
        Map<String,String> batchPropertiesMap = new HashMap<>();
        List<CfengineConfig> batchPropertiesList = cfengineConfigServiceimpl.getAllCfengineConfig();

        for(int i =0 ; i< batchPropertiesList.size(); i ++){
            CfengineConfigKey cfengineConfigKey = batchPropertiesList.get(i).getKey();
            batchPropertiesMap.put(cfengineConfigKey.getPropertiesName(),cfengineConfigKey.getPropertiesValue());
        }
        return batchPropertiesMap;
    }
}
