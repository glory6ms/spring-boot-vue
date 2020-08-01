package com.glory.springboot.reposity;

import com.glory.springboot.entities.DynamicEntity;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class dynamicRespTest {

    @Autowired
    dynamicResp dynamicResp;
    @Autowired
    private ElasticsearchOperations operations;
    @Test
    void test1() {
        List<DynamicEntity> all = dynamicResp.findAll();
        IndexCoordinates index = IndexCoordinates.of("dongtai");
        List<IndexQuery> queries = new ArrayList<>();
        Gson gson = new Gson();
        int count=0;
        for (DynamicEntity d:all){
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(String.valueOf(d.getDid()));
            indexQuery.setSource(gson.toJson(d));
            queries.add(indexQuery);
            count++;
            if (count==5000){
                count=0;
                System.out.println("inserting..");
                operations.bulkIndex(queries, index);
                queries.clear();
            }
        }
        System.out.println(count);
        if (count>1){
            operations.bulkIndex(queries, index);
            queries.clear();
        }

    }
}