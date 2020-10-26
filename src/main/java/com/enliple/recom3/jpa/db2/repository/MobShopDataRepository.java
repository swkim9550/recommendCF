package com.enliple.recom3.jpa.db2.repository;

import java.util.List;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.enliple.recom3.jpa.db2.domain.ComShopData;
import com.enliple.recom3.jpa.db2.domain.MobShopData;



public interface MobShopDataRepository extends CrudRepository<MobShopData, Long>{
	
	List<ComShopData> findTop1ByUserIdAndPcode(String userId,String pcode);
	List<ComShopData> findByUserId(String userId);

	@Query(nativeQuery=true, value = "SELECT\n" +
			"      NO as no\n" +
			"      ,USERID as userId\n" +
			"      ,PCODE as pcode\n" +
			"      ,CATE1 as cate1\n" +
			"      ,CATE2 as cate2\n" +
			"      ,CATE3 as cate3\n" +
			"      ,CATE4 as cate4\n" +
			"    FROM MOB_SHOP_DATA\n" +
			"WHERE USERID =:advId\n" +
			"AND pcode IN :serachPcodeString")
	List<MobShopData> findByList(String advId, List<String> serachPcodeString);
}
