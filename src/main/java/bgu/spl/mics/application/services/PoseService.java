package bgu.spl.mics.application.services;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position
 * and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;
    private final CountDownLatch latch;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */

    public PoseService(GPSIMU gpsimu, CountDownLatch latch) {
        super("pose service");
        this.gpsimu = gpsimu;
        this.latch = latch;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the
     * current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (message) -> {
            gpsimu.increaseTime();

            Pose pose = gpsimu.getPose();
            PoseEvent event = new PoseEvent(pose);

            if (gpsimu.getStatus().equals(STATUS.DOWN)) {
                this.terminate();
            } else {
                if (pose != null) {
                    sendEvent(event);
                }
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast) -> {
            this.terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast) -> {
            this.terminate();
        });

        latch.countDown();
    }
}
