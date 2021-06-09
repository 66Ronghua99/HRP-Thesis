package com.ronghua.bledetect.network.responses;

public class CertResponse {
    private String username;
    private String encodedCsr;

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
