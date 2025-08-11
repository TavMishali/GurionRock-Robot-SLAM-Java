package bgu.spl.mics.application.objects;

import java.util.List;

public class Cameras {
    private List<CamerasConfiguration> CamerasConfigurations;
    private String camera_datas_path;

    // Getters and setters
    public List<CamerasConfiguration> getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public void setCamerasConfigurations(List<CamerasConfiguration> camerasConfigurations) {
        this.CamerasConfigurations = camerasConfigurations;
    }

    public String getCamera_datas_path() {
        return camera_datas_path;
    }

    public void setCamera_datas_path(String cameraDatasPath) {
        this.camera_datas_path = cameraDatasPath;
    }
}