package bgu.spl.mics.application.services;

import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents
 * from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global
     *                   map.
     */

    private final CountDownLatch latch;
    private FusionSlam fusionSlam;

    public FusionSlamService(FusionSlam fusionSlam, CountDownLatch latch) {
        super("fusion slam service");
        this.fusionSlam = fusionSlam;
        this.latch = latch;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and
     * TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (tickBroadcast) -> {
            // try to handle all trackedObjectsEvent that didn't have the wanted pose
            fusionSlam.transformUnhandledEvents();

            // if all services terminated before the last tick we print output and terminate the system
            StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
            if (statisticalFolder.getAmountOfCamerasAlive() == 0
                && statisticalFolder.getAmountOfLidarsAlive() == 0) {
                fusionSlam.writeOutput();
                this.terminate();
            }
        });

        subscribeEvent(TrackedObjectsEvent.class, (trackedObjectsEvent) -> {
            fusionSlam.transformTrackedObjectEvent(trackedObjectsEvent);
            complete(trackedObjectsEvent, true);
        });

        subscribeEvent(PoseEvent.class, (message) -> {
            Pose pose = ((PoseEvent) message).getPose();
            fusionSlam.addPose(pose);

            complete(message, true);
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) -> {
            String error = CrashedBroadcast.getError();
            String senderName = CrashedBroadcast.getName();
            
            fusionSlam.writeCrashedOutput(error, senderName);
            this.terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) -> {
            fusionSlam.writeOutput();
            this.terminate();
        });

        latch.countDown();
    }
}
