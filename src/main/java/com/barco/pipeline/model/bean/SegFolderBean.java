package com.barco.pipeline.model.bean;

import com.google.gson.Gson;

public class SegFolderBean {

    private String externalId;
    private String uniqueId;
    private String expiryDays;

    public SegFolderBean() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(String expiryDays) {
        this.expiryDays = expiryDays;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
