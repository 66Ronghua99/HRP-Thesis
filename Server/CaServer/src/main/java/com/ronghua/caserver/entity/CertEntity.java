package com.ronghua.caserver.entity;

public class CertEntity {
    private String username;
    private String encodedCsr;
    private long timeMills;

    public long getTimeMills() {
        return timeMills;
    }

    public void setTimeMills(long timeMills) {
        this.timeMills = timeMills;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncodedCsr() {
        return encodedCsr;
    }

    public void setEncodedCsr(String encodedCsr) {
        this.encodedCsr = encodedCsr;
    }
}
