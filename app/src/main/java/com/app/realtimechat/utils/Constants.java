package com.app.realtimechat.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String CHILD_USERS = "Users";
    public static final String CHILD_CONTACTS = "Contacts";
    public static final String CHILD_GROUPS = "Groups";
    public static final String CHILD_MESSAGES = "Messages";
    public static final String CHILD_REQUEST = "Contacts request";

    public static final String getCurrentDatetime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY - hh:mm a");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
