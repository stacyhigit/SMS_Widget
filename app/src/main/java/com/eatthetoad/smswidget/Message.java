package com.eatthetoad.smswidget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private final String id;
    private final Long date;
    private String content;
    private final int read;
    private final List<Recipient> recipientList = new ArrayList<>();

    public Message(String id, Long date, String content, int read) {
        this.id = id;
        this.date = date;
        this.content = content;
        this.read = read;
    }

    public void addRecipient(Recipient recipient) {
        this.recipientList.add(recipient);
    }

    public String getRecipientNumbers() {
        return recipientList.stream().map(Recipient::getNumber).collect(Collectors.joining(","));
    }

    public String getRecipientDisplayNames() {
        return recipientList.stream().map(Recipient::getHeader).collect(Collectors.joining(", "));
    }

    public String getHeader() {
        if (recipientList.size() == 1 ) {
            return recipientList.get(0).getHeader();
        } else {
            return "MMS";
        }
    }

    public String getId(){
        return id;
    }

    public String getFormattedDate() {
        return DateTimeUtils.getFormattedDate(date, true);
    }

    public String getContent() {
        if (content == null) {
            if (recipientList.size() > 1) {
                return getRecipientDisplayNames();
            } else {
                return "MMS";
            }
        }
        return content;
    }

    public int getRead() {
        return read;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
