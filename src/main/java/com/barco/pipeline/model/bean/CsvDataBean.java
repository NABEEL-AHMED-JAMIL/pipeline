package com.barco.pipeline.model.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CsvDataBean {

    private String deviceId;
    private String deviceType;
    private String driveByTime;
    private String latitude;
    private String longitude;
    private String driveByTimeOut;
    private String latitudeOut;
    private String longitudeOut;

    public CsvDataBean() {
    }

    public CsvDataBean(String deviceId, String deviceType, String driveByTime,
        String latitude, String longitude, String driveByTimeOut,
        String latitudeOut, String longitudeOut) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.driveByTime = driveByTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.driveByTimeOut = driveByTimeOut;
        this.latitudeOut = latitudeOut;
        this.longitudeOut = longitudeOut;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDriveByTime() {
        return driveByTime;
    }

    public void setDriveByTime(String driveByTime) {
        this.driveByTime = driveByTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDriveByTimeOut() {
        return driveByTimeOut;
    }

    public void setDriveByTimeOut(String driveByTimeOut) {
        this.driveByTimeOut = driveByTimeOut;
    }

    public String getLatitudeOut() {
        return latitudeOut;
    }

    public void setLatitudeOut(String latitudeOut) {
        this.latitudeOut = latitudeOut;
    }

    public String getLongitudeOut() {
        return longitudeOut;
    }

    public void setLongitudeOut(String longitudeOut) {
        this.longitudeOut = longitudeOut;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}