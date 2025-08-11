package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.Event;

public class DetectObjectsEvent implements Event<Boolean> {
    private String senderName;
    private StampedDetectedObjects stampedDetectedObjects;

    public DetectObjectsEvent(String senderName, StampedDetectedObjects detectedObjects) {
        this.senderName = senderName;
        this.stampedDetectedObjects = detectedObjects;
    }

    public String getSenderName() {
        return senderName;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    public int getDetectedObjectsTime() {
        return stampedDetectedObjects.getTime();
    }
}
