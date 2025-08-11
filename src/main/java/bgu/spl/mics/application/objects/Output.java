package bgu.spl.mics.application.objects;

import java.util.Map;

public class Output {
    private int systemRunTime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandMarks;
    private Map<String, LandMark> landMarks;

    public Output(StatisticalFolder statisticalFolder, Map<String, LandMark> landMarks) {
        this.systemRunTime = statisticalFolder.getSystemRunTime();
        this.numDetectedObjects = statisticalFolder.getNumDetectedObjects();
        this.numTrackedObjects = statisticalFolder.getNumTrackedObjects();
        this.numLandMarks = statisticalFolder.getNumLandMarks();
        this.landMarks = landMarks;
    }

    public int getSystemRunTime() {
        return systemRunTime;
    }

    // Getter for numDetectedObjects
    public int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    // Getter for numTrackedObjects
    public int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    // Getter for numLandMarks
    public int getNumLandMarks() {
        return numLandMarks;
    }

    public Map<String, LandMark> getLandMarks() {
        return landMarks;
    }

    public LandMark getLandMarkById(String id) {
        return landMarks.get(id);
    }
}
