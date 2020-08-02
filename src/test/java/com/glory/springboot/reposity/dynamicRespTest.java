package com.glory.springboot.reposity;

import com.glory.springboot.entities.DynamicEntity;
import com.glory.springboot.entities.es_dynamic;
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
        IndexCoordinates index = IndexCoordinates.of("dongtaii");
        List<IndexQuery> queries = new ArrayList<>();
        Gson gson = new Gson();
        int count=0;
        for (DynamicEntity d:all){
            es_dynamic es=new es_dynamic();
            es.location[0] = d.getLng();
            es.location[1] = d.getLat();
            es.landSpeed = d.getLandSpeed();
            es.landCourse = d.getLandCourse();
            es.did = d.getDid();
            es.aisType = d.getAistype();
            es.mmsi = d.getMmsi();
            es.precion = d.getPrecion();
            es.shipCourse = d.getShipCourse();
            es.shipState = d.getShipState();
            es.time =d.getTime();
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(String.valueOf(d.getDid()));
            indexQuery.setSource(gson.toJson(es));
            queries.add(indexQuery);
            count++;
            if (count==500){
                count=0;
                System.out.println("inserting..");
                operations.bulkIndex(queries, index);
                queries.clear();
            }
        }
        System.out.println(count);
//        if (count>1){
//            operations.bulkIndex(queries, index);
//            queries.clear();
//        }

    }
}