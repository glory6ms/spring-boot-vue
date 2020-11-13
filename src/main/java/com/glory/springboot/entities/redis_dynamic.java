package com.glory.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class redis_dynamic {
    public int mmsi;
    public ArrayList<es_dynamic> trajectory;
}
