package com.glory.springboot.reposity;

import com.glory.springboot.entities.es_dynamic;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.math.BigDecimal;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class esRespTest {

    @Autowired
    esResp esResp;
    @Autowired
    ElasticsearchOperations elasticsearchOperations;
    @Test
    void test01(){
        es_dynamic es_dynamic=new es_dynamic();
        es_dynamic.aisType="class A";
        es_dynamic.did=1;
        es_dynamic.landCourse=new BigDecimal(1.0);
        es_dynamic.landSpeed=new BigDecimal(1.0);

        es_dynamic.location[0]=new BigDecimal(113.0);
        es_dynamic.location[1]=new BigDecimal(30.0);
        esResp.save(es_dynamic);
    }
    @Test
    void test02(){
        String[] include = {"mmsi","location"};
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        PageRequest pageRequest = PageRequest.of(0, 500);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(rangeQuery("time").gte("2017-02-05").lte("2017-02-08"));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest);
        IndexCoordinates index = IndexCoordinates.of("dongtaii");
        SearchHits<es_dynamic> search = elasticsearchOperations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
        for (int i=0;i<10;i++){
            System.out.println(search.getSearchHit(i));
        }
    }
}