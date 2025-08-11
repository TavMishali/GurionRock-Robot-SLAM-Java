package bgu.spl.mics.application.objects;

import java.util.List;

public class CrashedOutput {
    private String error;
    private String faultySensor;
    private CameraData lastCamerasFrame;
    private LidarData lastLidarWorkerTrackersFrame;
    private List<Pose> poses;
    private Output statistics;

    public CrashedOutput(String error, String faultySensor, CameraData lastCamerasFrame,
            LidarData lastLidarWorkerTrackersFrame, List<Pose> poses, Output statistics) {
        this.error = error;
        this.faultySensor = faultySensor;
        this.lastCamerasFrame = lastCamerasFrame;
        this.lastLidarWorkerTrackersFrame = lastLidarWorkerTrackersFrame;
        this.poses = poses;
        this.statistics = statistics;
    }

    public String getError() {
        return error;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public CameraData getLastCamerasFrame() {
        return lastCamerasFrame;
    }

    public LidarData getLastLidarWorkerTrackersFrame() {
        return lastLidarWorkerTrackersFrame;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public Output getStatistics() {
        return statistics;
    }

}
