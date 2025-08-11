package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    String id;
    String description;
    LinkedList<CloudPoint> coordinates;

    public LandMark(String id, String description, LinkedList<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return this.id;
    }

    public LinkedList<CloudPoint> getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinate(int index, CloudPoint point) {
        if (index > coordinates.size() - 1) {
            coordinates.add(point);
        } else {
            coordinates.set(index, point);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
