package com.ronghua.caserver.msgbody;

public class SignResponse {
    private String username;
    private String encodedCrt;
    private String error;
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncodedCrt() {
        return encodedCrt;
    }

    public void setEncodedCrt(String encodedCrt) {
        this.encodedCrt = encodedCrt;
    }
}
