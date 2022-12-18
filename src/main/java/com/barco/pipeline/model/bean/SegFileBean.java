package com.barco.pipeline.model.bean;

import com.google.gson.Gson;

public class SegFileBean {

    private String key;

    public SegFileBean() {
    }

    public SegFileBean(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
