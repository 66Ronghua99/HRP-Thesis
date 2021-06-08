package com.ronghua.caserver.entity;

public class CertEntity {
    private int userId;
    private String username;
    private String encodedCert;
    private long timeMillis;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncodedCert() {
        return encodedCert;
    }

    public void setEncodedCert(String encodedCert) {
        this.encodedCert = encodedCert;
    }
}
