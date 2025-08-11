package bgu.spl.mics.application.objects;

public class Configuration {
    private Cameras Cameras;
    private LiDarWorkers LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    // Getters and setters
    public Cameras getCameras() {
        return Cameras;
    }

    public void setCameras(Cameras cameras) {
        this.Cameras = cameras;
    }

    public LiDarWorkers getLidarWorkers() {
        return LiDarWorkers;
    }

    public void setLidarWorkers(LiDarWorkers lidarWorkers) {
        this.LiDarWorkers = lidarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public void setPoseJsonFile(String poseJsonFile) {
        this.poseJsonFile = poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public void setTickTime(int tickTime) {
        this.TickTime = tickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        this.Duration = duration;
    }
}