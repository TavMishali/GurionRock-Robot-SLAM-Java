package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LidarData {
    private ConcurrentHashMap<String, List<TrackedObject>> lidarWorkerTrackers;

    public LidarData() {
        this.lidarWorkerTrackers = new ConcurrentHashMap<>();
    }

    public LidarData(ConcurrentHashMap<String, List<TrackedObject>> lidarWorkerTrackers) {
        this.lidarWorkerTrackers = lidarWorkerTrackers;
    }
    
    public ConcurrentHashMap<String, List<TrackedObject>> getLidarWorkers() {
        return lidarWorkerTrackers;
    }

    public List<TrackedObject> getlidarById(String id) {
        return lidarWorkerTrackers.get(id);
    }
    
}
