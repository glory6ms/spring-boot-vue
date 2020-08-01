package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.math.BigDecimal;

@Document(indexName = "dongtai")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class es_dynamic implements Serializable {
    public static final long serialVersionUID = 1L;
    @Id
    public int did;
    public int mmsi;

    public BigDecimal lng;
    public BigDecimal lat;

    public BigDecimal landSpeed;
    public BigDecimal landCourse;

    public short shipCourse;
    public String shipState;

    public String precion;

    public String aisType;
    public String time;

    @Override
    public String toString() {
        return "es_dynamic{" +
                "did=" + did +
                ", mmsi=" + mmsi +
                ", lng=" + lng +
                ", lat=" + lat +
                ", landSpeed=" + landSpeed +
                ", landCourse=" + landCourse +
                ", shipCourse=" + shipCourse +
                ", shipState='" + shipState + '\'' +
                ", precion='" + precion + '\'' +
                ", aisType='" + aisType + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
