import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.FusionSlamService;
import static org.junit.jupiter.api.Assertions.*;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class FusionSlamServiceTest {
    StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
    FusionSlam fusionSlam = FusionSlam.getInstance();
    FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam, new CountDownLatch(0));
    String senderName = "Lidar1";
    CloudPoint cloudPoint = new CloudPoint(1.5, 2.5);
    CloudPoint cloudPoint2 = new CloudPoint(0.05, 12.78);
    CloudPoint[] cloudPoints = new CloudPoint[1];
    Pose pose = new Pose((float) 0.1, (float) 0.1, (float) 10, 1);
    Pose pose2 = new Pose((float) 0.5, (float) 0.9, (float) 100, 2);

    /**
     * transform tracked object into new landmarks
     *
     * @pre: none
     * @post: fusionSlam.landmarks will contain the two landmarks with the same values (id,description,coordinates) as received from the tracked objects
     */
    @Test
    public void NewTrackedObjects() {
        cloudPoints[0] = cloudPoint;
        fusionSlam.addPose(pose);
        fusionSlam.addPose(pose2);

        TrackedObject trackedObject = new TrackedObject("Wall1", 1, cloudPoints, "Wall");
        TrackedObject trackedObject2 = new TrackedObject("Wall2", 2, cloudPoints, "Wall");
        LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
        trackedObjects.add(trackedObject);
        trackedObjects.add(trackedObject2);

        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);
        LinkedList<CloudPoint> transformedCloudPoints = fusionSlam.convertCloudPoints(trackedObject);
        fusionSlam.transformTrackedObjectEvent(event);

        assertEquals(2, fusionSlam.getLandMarks().size());
        assertEquals(2, statisticalFolder.getNumLandMarks());

        assertEquals("Wall1", fusionSlam.getLandMarks().get(0).getId());
        assertEquals("Wall", fusionSlam.getLandMarks().get(0).getDescription());

        assertEquals("Wall2", fusionSlam.getLandMarks().get(1).getId());
        assertEquals("Wall", fusionSlam.getLandMarks().get(1).getDescription());

        LinkedList<CloudPoint> realCloudPoints = fusionSlam.getLandMarks().get(0).getCoordinates();
        assertEquals(transformedCloudPoints, realCloudPoints);

        // Cleanup
        fusionSlam.getLandMarks().clear();
    }

    /**
     * transform tracked object into landmarks and calculate new coordinates
     *
     * @pre: landmark exists in fusionSlam.landmarks
     * @post: fusionSlam.landmarks will contain only the existing landmark but with new calculated coordinates that is the avg of the 
     * coordinates of the landmark and the coordinates received in the Tracked event 
     */
    @Test
    public void ExistingTrackedObject() {
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        cloudPoints.add(cloudPoint);
        cloudPoints.add(cloudPoint2);
        CloudPoint newCloudPoint = new CloudPoint(-2.5, 3.5);
        CloudPoint newCloudPoint2 = new CloudPoint(15.5, -4.7809);
        CloudPoint[] newCloudPoints = new CloudPoint[2];
        newCloudPoints[0] = newCloudPoint;
        newCloudPoints[1] = newCloudPoint2;

        fusionSlam.addPose(pose);
        fusionSlam.addPose(pose2);
        LandMark landMark = new LandMark("Wall1", "Wall", cloudPoints);
        fusionSlam.addLandMark(landMark);

        TrackedObject trackedObject = new TrackedObject("Wall1", 1, newCloudPoints, "Wall");
        LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
        trackedObjects.add(trackedObject);

        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);
        LinkedList<CloudPoint> transformedCloudPoints = fusionSlam.convertCloudPoints(trackedObject);
        fusionSlam.transformTrackedObjectEvent(event);

        // asserting 0 because added landmark manually so we dont update statistical
        // folder
        assertEquals(0, statisticalFolder.getNumLandMarks());
        assertEquals(1, fusionSlam.getLandMarks().size());

        assertEquals("Wall1", fusionSlam.getLandMarks().get(0).getId());
        assertEquals("Wall", fusionSlam.getLandMarks().get(0).getDescription());

        LinkedList<CloudPoint> realCloudPoints = fusionSlam.getLandMarks().get(0).getCoordinates();
        double x1 = (transformedCloudPoints.get(0).getX() + cloudPoint.getX()) / 2;
        double y1 = (transformedCloudPoints.get(0).getY() + cloudPoint.getY()) / 2;
        assertEquals(x1, realCloudPoints.get(0).getX());
        assertEquals(y1, realCloudPoints.get(0).getY());

        double x2 = (transformedCloudPoints.get(1).getX() + cloudPoint2.getX()) / 2;
        double y2 = (transformedCloudPoints.get(1).getY() + cloudPoint2.getY()) / 2;
        assertEquals(x2, realCloudPoints.get(1).getX());
        assertEquals(y2, realCloudPoints.get(1).getY());

        // Cleanup
        fusionSlam.getLandMarks().clear();
    }

    @Test
    public void TrackedObjectWithFewerCoordinatesThanLandMark() {
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        cloudPoints.add(cloudPoint);
        cloudPoints.add(cloudPoint2);
        CloudPoint newCloudPoint = new CloudPoint(-2.5, 3.5);
        CloudPoint[] newCloudPoints = new CloudPoint[1];
        newCloudPoints[0] = newCloudPoint;

        fusionSlam.addPose(pose);
        fusionSlam.addPose(pose2);
        LandMark landMark = new LandMark("Wall1", "Wall", cloudPoints);
        fusionSlam.addLandMark(landMark);

        TrackedObject trackedObject = new TrackedObject("Wall1", 1, newCloudPoints, "Wall");
        LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
        trackedObjects.add(trackedObject);

        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);
        LinkedList<CloudPoint> transformedCloudPoints = fusionSlam.convertCloudPoints(trackedObject);
        fusionSlam.transformTrackedObjectEvent(event);

        // asserting 0 because added landmark manually so we dont update statistical
        // folder
        assertEquals(0, statisticalFolder.getNumLandMarks());
        assertEquals(1, fusionSlam.getLandMarks().size());

        assertEquals("Wall1", fusionSlam.getLandMarks().get(0).getId());
        assertEquals("Wall", fusionSlam.getLandMarks().get(0).getDescription());

        LinkedList<CloudPoint> realCloudPoints = fusionSlam.getLandMarks().get(0).getCoordinates();
        double x1 = (transformedCloudPoints.get(0).getX() + cloudPoint.getX()) / 2;
        double y1 = (transformedCloudPoints.get(0).getY() + cloudPoint.getY()) / 2;
        assertEquals(x1, realCloudPoints.get(0).getX());
        assertEquals(y1, realCloudPoints.get(0).getY());

        assertEquals(cloudPoint2.getX(), realCloudPoints.get(1).getX());
        assertEquals(cloudPoint2.getY(), realCloudPoints.get(1).getY());

        // Cleanup
        fusionSlam.getLandMarks().clear();
    }

    @Test
    public void TrackedObjectWithMoreCoordinatesThanLandMark() {
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        cloudPoints.add(cloudPoint);
        CloudPoint newCloudPoint = new CloudPoint(-2.5, 3.5);
        CloudPoint newCloudPoint2 = new CloudPoint(15.5, -4.7809);
        CloudPoint[] newCloudPoints = new CloudPoint[2];
        newCloudPoints[0] = newCloudPoint;
        newCloudPoints[1] = newCloudPoint2;

        fusionSlam.addPose(pose);
        fusionSlam.addPose(pose2);
        LandMark landMark = new LandMark("Wall1", "Wall", cloudPoints);
        fusionSlam.addLandMark(landMark);

        TrackedObject trackedObject = new TrackedObject("Wall1", 1, newCloudPoints, "Wall");
        LinkedList<TrackedObject> trackedObjects = new LinkedList<>();
        trackedObjects.add(trackedObject);

        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);
        LinkedList<CloudPoint> transformedCloudPoints = fusionSlam.convertCloudPoints(trackedObject);
        fusionSlam.transformTrackedObjectEvent(event);

        // asserting 0 because added landmark manually so we dont update statistical
        // folder
        assertEquals(0, statisticalFolder.getNumLandMarks());
        assertEquals(1, fusionSlam.getLandMarks().size());

        assertEquals("Wall1", fusionSlam.getLandMarks().get(0).getId());
        assertEquals("Wall", fusionSlam.getLandMarks().get(0).getDescription());

        LinkedList<CloudPoint> realCloudPoints = fusionSlam.getLandMarks().get(0).getCoordinates();
        double x1 = (transformedCloudPoints.get(0).getX() + cloudPoint.getX()) / 2;
        double y1 = (transformedCloudPoints.get(0).getY() + cloudPoint.getY()) / 2;
        assertEquals(x1, realCloudPoints.get(0).getX());
        assertEquals(y1, realCloudPoints.get(0).getY());

        assertEquals(transformedCloudPoints.get(1).getX(), realCloudPoints.get(1).getX());
        assertEquals(transformedCloudPoints.get(1).getY(), realCloudPoints.get(1).getY());

        // Cleanup
        fusionSlam.getLandMarks().clear();
    }
}
