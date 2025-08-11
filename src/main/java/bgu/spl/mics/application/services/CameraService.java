package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import java.util.concurrent.CountDownLatch;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    /**
     * Constructor for CameraService.
     *
     * 
     * @param camera The Camera object that this service will use to detect objects.
     */

    private final CountDownLatch latch;
    private Camera camera;

    public CameraService(Camera camera, CountDownLatch latch) {
        super("camera" + camera.getId());
        this.camera = camera;
        this.latch = latch;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for
     * sending
     * DetectObjectsEvents.
     */

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (tickBroadcast) -> {
            camera.increaseTime();
            StampedDetectedObjects stampedDetectedObjects = camera.transformDetectedObject();

            if (camera.getStatus() == STATUS.ERROR) {
                sendBroadcast(new CrashedBroadcast(getName(), "Camera Crashed"));
                terminate();
            }

            // find the detected objects that reached time T+Frequency

            if (stampedDetectedObjects != null) {
                DetectObjectsEvent event = new DetectObjectsEvent(this.getName(), stampedDetectedObjects);
                sendEvent(event);
            }

            if (camera.getStatus().equals(STATUS.DOWN)) {
                this.terminate();
            }
        });
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) -> {
            statisticalFolder.changeAmountOfCamerasAlive(-1);
            this.terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) -> {
            statisticalFolder.changeAmountOfCamerasAlive(-1);
            this.terminate();
        });

        statisticalFolder.changeAmountOfCamerasAlive(1);

        latch.countDown();
    }
}
