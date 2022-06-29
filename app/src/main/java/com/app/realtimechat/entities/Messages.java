package com.app.realtimechat.entities;

import java.util.HashMap;
import java.util.Map;

public class Messages {

    private String from, message, type, to, messageID, currentDatetime, name;

    public Messages() {
    }

    public Messages(String from, String message, String type, String to, String messageID, String currentDatetime) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.currentDatetime = currentDatetime;
    }

    public Messages(String from, String message, String type, String to, String messageID, String currentDatetime, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.currentDatetime = currentDatetime;
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getCurrentDatetime() {
        return currentDatetime;
    }

    public void setCurrentDatetime(String currentDatetime) {
        this.currentDatetime = currentDatetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map getMessagesBody() {
        Map messageTextBody = new HashMap();
        messageTextBody.put("message", getMessage());
        messageTextBody.put("type", getType());
        messageTextBody.put("from", getFrom());
        messageTextBody.put("to", getTo());
        messageTextBody.put("messageID", getMessageID());
        messageTextBody.put("currentDatetime", getCurrentDatetime());
        return messageTextBody;
    }
}
