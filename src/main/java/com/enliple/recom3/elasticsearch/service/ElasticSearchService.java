package com.enliple.recom3.elasticsearch.service;


import com.enliple.recom3.common.constants.ConstantsCommon;
import com.enliple.recom3.worker.dto.ProductDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
@Slf4j
@PropertySource("classpath:/properties/elastic.properties")
public class ElasticSearchService {
    @Value("${enliple.clusterName}") private String clusterName;
    @Value("${enliple.indexNameForWeb}") private String indexNameForWeb;
    @Value("${enliple.indexNameForMobile}") private String indexNameForMobile;
    @Value("${enliple.transportIP}") private String transportIpString;
    @Value("${enliple.restIP}") private String restIpString;

    protected TransportClient client;
    @Getter
    private RestClient restClient;

    private static final int HTTP_REST_PORT = 9200;

    @PostConstruct
    private void init() throws NumberFormatException, UnknownHostException {
        //initClient();
        initRestClient();
    }

    /**
     * init tcp client
     */
    private void initClient() throws NumberFormatException, UnknownHostException {

        if(client != null) {
            log.warn("client is not null, init skip");
            return;
        }

        long _time = System.currentTimeMillis();

        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", true)
                .build();

        String[] transportIps = transportIpString.split(ConstantsCommon.PIPE_REGEX);
        client = new PreBuiltTransportClient(settings);

        for(String transportIp : transportIps) {
            String[] transportInfo = transportIp.split(ConstantsCommon.COLON_REGEX);
            client.addTransportAddress(new TransportAddress(
                    InetAddress.getByName(transportInfo[0]), Integer.parseInt(transportInfo[1])));
        }

        log.info("initClient done, ms: {}", (System.currentTimeMillis() - _time));
    }

    /**
     * init rest client
     */
    private void initRestClient() {

        if(restClient != null) {
            log.warn("restClient is not null, init skip");
            return;
        }

        long _time = System.currentTimeMillis();

        List<HttpHost> httpHostList = new ArrayList<>();
        String[] restIPs = restIpString.split(ConstantsCommon.PIPE_REGEX);
        for(String restIP : restIPs) {
            String[] tokens = restIP.split(ConstantsCommon.COLON_REGEX);
            if(tokens.length != 2) {
                log.warn("invalid restIP: {}, restIpString: {}", restIP, restIpString);
                continue;
            }
            String host = tokens[0];
            String port = tokens[1];
            httpHostList.add(new HttpHost(host, Integer.parseInt(port), "http"));
        }

        HttpHost[] hosts = httpHostList.toArray(new HttpHost[httpHostList.size()]);
        RestClientBuilder builder = RestClient.builder(hosts);
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnPerRoute(10); // RestClientBuilder.DEFAULT_MAX_CONN_PER_ROUTE; // 10
                httpClientBuilder.setMaxConnTotal(30); // RestClientBuilder.DEFAULT_MAX_CONN_TOTAL; // 30
                return httpClientBuilder;
            }
        });

        restClient = builder.build();

        log.info("initRestClient done, ms: {}", (System.currentTimeMillis() - _time));
    }

    @PreDestroy
    private void destroy() {
        if (client != null) {
            client.close();
            log.info("client close");
        }
        if(restClient != null) {
            try {
                restClient.close();
                log.info("restClient");
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    /**
     * restClient-scroll 로 엘라스틱 조회
     * @param advId
     * @param pc
     * @return
     */
    public HashMap<String, ProductDto> getAllProductCategoryV2(String advId, boolean pc) {
        String scrollId = "";
        try {
            HashMap<String, ProductDto> productDtoMap = new HashMap<String, ProductDto>();
            int is_Pc = pc ? 1 : 2;
            String indexName = "";
            if (pc) {
                indexName = indexNameForWeb + "/_search";
            } else {
                indexName = indexNameForMobile + "/_search";
            }
            log.info(indexName);

            String query = "userid:"+advId;
            int totalsize = 0;

            while (true){
                Request request = new Request("GET", indexName);
                int maxsize = 5000;
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("scroll", "1m");
                if(StringUtils.isNotEmpty(scrollId)){
                    indexName = "/_search/scroll";
                    paramMap = openScroll(paramMap,scrollId);
                    request = new Request("GET", indexName);
                    request.addParameter("scroll", "1m");
                    request.addParameter("scroll_id", scrollId);
                }else{
                    paramMap.put("q", query);
                    paramMap.put("size", String.valueOf(maxsize));
                    paramMap.put("sort","lastUpdate:desc");
                    request.addParameter("scroll", "1m");
                    request.addParameter("q", query);
                    request.addParameter("size", String.valueOf(maxsize));
                    request.addParameter("sort","lastUpdate:desc");
                }

                Response response = restClient.performRequest(request);
                String data = EntityUtils.toString(response.getEntity());

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
                scrollId = jsonObject.get("_scroll_id").toString();
                JSONObject hitsObject = (JSONObject) jsonObject.get("hits");
                JSONArray hitsArray = (JSONArray) hitsObject.get("hits");

                //log.debug(String.valueOf(hitsArray.size())); //조회된 카운트
                log.debug(scrollId);
                log.debug(String.valueOf(totalsize));

                if (hitsArray.size() > 0) {
                    totalsize += hitsArray.size();
                    productDtoMap = getProductDtoMapV2(productDtoMap,hitsArray, is_Pc);
                }else{
                    return productDtoMap;
                }
            }
        } catch (Exception e) {
            log.info(e.toString());
        }
        finally {
            try{
                closeScroll(scrollId);
            }catch (Exception e){

            }
        }
        return new HashMap<String, ProductDto>();
    }

    private HashMap<String, ProductDto> getProductDtoMapV2(HashMap<String, ProductDto> productDtoMap,JSONArray hitsArray, int is_Pc) {
        try{
            for(int i =0 ; i < hitsArray.size(); i++) {
                //TODO 파싱 개발.
                JSONObject hits = (JSONObject) hitsArray.get(i);
                JSONObject sourceObject = (JSONObject) hits.get("_source");
                String pcode = sourceObject.get("pcode").toString();
                String category = sourceObject.get("cate4").toString();
                String CATE1 = sourceObject.get("cate1").toString();
                String CATE2 = sourceObject.get("cate2").toString();
                String CATE3 = sourceObject.get("cate3").toString();

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
            }
        }catch (Exception e){
            log.info("parsing error {} ",e.toString());
        }finally {

        }
        return productDtoMap;
    }

    private Map<String, String> openScroll(Map<String, String> paramMap, String scrollId){
        paramMap.put("scroll_id", scrollId);
        return paramMap;
    }

    private void closeScroll(String scrollId){
        try{
            Map<String, String> paramMap = new HashMap<String, String>();
            //TODO scroll 닫아야 부하가 없음.
            String indexName = "/_search/scroll";
            paramMap.put("scroll_id", scrollId);
            Request request = new Request("DELETE", indexName);
            request.addParameter("scroll_id", scrollId);
            Response response = restClient.performRequest(request);
            String data = EntityUtils.toString(response.getEntity());
            log.info("closeScroll id is {}",scrollId);
        }catch (Exception e){
            log.info("closeScroll exception {}",e.toString());
        }
    }


    /**
     * TransportClient-searchAfter 로 엘라스틱서치 조회
     * @param advId
     * @param pc
     * @param pcodeAuidsMap
     * @return
     */
    public HashMap<String, ProductDto> getAllProductCategory(String advId, boolean pc, HashMap<String, Set<Integer>> pcodeAuidsMap) {
        try {
            HashMap<String, ProductDto> productDtoMap = new HashMap<String, ProductDto>();
            int is_Pc = pc ? 1 : 2;
            String indexName = "";
            Object[] searchAfter = null;

            if (pc) {
                indexName = indexNameForWeb;
            } else {
                indexName = indexNameForMobile;
            }
            int elasticSize = 0;

            while (true) {
                QueryBuilder qb = QueryBuilders.boolQuery()
                        //어차피 만개씩밖에 찌르지 못해서 pcodeSet이 의미가없어서 주석처리.
                        //.must(QueryBuilders.termsQuery("pcode",pcodeSet))
                        .must(QueryBuilders.termQuery("userid", advId));

                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName)
                        .setSize(5000)
                        .setSearchType(SearchType.QUERY_THEN_FETCH)
                        .setQuery(qb)
                        .addSort("lastUpdate", SortOrder.DESC);

                if (searchAfter != null) {
                    // 바로 직전 쿼리의 마지막 Docuemnt의 sort_value를 셋팅한다.
                    // 동일한 쿼리에 'sort_value' 의 조건식이 더 추가된다.
                    searchRequestBuilder.searchAfter(searchAfter);
                }

                SearchResponse searchResponse = searchRequestBuilder.get();
                SearchHit[] SearchHitList = searchResponse.getHits().getHits();

                if (SearchHitList.length > 0) {
                    productDtoMap = getProductDtoMap(productDtoMap,SearchHitList, is_Pc);
                    elasticSize = productDtoMap.size();
                    log.info("elasticSize-{} =" + elasticSize,advId);
                    // foreach 각 문서 처리
                    SearchHit lastHitDocument = SearchHitList[SearchHitList.length - 1];
                    searchAfter = lastHitDocument.getSortValues();
                } else {
                    return productDtoMap;
                }
            }
        } catch (Exception e) {

        }
        return new HashMap<String, ProductDto>();
    }

    private HashMap<String, ProductDto> getProductDtoMap(HashMap<String, ProductDto> productDtoMap, SearchHit[] hits, int is_Pc) {
        for(SearchHit searchHit : hits) {
            Map<String, Object> searchHitSourceMap = searchHit.getSourceAsMap();
            String pcode = searchHitSourceMap.get("pcode").toString();
            String category = searchHitSourceMap.get("cate4").toString();
            String CATE1 = searchHitSourceMap.get("cate1").toString();
            String CATE2 = searchHitSourceMap.get("cate2").toString();
            String CATE3 = searchHitSourceMap.get("cate3").toString();

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
        }

        return productDtoMap;
    }
}
