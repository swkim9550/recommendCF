package com.enliple.recom3.jpa.db1.service;

import com.enliple.recom3.jpa.db1.domain.CfengineConfig;
import com.enliple.recom3.jpa.db1.repository.CfengineConfigRepositroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("RECOM_CONFIG_JPA")
public class CfengineConfigServiceimpl implements CfengineConfigService {

    @Autowired
    private CfengineConfigRepositroy cfengineConfigRepositroy;

    @Override
    public List<CfengineConfig> getAllCfengineConfig() {
        List<CfengineConfig> cfengineConfig = cfengineConfigRepositroy.findAllBy();
        return cfengineConfig;
    }
}
