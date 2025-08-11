package bgu.spl.mics.application.objects;

import java.util.Objects;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description,
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;
    private String description;
    private CloudPoint[] coordinates;

    public TrackedObject(String id, int time, CloudPoint[] coordinates, String description) {
        this.id = id;
        this.time = time;
        this.coordinates = coordinates;
        this.description = description;
    }

    public String getId() {
        return this.id;
    }

    public CloudPoint[] getCloudPoints() {
        return this.coordinates;
    }

    public int getTime() {
        return this.time;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TrackedObject that = (TrackedObject) obj;
        return Objects.equals(id, that.id);
    }
}
