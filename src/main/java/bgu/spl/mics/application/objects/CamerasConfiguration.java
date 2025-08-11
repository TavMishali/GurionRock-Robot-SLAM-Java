package bgu.spl.mics.application.objects;

public class CamerasConfiguration {
    private int id;
    private int frequency;
    private String camera_key;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getCamera_key() {
        return camera_key;
    }

    public void setCamera_key(String cameraKey) {
        this.camera_key = cameraKey;
    }
}