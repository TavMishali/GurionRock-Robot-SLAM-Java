package bgu.spl.mics.application.objects;
import java.util.List;
/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects; 
    }

    public int getTime(){return time;}

    public List<DetectedObject> getDetectedObjects(){return detectedObjects;}

    public String getDescriptionById(String id) {
        for(DetectedObject detectedObject: detectedObjects) {
            if(detectedObject.getId().equals(id)) {
                return detectedObject.getDescription();
            }
        }

        // if didnt find return null
        return null;
    }
}
