package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class es_test {
    @Id
    public String id;
    public int mmsi;
    public BigDecimal[] location = new BigDecimal[2];
    public BigDecimal landSpeed;
    public BigDecimal landCourse;
    public short shipCourse;
    public String shipState;
    public String precion;
    public String aisType;
    public String time;
}
