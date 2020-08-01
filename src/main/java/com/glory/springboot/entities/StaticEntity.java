package com.glory.springboot.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "static", schema = "ais")
public class StaticEntity {
    private Integer sid;
    private Integer mmsi;
    private String shipName;
    private String calledName;
    private String shipType;
    private Date time;
    private short shipLength;
    private short shipWidth;
    private BigDecimal waterDepth;
    private String poType;
    private String eta;
    private String imo;
    private String destination;
    private String aisType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    @Basic
    @Column(name = "MMSI", nullable = false)
    public Integer getMmsi() {
        return mmsi;
    }

    public void setMmsi(Integer mmsi) {
        this.mmsi = mmsi;
    }

    @Basic
    @Column(name = "Ship_Name", nullable = true, length = 40)
    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    @Basic
    @Column(name = "Called_Name", nullable = true, length = 20)
    public String getCalledName() {
        return calledName;
    }

    public void setCalledName(String calledName) {
        this.calledName = calledName;
    }

    @Basic
    @Column(name = "Ship_Type", nullable = false, length = 20)
    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    @Basic
    @Column(name = "Time", nullable = false)
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Basic
    @Column(name = "Ship_Length", nullable = false)
    public short getShipLength() {
        return shipLength;
    }

    public void setShipLength(short shipLength) {
        this.shipLength = shipLength;
    }

    @Basic
    @Column(name = "Ship_Width", nullable = false)
    public short getShipWidth() {
        return shipWidth;
    }

    public void setShipWidth(short shipWidth) {
        this.shipWidth = shipWidth;
    }

    @Basic
    @Column(name = "Water_Depth", nullable = false,precision=4, scale=1)
    public BigDecimal getWaterDepth() {
        return waterDepth;
    }

    public void setWaterDepth(BigDecimal waterDepth) {
        this.waterDepth = waterDepth;
    }

    @Basic
    @Column(name = "Po_Type", nullable = false, length = 20)
    public String getPoType() {
        return poType;
    }

    public void setPoType(String poType) {
        this.poType = poType;
    }

    @Basic
    @Column(name = "ETA", nullable = false, length = 15)
    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    @Basic
    @Column(name = "IMO", nullable = false, length = 15)
    public String getImo() {
        return imo;
    }

    public void setImo(String imo) {
        this.imo = imo;
    }

    @Basic
    @Column(name = "Destination", nullable = false, length = 40)
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Basic
    @Column(name = "AISType", nullable = false, length = 10)
    public String getAisType() {
        return aisType;
    }

    public void setAisType(String aisType) {
        this.aisType = aisType;
    }



    @Override
    public int hashCode() {
        int result = mmsi;
        result = 31 * result + (shipName != null ? shipName.hashCode() : 0);
        result = 31 * result + (calledName != null ? calledName.hashCode() : 0);
        result = 31 * result + (shipType != null ? shipType.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (int) shipLength;
        result = 31 * result + (int) shipWidth;
        result = 31 * result + (waterDepth != null ? waterDepth.hashCode() : 0);
        result = 31 * result + (poType != null ? poType.hashCode() : 0);
        result = 31 * result + (eta != null ? eta.hashCode() : 0);
        result = 31 * result + (imo != null ? imo.hashCode() : 0);
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (aisType != null ? aisType.hashCode() : 0);
        return result;
    }

    public StaticEntity() {
    }

    @Override
    public String toString() {
        return "StaticEntity{" +
                "mmsi=" + mmsi +
                ", shipName='" + shipName + '\'' +
                ", calledName='" + calledName + '\'' +
                ", shipType='" + shipType + '\'' +
                ", time=" + time +
                ", shipLength=" + shipLength +
                ", shipWidth=" + shipWidth +
                ", waterDepth=" + waterDepth +
                ", poType='" + poType + '\'' +
                ", eta='" + eta + '\'' +
                ", imo='" + imo + '\'' +
                ", destination='" + destination + '\'' +
                ", aisType='" + aisType + '\'' +
                '}';
    }
}
