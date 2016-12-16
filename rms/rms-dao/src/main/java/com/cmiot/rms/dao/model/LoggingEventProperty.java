package com.cmiot.rms.dao.model;

public class LoggingEventProperty extends LoggingEventPropertyKey {
    private String mappedValue;

    public String getMappedValue() {
        return mappedValue;
    }

    public void setMappedValue(String mappedValue) {
        this.mappedValue = mappedValue == null ? null : mappedValue.trim();
    }
}