package com.enliple.recom3.jpa.db2.repository;

import com.enliple.recom3.jpa.db2.domain.AdverPrdtCateInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AdverPrdtCateInfoRepository extends Repository<AdverPrdtCateInfo, Long> {

	@Query(nativeQuery = true, value = "SELECT \n" +
			"M.NO\n" +
			", M.ADVER_ID\n" +
			", M.PRODUCT_CODE\n" +
			", M.ADVER_CATE_NO\n" +
			", S.FIRST_CATE\n" +
			", S.SECOND_CATE\n" +
			", S.THIRD_CATE\n" +
			", S.FOURTH_CATE\n" +
			"FROM \n" +
			"(SELECT\n" +
			" NO,\n" +
			" ADVER_ID,\n" +
			" PRODUCT_CODE,\n" +
			" ADVER_CATE_NO\n" +
			"FROM ADVER_PRDT_CATE_INFO\n" +
			"GROUP BY ADVER_ID, PRODUCT_CODE) M,\n" +
			"ADVER_PRDT_STANDARD_CATE S\n" +
			"WHERE M.ADVER_CATE_NO = S.NO\n" +
			"AND M.ADVER_ID=:advId")
	List<AdverPrdtCateInfo> findAdverPrdtCateInfoList(String advId);

}
