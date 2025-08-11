package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String senderName;
    private String error;

    public CrashedBroadcast(String senderName, String error) {
        this.senderName = senderName;
        this.error = error;
    }

    public String getName() {
        return senderName;
    }

    public String getError() {
        return error;
    }
}
