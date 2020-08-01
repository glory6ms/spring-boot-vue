package com.glory.springboot.reposity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

//静态表数据库操作测试类
@SpringBootTest
class staticRespTest {

    //自动注入
    @Autowired
    staticResp staticResp;
    //findAll
    @Test
    void table(){
        System.out.println(staticResp.findAll());
    }
}