package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting
 * TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int TickTime;
    private int Duration;
    private int currentTick;
    private StatisticalFolder statisticalFolder;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("Time Service");
        this.TickTime = TickTime * 1000;
        this.Duration = Duration;
        this.currentTick = 0;
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified
     * duration.
     */
    @Override
    protected void initialize() {
        while (Duration > 0) {
            if (statisticalFolder.getAmountOfCamerasAlive() == 0
                    && statisticalFolder.getAmountOfLidarsAlive() == 0) {
                this.terminate();
                break;
            } else {
                currentTick++;
                sendBroadcast(new TickBroadcast(currentTick));
                statisticalFolder.setSystemRunTime(currentTick);

                try {
                    Thread.sleep(TickTime);
                } catch (InterruptedException e) {

                }

                Duration--;
            }
        }

        sendBroadcast(new TerminatedBroadcast(getName()));
        this.terminate();
    }
}
