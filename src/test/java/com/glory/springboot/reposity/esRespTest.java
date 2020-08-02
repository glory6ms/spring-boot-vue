package com.glory.springboot.reposity;

import com.glory.springboot.entities.es_dynamic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class esRespTest {

    @Autowired
    esResp esResp;
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
}