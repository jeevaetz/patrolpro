/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.patrolpro.model;

import java.util.Date;

/**
 *
 * @author ira
 */
public class LatLongObj implements Comparable {

    private double lat;
    private double log;
    private String employeeName;
    private String deviceNickname;
    private String time;
    private double accuracy;
    private boolean isMarker;
    private Integer markerId;
    private String imageUrl;
    private double speed;
    private String iconURL;
    private Date markerScanned;

    public LatLongObj() {

    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * @return the log
     */
    public double getLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(double log) {
        this.log = log;
    }

    /**
     * @return the employeeName
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * @param employeeName the employeeName to set
     */
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return the accuracy
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return the deviceNickname
     */
    public String getDeviceNickname() {
        return deviceNickname;
    }

    /**
     * @param deviceNickname the deviceNickname to set
     */
    public void setDeviceNickname(String deviceNickname) {
        this.deviceNickname = deviceNickname;
    }

    /**
     * @return the isMarker
     */
    public boolean isIsMarker() {
        return isMarker;
    }

    /**
     * @param isMarker the isMarker to set
     */
    public void setIsMarker(boolean isMarker) {
        this.isMarker = isMarker;
    }

    /**
     * @return the markerId
     */
    public Integer getMarkerId() {
        return markerId;
    }

    /**
     * @param markerId the markerId to set
     */
    public void setMarkerId(Integer markerId) {
        this.markerId = markerId;
    }

    /**
     * @return the speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return the iconURL
     */
    public String getIconURL() {
        return iconURL;
    }

    /**
     * @param iconURL the iconURL to set
     */
    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    /**
     * @return the markerScanned
     */
    public Date getMarkerScanned() {
        return markerScanned;
    }

    /**
     * @param markerScanned the markerScanned to set
     */
    public void setMarkerScanned(Date markerScanned) {
        this.markerScanned = markerScanned;
    }

    @Override
    public int compareTo(Object o) {
        try {
            LatLongObj comp = (LatLongObj)o;
            return this.getMarkerScanned().compareTo(comp.getMarkerScanned());
        } catch (Exception exe) {}
        return 1;
    }

}
