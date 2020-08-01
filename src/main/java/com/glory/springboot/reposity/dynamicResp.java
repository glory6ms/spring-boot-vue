package com.glory.springboot.reposity;

import com.glory.springboot.entities.DynamicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//mysql-jpa数据操作的repository
public interface dynamicResp extends JpaRepository<DynamicEntity,Integer>, JpaSpecificationExecutor<DynamicEntity> {
}
