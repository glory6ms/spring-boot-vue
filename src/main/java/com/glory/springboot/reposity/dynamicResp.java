package com.glory.springboot.reposity;

import com.glory.springboot.entities.DynamicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;

//mysql-jpa数据操作的repository
public interface dynamicResp extends JpaRepository<DynamicEntity,Integer>, JpaSpecificationExecutor<DynamicEntity> {
    @Query("select d from DynamicEntity d where d.time between ?1 and ?2")
    Collection<DynamicEntity> findBetweenTime(String timein, String timeout);
}
