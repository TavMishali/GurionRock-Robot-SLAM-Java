package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraData {
    private Map<String, List<StampedDetectedObjects>> cameras;

    public CameraData() {
        this.cameras = new HashMap<>();
    }

    public CameraData(Map<String, List<StampedDetectedObjects>> cameras) {
        this.cameras = cameras;
    }

    public Map<String, List<StampedDetectedObjects>> getCameras() {
        return cameras;
    }

    public List<StampedDetectedObjects> getCameraById(String id) {
        return cameras.get(id);
    }
}
