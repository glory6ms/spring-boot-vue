package com.glory.springboot.reposity;

import com.glory.springboot.entities.StaticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface staticResp extends JpaRepository<StaticEntity,Integer>, JpaSpecificationExecutor<StaticEntity> {

    @Query(value = "select dynamic.lng,dynamic.lat,dynamic.land_speed,dynamic.land_course" +
            ",static.mmsi,static.ship_name,static.ship_length,static.ship_width,static.water_depth,static.destination,static.ship_type" +
            " from dynamic,static where dynamic.time> ?1 and dynamic.time< ?2 and dynamic.mmsi=static.mmsi group by static.mmsi",nativeQuery = true)
     List<Object[]> findListBytime(String Timein, String Timeout);

    List<StaticEntity> findByMmsiIn(List<Integer> MMSI);

}
