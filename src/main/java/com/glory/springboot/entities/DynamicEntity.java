package com.glory.springboot.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "dynamic", schema = "ais")
public class DynamicEntity {
    private int did;
    private String aistype;
    private BigDecimal landCourse;
    private BigDecimal landSpeed;
    private BigDecimal lat;
    private BigDecimal lng;
    private int mmsi;
    private String precion;
    private short shipCourse;
    private String shipState;
    private String time;

    @Id
    @Column(name = "did", nullable = false)
    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    @Basic
    @Column(name = "aistype", nullable = false, length = 10)
    public String getAistype() {
        return aistype;
    }

    public void setAistype(String aistype) {
        this.aistype = aistype;
    }

    @Basic
    @Column(name = "land_course", nullable = false, precision = 1)
    public BigDecimal getLandCourse() {
        return landCourse;
    }

    public void setLandCourse(BigDecimal landCourse) {
        this.landCourse = landCourse;
    }

    @Basic
    @Column(name = "land_speed", nullable = false, precision = 1)
    public BigDecimal getLandSpeed() {
        return landSpeed;
    }

    public void setLandSpeed(BigDecimal landSpeed) {
        this.landSpeed = landSpeed;
    }

    @Basic
    @Column(name = "lat", nullable = false, precision = 13)
    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    @Basic
    @Column(name = "lng", nullable = false, precision = 13)
    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    @Basic
    @Column(name = "mmsi", nullable = false)
    public int getMmsi() {
        return mmsi;
    }

    public void setMmsi(int mmsi) {
        this.mmsi = mmsi;
    }

    @Basic
    @Column(name = "precion", nullable = false, length = 10)
    public String getPrecion() {
        return precion;
    }

    public void setPrecion(String precion) {
        this.precion = precion;
    }

    @Basic
    @Column(name = "ship_course", nullable = false)
    public short getShipCourse() {
        return shipCourse;
    }

    public void setShipCourse(short shipCourse) {
        this.shipCourse = shipCourse;
    }



    @Basic
    @Column(name = "ship_state", nullable = false, length = 40)
    public String getShipState() {
        return shipState;
    }

    public void setShipState(String shipState) {
        this.shipState = shipState;
    }



    @Basic
    @Column(name = "time", nullable = false)
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public DynamicEntity() {
    }
    public DynamicEntity(BigDecimal lng,BigDecimal lat) {
         this.lng = lng;
         this.lat = lat;
    }

    @Override
    public String toString() {
        return "DynamicEntity{" +
                "aistype='" + aistype + '\'' +
                ", landCourse=" + landCourse +
                ", landSpeed=" + landSpeed +
                ", lat=" + lat +
                ", lng=" + lng +
                ", mmsi=" + mmsi +
                ", precion='" + precion + '\'' +
                ", shipCourse=" + shipCourse +
                ", shipState='" + shipState + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
