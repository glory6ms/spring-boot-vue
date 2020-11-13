package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashMap;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class place {
    public String placeId;
    public String geometry;
    public String city;
    public String country;
    public LinkedHashMap lon1;
    public LinkedHashMap lat1;
    public LinkedHashMap lon2;
    public LinkedHashMap lat2;
    public LinkedHashMap lon3;
    public LinkedHashMap lat3;
    public String provience;
}
