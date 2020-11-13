package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@Document(indexName = "dongtaii")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class es_dynamic implements Serializable {
    public static final long serialVersionUID = 1L;
//    public Long did;
//    public int mmsi;

    public Double[] location = new Double[2];
//    public List<BigDecimal> location = new ArrayList<>();

    public float landSpeed;
    public float landCourse;

    public short shipCourse;
    public String shipState;

    public String precion;

    public String aisType;
    public String time;

    @Override
    public String toString() {
        return "es_dynamic{" +
//                "did=" + did +
//                ", mmsi=" + mmsi +
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
