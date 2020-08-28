package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

//@Document(indexName = "dongtaii")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class es_dynamic implements Serializable {
    public static final long serialVersionUID = 1L;
    @Id
    public int did;
    public int mmsi;

    public BigDecimal[] location = new BigDecimal[2];

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
                ", location=" + Arrays.toString(location) +
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
