import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.LinkedList;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.TrackedObject;

public class LidarServiceTest {
    static List<Double> point = new LinkedList<>();
    static List<Double> point2 = new LinkedList<>();
    static List<Double> point3 = new LinkedList<>();
    static List<List<Double>> list1 = new LinkedList<>();
    static List<List<Double>> list2 = new LinkedList<>();
    static List<List<Double>> list3 = new LinkedList<>();
    static List<StampedCloudPoints> stampedCloudPointsList = new LinkedList<>();
    static LiDarWorkerTracker liDarWorkerTracker = new LiDarWorkerTracker("2", 2, "./lidar_data.json");

    @BeforeAll
    public static void setup() {
        point.add(1.0);
        point.add(2.0);
        point2.add(3.0);
        point2.add(4.0);
        point3.add(5.0);
        point3.add(6.0);
        list1.add(point);
        list2.add(point2);
        list3.add(point3);

        StampedCloudPoints stamp1CloudPoints = new StampedCloudPoints("Wall_1", 3, list1);
        StampedCloudPoints stamp1CloudPoints2 = new StampedCloudPoints("Wall_2", 3, list2);
        StampedCloudPoints stamp1CloudPoints3 = new StampedCloudPoints("ChairBase_1", 3, list3);

        stampedCloudPointsList.add(stamp1CloudPoints);
        stampedCloudPointsList.add(stamp1CloudPoints2);
        stampedCloudPointsList.add(stamp1CloudPoints3);
    }

    /**
     * Transform detected objects into tracked objects and verify the
     * transformation.
     *
     * @pre: HistoyTrackedObjects is empty
     *       liderdataBase sulde have stemptedCloudePoints that mach the ones i
     *       created
     * 
     * @post: listTrackedObjects contains the corresponding TrackedObjects:
     *        - trackedObject1 with name "Wall_1", timestamp 3, and cloud points
     *        {(1.0, 2.0)}
     *        - trackedObject2 with name "Wall_2", timestamp 3, and cloud points
     *        {(3.0, 4.0)}
     *        - trackedObject3 with name "ChairBase_1", timestamp 3, and cloud
     *        points {(5.0, 6.0)}
     *        liDarWorkerTracker.getHistoyTrackedObjects() == listTrackedObjects at
     *        time=5
     */
    @SuppressWarnings("static-access")
    @Test
    public void TtransformDetectedObjects() {
        (liDarWorkerTracker.getLiDarDataBase()).setDataForUniTests(stampedCloudPointsList);
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();

        List<DetectedObject> detectedObjectsList = new LinkedList<>();
        DetectedObject newDetectedObject1 = new DetectedObject("Wall_1", "Wall");
        DetectedObject newDetectedObject2 = new DetectedObject("Wall_2", "Wall");
        DetectedObject newDetectedObject3 = new DetectedObject("ChairBase_1", "Chair Base");
        detectedObjectsList.add(newDetectedObject1);
        detectedObjectsList.add(newDetectedObject2);
        detectedObjectsList.add(newDetectedObject3);
        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(3, detectedObjectsList);
        DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent("camera1", stampedDetectedObjects);
        LinkedList<TrackedObject> listTrackedObjects = liDarWorkerTracker.transformDetectedObjects(detectObjectsEvent);

        CloudPoint[] cloudePoint1 = { new CloudPoint(1.0, 2.0) };
        CloudPoint[] cloudePoint2 = { new CloudPoint(3.0, 4.0) };
        CloudPoint[] cloudePoint3 = { new CloudPoint(5.0, 6.0) };
        TrackedObject trackedObject1 = new TrackedObject("Wall_1", 3, cloudePoint1, "Wall");
        TrackedObject trackedObject2 = new TrackedObject("Wall_2", 3, cloudePoint2, "Wall");
        TrackedObject trackedObject3 = new TrackedObject("ChairBase_1", 3, cloudePoint3, "Chair Base");
        LinkedList<TrackedObject> listTrackedObjects1 = new LinkedList<>();
        listTrackedObjects1.add(trackedObject1);
        listTrackedObjects1.add(trackedObject2);
        listTrackedObjects1.add(trackedObject3);

        assertEquals(listTrackedObjects, listTrackedObjects1);
        assertEquals(listTrackedObjects, liDarWorkerTracker.getHistoyTrackedObjects());

        // Cleanup:
        liDarWorkerTracker.setTimeZeroUnitTest();
        liDarWorkerTracker.getHistoyTrackedObjects().clear();
    }

    /**
     * Add tracked objects to liDarWorkerTracker and verify the changes to the last
     * tracked objects and history.
     *
     * @pre: liderdataBase sulde have stemptedCloudePoints that mach the ones i
     *       created
     *       LastTrackedObjects is empty
     *       HistoyTrackedObjects is empty
     * @post: listTrackedObjects1 contains the corresponding TrackedObjects:
     *        - trackedObject1 with name "Wall_1", timestamp 3, and cloud points
     *        {(1.0, 2.0)}
     *        - trackedObject2 with name "Wall_2", timestamp 3, and cloud points
     *        {(3.0, 4.0)}
     *        - trackedObject3 with name "ChairBase_1", timestamp 3, and cloud
     *        points {(5.0, 6.0)}
     *        liDarWorkerTracker.getLastTrackedObjects() == listTrackedObjects1
     *        liDarWorkerTracker.getHistoyTrackedObjects() == listTrackedObjects1
     *        After calling increaseTime() 3 times and do addTrackedObjects(),
     *        liDarWorkerTracker.getLastTrackedObjects() == listTrackedObjects1
     *        After calling increaseTime() 2 more times and do addTrackedObjects()
     *        again,
     *        liDarWorkerTracker.getLastTrackedObjects() is empty (size == 0).
     */
    @SuppressWarnings("static-access")
    @Test
    public void TaddTrackedObjects() {
        (liDarWorkerTracker.getLiDarDataBase()).setDataForUniTests(stampedCloudPointsList);
        DetectedObject newDetectedObject1 = new DetectedObject("Wall_1", "Wall");
        DetectedObject newDetectedObject2 = new DetectedObject("Wall_2", "Wall");
        DetectedObject newDetectedObject3 = new DetectedObject("ChairBase_1", "Chair Base");
        List<DetectedObject> detectedObjectsList = new LinkedList<>();

        detectedObjectsList.add(newDetectedObject1);
        detectedObjectsList.add(newDetectedObject2);
        detectedObjectsList.add(newDetectedObject3);
        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(3, detectedObjectsList);
        DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent("camera1", stampedDetectedObjects);
        liDarWorkerTracker.transformDetectedObjects(detectObjectsEvent);

        CloudPoint[] cloudePoint1 = { new CloudPoint(1.0, 2.0) };
        CloudPoint[] cloudePoint2 = { new CloudPoint(3.0, 4.0) };
        CloudPoint[] cloudePoint3 = { new CloudPoint(5.0, 6.0) };

        TrackedObject trackedObject1 = new TrackedObject("Wall_1", 3, cloudePoint1, "Wall");
        TrackedObject trackedObject2 = new TrackedObject("Wall_2", 3, cloudePoint2, "Wall");
        TrackedObject trackedObject3 = new TrackedObject("ChairBase_1", 3, cloudePoint3, "Chair Base");
        LinkedList<TrackedObject> listTrackedObjects1 = new LinkedList<>();
        listTrackedObjects1.add(trackedObject1);
        listTrackedObjects1.add(trackedObject2);
        listTrackedObjects1.add(trackedObject3);
        assertEquals(listTrackedObjects1, liDarWorkerTracker.getLastTrackedObjects());
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.addTrackedObjects();
        assertEquals(listTrackedObjects1, liDarWorkerTracker.getLastTrackedObjects());
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.increaseTime();
        liDarWorkerTracker.addTrackedObjects();

        assertEquals(liDarWorkerTracker.getLastTrackedObjects().size(), 0);
        assertEquals(listTrackedObjects1, liDarWorkerTracker.getHistoyTrackedObjects());
        // Cleanup:
        liDarWorkerTracker.getHistoyTrackedObjects().clear();
        liDarWorkerTracker.getHistoyTrackedObjects().clear();
    }
}
