package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.MicroService;
import java.util.concurrent.CountDownLatch;
import java.util.LinkedList;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker liDarWorkerTracker;
    private final CountDownLatch latch;

    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to
     *                     process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) {
        super("LiDar" + LiDarWorkerTracker.getID());
        this.liDarWorkerTracker = LiDarWorkerTracker;
        this.latch = latch;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */

    @Override
    protected void initialize() {
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent) -> {
            LinkedList<TrackedObject> toSendTrackedObjects = liDarWorkerTracker
                    .transformDetectedObjects(DetectObjectsEvent);
            
            if (liDarWorkerTracker.getStatus() == STATUS.ERROR) {
                sendBroadcast(new CrashedBroadcast(getName(),
                        "LidarWorkerTracker" + liDarWorkerTracker.getID() + " Crashed"));
                this.terminate();
            } else {
                if (!toSendTrackedObjects.isEmpty()) {
                    sendEvent(new TrackedObjectsEvent(toSendTrackedObjects));
                }
            }

            complete(DetectObjectsEvent, true);
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) -> {
            LinkedList<TrackedObject> toSendTrackedObjects = liDarWorkerTracker.addTrackedObjects();
            liDarWorkerTracker.increaseTime();
            if (!toSendTrackedObjects.isEmpty()) {
                sendEvent(new TrackedObjectsEvent(toSendTrackedObjects));
            }

            if (liDarWorkerTracker.getStatus().equals(STATUS.DOWN)) {
                terminate();
            }
        });
        
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) -> {
            statisticalFolder.changeAmountOfLidarsAlive(-1);
            this.terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) -> {
            statisticalFolder.changeAmountOfLidarsAlive(-1);
            this.terminate();
        });

        statisticalFolder.changeAmountOfLidarsAlive(1);

        latch.countDown();
    }
}
