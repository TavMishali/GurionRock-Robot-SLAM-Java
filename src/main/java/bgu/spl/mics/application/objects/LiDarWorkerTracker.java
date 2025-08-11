package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.mics.application.messages.DetectObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using
 * data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private String id;
    private int frequency;
    private volatile STATUS status;
    private LinkedList<TrackedObject> lastTrackedObjects;
    private LinkedList<TrackedObject> trackedObjectsHistory;
    private LiDarDataBase lidarsData = null;
    private int globalTime = 0;

    public LiDarWorkerTracker(String id, int frequency, String filePath) {
        this.id = id;
        this.frequency = frequency;
        this.lastTrackedObjects = new LinkedList<>();
        this.trackedObjectsHistory = new LinkedList<>();
        this.lidarsData = LiDarDataBase.getInstance(filePath);
        this.status = STATUS.UP;
    }

    public void increaseTime() {
        globalTime++;
    }

    public STATUS getStatus() {
        return status;
    }

    public String getID() {
        return id;
    }

    public CloudPoint[] createCloudPoint(List<List<Double>> cloudPoint) {
        CloudPoint[] newCloudPoints = new CloudPoint[cloudPoint.size()];
        int index = 0;

        for (List<Double> pointList : cloudPoint) {
            CloudPoint newCloudPoint = new CloudPoint(pointList.get(0), pointList.get(1));
            newCloudPoints[index] = newCloudPoint;
            index++;
        }

        return newCloudPoints;
    }

    public LinkedList<TrackedObject> transformDetectedObjects(DetectObjectsEvent detectObjectsEvent) {
        int currentTime = detectObjectsEvent.getDetectedObjectsTime();
        StampedDetectedObjects stampedDetectedObjects = detectObjectsEvent.getStampedDetectedObjects();

        for (StampedCloudPoints SCP : lidarsData.getStampedCloudPoints()) {
            if (SCP.getTime() == currentTime) {
                for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()) {
                    if (SCP.getId().equals(detectedObject.getId())) {
                        CloudPoint[] trackCloudPoints = createCloudPoint(SCP.getCloudPoints());
                        TrackedObject newTrackedObject = new TrackedObject(SCP.getId(), SCP.getTime(), trackCloudPoints,
                                stampedDetectedObjects.getDescriptionById(SCP.getId()));

                        lastTrackedObjects.add(newTrackedObject);
                    }
                }
            }
        }

        LinkedList<TrackedObject> trackedObjects = addTrackedObjects();
        return trackedObjects;
    }

    @SuppressWarnings("static-access")
    public LinkedList<TrackedObject> addTrackedObjects() {
        LinkedList<TrackedObject> toSendTrackedObjects = new LinkedList<>();
        LinkedList<TrackedObject> toRemove = new LinkedList<>();

        for (TrackedObject trackedObject : lastTrackedObjects) {
            if ((trackedObject.getTime() + frequency) <= globalTime) {
                if (trackedObject.getId().equals("ERROR")) {
                    status = STATUS.ERROR;
                    break;
                } else {
                    toRemove.add(trackedObject);
                    toSendTrackedObjects.add(trackedObject);
                }
            }
        }
        if (status != STATUS.ERROR) {
            lastTrackedObjects.removeAll(toRemove);
            trackedObjectsHistory.addAll(toRemove);

            StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

            if (!toSendTrackedObjects.isEmpty()) {
                statisticalFolder.increaseTrackedObjects(new AtomicInteger(toSendTrackedObjects.size()));
                statisticalFolder.setLidarData(id, toSendTrackedObjects);
            }

            if (lastTrackedObjects.isEmpty() && statisticalFolder.getAmountOfCamerasAlive() == 0
                    && lidarsData.maxTime + frequency < globalTime) {
                status = STATUS.DOWN;
                statisticalFolder.changeAmountOfLidarsAlive(-1);
            }

            return toSendTrackedObjects;
        }

        else {
            return null;
        }
    }

    public LinkedList<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public LinkedList<TrackedObject> getHistoyTrackedObjects() {
        return trackedObjectsHistory;
    }

    public LiDarDataBase getLiDarDataBase() {// for unitest
        return lidarsData;
    }

    public void setTimeZeroUnitTest() {
        this.globalTime = 0;
    }
}