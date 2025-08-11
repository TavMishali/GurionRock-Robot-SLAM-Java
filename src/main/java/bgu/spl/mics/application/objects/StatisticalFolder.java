package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks
 * identified.
 */
public class StatisticalFolder {
    private int systemRunTime = 0;
    private AtomicInteger numDetectedObjects = new AtomicInteger(0);
    private AtomicInteger numTrackedObjects = new AtomicInteger(0);
    private int numLandMarks = 0;
    private CameraData lastCamerasFrame = new CameraData();
    private LidarData lastLidarWorkerTrackersFrame = new LidarData();
    private AtomicInteger amountOfCamerasAlive = new AtomicInteger(0);
    private AtomicInteger amountOfLidarsAlive = new AtomicInteger(0);

    public void setSystemRunTime(int runTime) {
        this.systemRunTime = runTime;
    }

    public void increaseDetectedObjects(AtomicInteger amountOfObjects) {
        int currAmountOfObjects;
        int futureAmountOfObjects;

        do {
            currAmountOfObjects = numDetectedObjects.get();
            futureAmountOfObjects = numDetectedObjects.get() + amountOfObjects.get();
        } while (!numDetectedObjects.compareAndSet(currAmountOfObjects, futureAmountOfObjects));
    }

    public void increaseTrackedObjects(AtomicInteger amountOfObjects) {
        int currAmountOfObjects;
        int futureAmountOfObjects;

        do {
            currAmountOfObjects = numTrackedObjects.get();
            futureAmountOfObjects = numTrackedObjects.get() + amountOfObjects.get();
        } while (!numTrackedObjects.compareAndSet(currAmountOfObjects, futureAmountOfObjects));
    }

    public void changeAmountOfCamerasAlive(int diff) {
        int currAmountOfObjects;
        int futureAmountOfObjects;

        do {
            currAmountOfObjects = amountOfCamerasAlive.get();
            futureAmountOfObjects = amountOfCamerasAlive.get() + diff;
        } while (!amountOfCamerasAlive.compareAndSet(currAmountOfObjects, futureAmountOfObjects));
    }

    public void changeAmountOfLidarsAlive(int diff) {
        int currAmountOfObjects;
        int futureAmountOfObjects;

        do {
            currAmountOfObjects = amountOfLidarsAlive.get();
            futureAmountOfObjects = amountOfLidarsAlive.get() + diff;
        } while (!amountOfLidarsAlive.compareAndSet(currAmountOfObjects, futureAmountOfObjects));
    }

    public void setCameraData(String cameraId, StampedDetectedObjects stampedDetectedObjects) {
        List<StampedDetectedObjects> newList = new CopyOnWriteArrayList<>();
        newList.add(stampedDetectedObjects);
        lastCamerasFrame.getCameras().put(cameraId, newList);
    }

    public void setLidarData(String lidarWorkerId, List<TrackedObject> trackedObjects) {
        lastLidarWorkerTrackersFrame.getLidarWorkers().put(lidarWorkerId, trackedObjects);
    }

    public void increaseLandMarks(int amountOfLandMarks) {
        this.numLandMarks += amountOfLandMarks;
    }

    public int getSystemRunTime() {
        return systemRunTime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandMarks() {
        return numLandMarks;
    }

    public CameraData getCameraData() {
        return this.lastCamerasFrame;
    }

    public LidarData getLidarData() {
        return this.lastLidarWorkerTrackersFrame;
    }

    public int getAmountOfCamerasAlive() {
        return this.amountOfCamerasAlive.get();
    }

    public int getAmountOfLidarsAlive() {
        return this.amountOfLidarsAlive.get();
    }

    private static class SingletonStatisticalFolder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    // Public method to get the singleton instance
    public static StatisticalFolder getInstance() {
        return SingletonStatisticalFolder.INSTANCE;
    }
}
