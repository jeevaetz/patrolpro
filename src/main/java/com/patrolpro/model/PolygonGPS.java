/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.patrolpro.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author ira
 */
public class PolygonGPS implements Serializable {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer pointCount;

    public PolygonGPS() {
        
    }
    
    /**
     * @return the latitude
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the pointCount
     */
    public Integer getPointCount() {
        return pointCount;
    }

    /**
     * @param pointCount the pointCount to set
     */
    public void setPointCount(Integer pointCount) {
        this.pointCount = pointCount;
    }
}
