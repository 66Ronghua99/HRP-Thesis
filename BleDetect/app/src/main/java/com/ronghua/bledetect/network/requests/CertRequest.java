package com.ronghua.bledetect.network.requests;

public class CertRequest {
    //this username is account unique, can be authenticated by token or other authentication methods
    //possible with two factor authentication, e-mail or mobile, but we skip this step
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
