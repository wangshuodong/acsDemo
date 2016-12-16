package com.cmiot.rms.dao.model;

public class LoggingEventPropertyKey {
    private Long eventId;

    private String mappedKey;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getMappedKey() {
        return mappedKey;
    }

    public void setMappedKey(String mappedKey) {
        this.mappedKey = mappedKey == null ? null : mappedKey.trim();
    }
}