package com.atguigu.gulimall.search;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Data
    @ToString
    class Product {
        private String spuName;
        private Long id;
    }

    @Data
    @ToString
    public static class Account {

        private int account_number;
        private String address;
        private int age;
        private int balance;
        private String city;
        private String email;
        private String employer;
        private String firstname;
        private String gender;
        private String lastname;
        private String state;
    }

    @Test
    public void termData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("age", 32);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        log.info("请求报文：{}", searchSourceBuilder.toString());
        log.info("响应报文：{}", searchResponse.toString());

        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsArray = hits.getHits();
        for (SearchHit hit : hitsArray) {
            String source = hit.getSourceAsString();
            Account account = JSON.parseObject(source, Account.class);
            log.info(account.toString());
        }

    }


    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("age_avg")
                .field("age").size(1000);
        aggregation.subAggregation(AggregationBuilders.avg("banlances_avg")
                .field("balance"));
        searchSourceBuilder.aggregation(aggregation);

        searchSourceBuilder.sort(new FieldSortBuilder("balance").order(SortOrder.DESC));
        searchSourceBuilder.size(5);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        log.info(searchSourceBuilder.toString());
        log.info(searchResponse.toString());

    }

    @Test
    public void searchAggData() throws IOException {
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.sort(new FieldSortBuilder("account_number").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("balance").order(SortOrder.ASC));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        log.info(searchResponse.toString());

    }

    /**
     * 索引数据
     * @throws IOException
     */
    @Test
    public void indexData() throws IOException {
        // 1. 发送实体数据
        Product product = new Product();
        product.setSpuName("华为");
        product.setId(10L);
        // 2. 转为 JSON 字符串
        String productJsonStr = JSON.toJSONString(product);
        // 3. 创建索引
        IndexRequest request = new IndexRequest("product");
        // 4. 文档标识
        request.id("1");
        // 5. 设置传送数据
        request.source(productJsonStr, XContentType.JSON);
        // 6. 索引操作请求，设置请求 RequestOptions COMMON_OPTIONS
        IndexResponse indexResponse = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 7. 打印响应信息
        log.info("响应信息：{}", indexResponse.toString());
    }

}
