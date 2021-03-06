package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ??????????????????
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    private static final String SEP_FLAG = "_";

    /**
     * ?????? ES
     * @param param  ?????????????????????
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {
        //1?????????????????????????????????DSL??????
        SearchResult result = null;
        SearchRequest searchRequest = buildSearchRequrest(param);
        try {
            //2?????????????????????
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3???????????????????????????????????????????????????
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * ??????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @return ???????????????
     */
    private SearchRequest buildSearchRequrest(SearchParam param) {
        //??????DSL?????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * ???????????????????????????????????????????????????????????????????????????
         */
        //1?????????bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1???must-???????????????
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2???bool - filter - ??????????????????id??????
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2???bool - filter - ????????????id??????
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2???bool - filter - ???????????????????????????????????????
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attrStr : param.getAttrs()) {
                //attrs=1_5???:8???&attrs=2_16G:8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                //attr = 1_5???:8???
                String[] s = attrStr.split(SEP_FLAG);
                //???????????????id
                String attrId = s[0];
                //??????????????????????????????
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //?????????????????????????????????nested??????
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //1.2???bool - filter - ?????????????????????????????????
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //1.2???bool - filter - ??????????????????
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split(SEP_FLAG);
            if (s.length == 2) {
                //??????
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith(SEP_FLAG)) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith(SEP_FLAG)) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        sourceBuilder.query(boolQuery);

        /**
         * ???????????????????????????
         */
        //2.1?????????
        if (StringUtils.isNotEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split(SEP_FLAG);
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        //2.2?????????
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3?????????
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * ????????????
         */
        //1???????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        //????????????????????????
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        //2??????????????? catalog_agg
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        //3??????????????? attr_agg
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");

        //????????????????????????attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //?????????????????????attr_id???????????????
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //?????????????????????attr_id?????????????????????????????????attrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        String s = sourceBuilder.toString();
        log.info("?????????DSL:{}", s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * ??????????????????
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        //1????????????????????????????????????
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    if (skuTitle != null && skuTitle.getFragments() != null && skuTitle.getFragments().length > 0) {
                        String highString = skuTitle.getFragments()[0].string();
                        esModel.setSkuTitle(highString);
                    }
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2???????????????????????????????????????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            //1??????????????????id
            long attrId = bucket.getKeyAsNumber().longValue();
            //2????????????????????????
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();

            //3???????????????????????????
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3???????????????????????????????????????????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1??????????????????id
            long brandId = bucket.getKeyAsNumber().longValue();
            //2?????????????????????
            List<? extends Terms.Bucket> brandNameAgg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets();
            String brandName = "";
            if (!CollectionUtils.isEmpty(brandNameAgg)) {
                brandName = brandNameAgg.get(0).getKeyAsString();
            }

            //3????????????????????????
            List<? extends Terms.Bucket> brandImgAgg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets();
            String brandImg = "";
            if (!CollectionUtils.isEmpty(brandImgAgg)) {
                brandImg = brandImgAgg.get(0).getKeyAsString();
            }
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4???????????????????????????????????????????????????
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //????????????id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //???????????????
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            List<? extends Terms.Bucket> nameAggBuckets = catalogNameAgg.getBuckets();
            String catalogName = "";
            if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                catalogName = nameAggBuckets.get(0).getKeyAsString();
            }
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5???????????????-??????
        result.setPageNum(param.getPageNum());

        //5???????????????-????????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        //5???????????????-?????????-??????
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);
        return result;
    }


}
