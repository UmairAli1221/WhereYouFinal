package com.uberclone.whereyou.Model;

/**
 * Created by Umair Ali on 1/25/2018.
 */

public class Messages {
    private String message, type,messageImage,imageMessage;
    private long  time;
    private boolean seen;
    private String from;

    public Messages() {
    }

    public Messages(String message, String type, String messageImage, long time, boolean seen, String from) {
        this.message = message;
        this.type = type;
        this.messageImage = messageImage;
        this.time = time;
        this.seen = seen;
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

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getImageMessage() {
        return imageMessage;
    }

    public void setImageMessage(String imageMessage) {
        this.imageMessage = imageMessage;
    }
}
