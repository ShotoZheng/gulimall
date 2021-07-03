package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimallElasticSearchConfig {

    private static final String HOST_IP = "192.168.160.129";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    public static final RequestOptions COMMON_OPTIONS;

    /**
     * 请求统一定制
     */
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // 这里定制请求...
        COMMON_OPTIONS = builder.build();
    }

    /**
     *  创建 RestHighLevelClient 实例
     * @return
     */
    @Bean
    RestHighLevelClient client() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(HOST_IP, PORT, SCHEME));
        return new RestHighLevelClient(builder);
    }
}
