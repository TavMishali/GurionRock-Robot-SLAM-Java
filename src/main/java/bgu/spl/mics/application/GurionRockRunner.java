package bgu.spl.mics.application;

import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CamerasConfiguration;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.application.objects.Configuration;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.JsonParser;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.LidarConfiguration;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the
     *             path to the configuration file.
     */
    public static void main(String[] args) {
        JsonParser parser = new JsonParser();
        Configuration configFile = parser
                .getConfiguration(args[0]);
        // amount is 2 because pose + fusion will always have single instance
        int amountOfServices = 2;

        amountOfServices += configFile.getCameras().getCamerasConfigurations().size();
        amountOfServices += configFile.getLidarWorkers().getLidarConfigurations().size();
        CountDownLatch latch = new CountDownLatch(amountOfServices);

        for (CamerasConfiguration cameraConfig : configFile.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(cameraConfig.getId(), cameraConfig.getFrequency(),
                    configFile.getCameras().getCamera_datas_path());
            CameraService cameraService = new CameraService(camera, latch);
            Thread cameraThread = new Thread(cameraService);
            cameraThread.start();
        }

        for (LidarConfiguration lidarConfig : configFile.getLidarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker newLidarWorker = new LiDarWorkerTracker("LiDarWorkerTracker" + lidarConfig.getId(),
                    lidarConfig.getFrequency(), configFile.getLidarWorkers().getLidarsDataPath());
            LiDarService newLiDarService = new LiDarService(newLidarWorker, latch);
            Thread lidarThread = new Thread(newLiDarService);
            lidarThread.start();
        }

        GPSIMU gpsimu = new GPSIMU(configFile.getPoseJsonFile());
        PoseService poseService = new PoseService(gpsimu, latch);
        Thread poseThread = new Thread(poseService);
        poseThread.start();

        FusionSlam fusionSlam = FusionSlam.getInstance();
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam, latch);
        Thread fusionThread = new Thread(fusionSlamService);
        fusionThread.start();

        TimeService timeService = new TimeService(configFile.getTickTime(), configFile.getDuration());
        Thread timeThread = new Thread(timeService);

        try {
            // Wait for all MicroServices to finish initialization
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Running time service only after all services finished initialize
        timeThread.start();
    }
}
