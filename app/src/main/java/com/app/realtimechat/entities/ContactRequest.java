package com.app.realtimechat.entities;

public class ContactRequest {
    String uid,requestType;

    public ContactRequest() {
    }

    public ContactRequest(String uid, String requestType) {
        this.uid = uid;
        this.requestType = requestType;
    }


    public ContactRequest(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
