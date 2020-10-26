package com.enliple.recom3.jpa.db1.repository;


import com.enliple.recom3.jpa.db1.domain.AdverIdByType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface AdverIdTypeListRepository extends CrudRepository<AdverIdByType, Long> {

    @Query(nativeQuery=true, value = "SELECT\n" +
            "      * \n" +
            "    FROM RECOM_ADVER_ID_TYPE_LIST\n" +
            "WHERE ADVER_TYPE =:type\n")
    List<AdverIdByType> findByType(String type);
}
