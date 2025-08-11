package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;
    StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

    public GPSIMU(String filePath) {
        JsonParser parser = new JsonParser();
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = parser.getPoseData(filePath);
    }

    public Pose getPose() {
        if (currentTick > 0 && statisticalFolder.getAmountOfCamerasAlive() == 0
                && statisticalFolder.getAmountOfLidarsAlive() == 0) {
            this.status = STATUS.DOWN;
            return null;
        }

        if(currentTick > poseList.size()) {
            this.status = STATUS.DOWN;
            return null;
        }

        return poseList.get(currentTick - 1);
    }

    public void increaseTime() {
        this.currentTick++;
    }

    public STATUS getStatus() {
        return this.status;
    }
}
