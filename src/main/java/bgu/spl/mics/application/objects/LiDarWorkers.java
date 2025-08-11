package bgu.spl.mics.application.objects;

import java.util.List;

public class LiDarWorkers {
    private List<LidarConfiguration> LidarConfigurations;
    private String lidars_data_path;

    // Getters and setters
    public List<LidarConfiguration> getLidarConfigurations() {
        return LidarConfigurations;
    }

    public void setLidarConfigurations(List<LidarConfiguration> lidarConfigurations) {
        this.LidarConfigurations = lidarConfigurations;
    }

    public String getLidarsDataPath() {
        return lidars_data_path;
    }

    public void setLidarsDataPath(String lidarsDataPath) {
        this.lidars_data_path = lidarsDataPath;
    }
}