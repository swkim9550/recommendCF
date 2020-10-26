package com.enliple.recom3.jpa.db1.repository;

import com.enliple.recom3.jpa.db1.domain.CfengineConfig;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Map;

public interface CfengineConfigRepositroy  extends CrudRepository<CfengineConfig, Long> {
    List<CfengineConfig> findAllBy();
}
