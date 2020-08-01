package com.glory.springboot.reposity;

import com.glory.springboot.entities.es_dynamic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface esResp extends ElasticsearchRepository<es_dynamic,Integer> {


}
