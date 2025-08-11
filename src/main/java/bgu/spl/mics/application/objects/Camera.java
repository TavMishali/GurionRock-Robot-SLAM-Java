package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */

public class Camera {
    private int id;
    private int frequncy;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    private int globalTime;
    StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

    public Camera(int id, int frequncy, String filePath) {
        this.id = id;
        this.frequncy = frequncy;
        this.status = STATUS.UP;
        this.globalTime = 0;

        JsonParser parser = new JsonParser();
        CameraData cameraData = parser.getCameraData(filePath);
        detectedObjectsList = cameraData.getCameraById("camera" + id);
    }

    public int getFrequency() {
        return frequncy;
    }

    public int getId() {
        return id;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return this.status;
    }

    public void increaseTime() {
        this.globalTime++;
    }

    public StampedDetectedObjects transformDetectedObject() {
        // Check if this object has an error
        StampedDetectedObjects currentObject = detectedObjectsList.stream()
                .filter(obj -> obj.getTime() == globalTime)
                .findFirst()
                .orElse(null);

        if (currentObject != null) {
            boolean hasError = currentObject.getDetectedObjects().stream()
                    .anyMatch(obj -> "ERROR".equals(obj.getId()));
            if (hasError) {
                statisticalFolder.changeAmountOfCamerasAlive(-1);
                setStatus(STATUS.ERROR);
                return null;
            }
        }
        
        StampedDetectedObjects processedObject = detectedObjectsList.stream()
                .filter(obj -> obj.getTime() + frequncy == globalTime)
                .findFirst()
                .orElse(null);

        // Process objects at time T + frequency
        if (processedObject != null) {
            statisticalFolder.increaseDetectedObjects(
                    new AtomicInteger(processedObject.getDetectedObjects().size()));
            statisticalFolder.setCameraData("camera" + id, processedObject);
            return processedObject;
        }

        StampedDetectedObjects lastDetectedObject = detectedObjectsList.get(detectedObjectsList.size() - 1);

        // If no objects are left
        if (globalTime > lastDetectedObject.getTime() + frequncy) {
            // shutting camera down
            setStatus(STATUS.DOWN);
            statisticalFolder.changeAmountOfCamerasAlive(-1);
        }

        return null;
    }
}
