package com.app.realtimechat.entities;

public class RequestInfo {
    String receivedUserUid, requestType, currentGroupName;

    public RequestInfo() {
    }

    public RequestInfo(String uid, String requestType) {
        this.receivedUserUid = uid;
        this.requestType = requestType;
    }


    public RequestInfo(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getReceivedUserUid() {
        return receivedUserUid;
    }

    public void setReceivedUserUid(String receivedUserUid) {
        this.receivedUserUid = receivedUserUid;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }

    public void setCurrentGroupName(String currentGroupName) {
        this.currentGroupName = currentGroupName;
    }
}
