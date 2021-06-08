package com.ronghua.caserver.entity;

import java.util.List;

public class CsrList {
    private String username;
    private List<CsrEntity> csrEntities;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<CsrEntity> getCsrEntities() {
        return csrEntities;
    }

    public void setCsrEntities(List<CsrEntity> csrEntities) {
        this.csrEntities = csrEntities;
    }
}
