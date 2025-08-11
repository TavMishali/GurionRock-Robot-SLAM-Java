package bgu.spl.mics.application.messages;
import java.util.LinkedList;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.Event;

public class TrackedObjectsEvent implements Event<Boolean> {
    private LinkedList<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(LinkedList<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public LinkedList<TrackedObject> getTrackedObjects(){return trackedObjects;}
}
