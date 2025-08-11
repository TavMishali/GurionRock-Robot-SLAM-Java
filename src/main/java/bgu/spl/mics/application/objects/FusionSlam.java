package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import com.google.gson.Gson;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping
 * (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update
 * a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam
 * exists.
 */
public class FusionSlam {
    // Public method to get the singleton instance
    private ArrayList<LandMark> landMarks;
    private LinkedList<Pose> poses;
    private StatisticalFolder statisticalFolder;
    private LinkedList<TrackedObjectsEvent> unhandledEvents;

    public FusionSlam() {
        this.landMarks = new ArrayList<>();
        this.poses = new LinkedList<>();
        this.unhandledEvents = new LinkedList<>();
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    public void addLandMark(LandMark landMark) {
        this.landMarks.add(landMark);
    }

    public void addPose(Pose pose) {
        this.poses.add(pose);
    }

    public ArrayList<LandMark> getLandMarks() {
        return this.landMarks;
    }

    public Map<String, LandMark> createLandMarkMap() {
        Map<String, LandMark> landMarkMap = new HashMap<>();

        for (LandMark landMark : landMarks) {
            landMarkMap.put(landMark.getId(), landMark);
        }

        return landMarkMap;
    }

    public void writeOutput() {
        Output output = new Output(statisticalFolder, createLandMarkMap());

        try (FileWriter writer = new FileWriter("output.json")) {
            new Gson().toJson(output, writer);
        } catch (IOException e) {
        }
    }

    public void writeCrashedOutput(String error, String senderName) {
        CameraData cameraData = statisticalFolder.getCameraData();
        LidarData lidarData = statisticalFolder.getLidarData();
        Output output = new Output(statisticalFolder, createLandMarkMap());
        CrashedOutput crashedOutput = new CrashedOutput(error, senderName, cameraData, lidarData, poses,
                output);

        try (FileWriter writer = new FileWriter("output.json")) {
            new Gson().toJson(crashedOutput, writer);
        } catch (IOException e) {
            
        }
    }

    public LandMark getLandMarkById(String id) {
        for (LandMark landMark : landMarks) {
            if (landMark.getId().equals(id))
                return landMark;
        }

        return null;
    }

    public double[] calculatePoint(double xRobot, double yRobot, double yaw, double xPoint, double yPoint) {
        double radianYaw = (Math.PI * yaw) / 180;
        double cosYaw = Math.cos(radianYaw);
        double sinYaw = Math.sin(radianYaw);
        double xGlobal = (cosYaw * xPoint) - (sinYaw * yPoint) + xRobot;
        double yGlobal = (sinYaw * xPoint) + (cosYaw * yPoint) + yRobot;

        double[] coordinates = { xGlobal, yGlobal };
        return coordinates;
    }

    public LinkedList<CloudPoint> convertCloudPoints(TrackedObject trackedObject) {
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        int time = trackedObject.getTime();

        if (poses.size() < time - 1) {
            return null;
        } else {
            Pose currPose = poses.get(time - 1);

            for (CloudPoint currPoint : trackedObject.getCloudPoints()) {
                double[] coordinates = calculatePoint(currPose.getX(),
                        currPose.getY(), currPose.getYaw(), currPoint.getX(), currPoint.getY());
                CloudPoint cloudPoint = new CloudPoint(coordinates[0], coordinates[1]);
                cloudPoints.add(cloudPoint);
            }

            return cloudPoints;
        }
    }

    public void transformUnhandledEvents() {
        for (TrackedObjectsEvent trackedObjectsEvent : unhandledEvents) {
            transformTrackedObjectEvent(trackedObjectsEvent);
        }
    }

    public void transformTrackedObjectEvent(TrackedObjectsEvent trackedObjectsEvent) {
        int amountOfNewLandMarks = 0;
        LinkedList<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();

        for (TrackedObject trackedObject : trackedObjects) {
            LandMark landMark = getLandMarkById(trackedObject.getId());
            LinkedList<CloudPoint> cloudPoints = convertCloudPoints(trackedObject);

            // this means that there is no pose available for the event
            if (cloudPoints == null) {
                // if the list doesn't contain the event add him
                if (!unhandledEvents.contains(trackedObjectsEvent)) {
                    unhandledEvents.add(trackedObjectsEvent);
                }
            } else { // the pose exist so the cloudPoints exist
                if (landMark == null) {
                    landMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), cloudPoints);
                    amountOfNewLandMarks++;
                    landMarks.add(landMark);
                } else {
                    for (int i = 0; i < cloudPoints.size(); i++) {
                        CloudPoint calculatedPoint;
                        if (i > landMark.getCoordinates().size() - 1) {
                            calculatedPoint = cloudPoints.get(i);
                        } else {
                            CloudPoint newPoint = cloudPoints.get(i);
                            CloudPoint oldPoint = landMark.getCoordinates().get(i);

                            double newX = (newPoint.getX() + oldPoint.getX()) / 2.0;
                            double newY = (newPoint.getY() + oldPoint.getY()) / 2.0;
                            calculatedPoint = new CloudPoint(newX, newY);
                        }

                        landMark.setCoordinate(i, calculatedPoint);
                    }
                }
            }
        }
        statisticalFolder.increaseLandMarks(amountOfNewLandMarks);
    }
}
