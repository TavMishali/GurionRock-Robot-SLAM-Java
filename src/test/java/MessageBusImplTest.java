import org.junit.jupiter.api.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBusImplTest {
    FusionSlam fusionSlam = FusionSlam.getInstance();
    MessageBus messageBus = MessageBusImpl.getInstance();
    Camera camera = new Camera(1, 2, "./camera_data.json");
    Camera camera2 = new Camera(2, 3, "./camera_data.json");
    MicroService cameraService = new CameraService(camera, null);
    CameraService cameraService2 = new CameraService(camera2, null);
    LiDarWorkerTracker liDarWorkerTracker = new LiDarWorkerTracker("2", 2, "./lidar_data.json");
    LiDarService liDarService = new LiDarService(liDarWorkerTracker, null);

    // Register Tests:
    /**
     * Registers a cameraService to the messageBus and verifies the size of its
     * queue.
     *
     * @pre: cameraService is an initialized microservice.
     *       messageBus.getMicroServicesQueueMap().get(cameraService) returns a
     *       valid queue.
     * @post: messageBus.getMicroServicesQueueMap().get(cameraService).size() == 0
     *        cameraService is successfully registered to messageBus.
     */

    @Test
    public void CreateMicroServiceQueue() {
        messageBus.register(cameraService);

        assertEquals(0, messageBus.getMicroServicesQueueMap().get(cameraService).size());

        // Cleanup:
        messageBus.unregister(cameraService);
    }

    @Test
    public void CreateMicroServiceQueueMultipleServices() {
        messageBus.register(cameraService);
        messageBus.register(cameraService2);

        assertEquals(0, messageBus.getMicroServicesQueueMap().get(cameraService).size());
        assertEquals(0, messageBus.getMicroServicesQueueMap().get(cameraService2).size());

        // Cleanup:
        messageBus.unregister(cameraService);
        messageBus.unregister(cameraService2);
    }

    /**
     * Subscribes cameraService to DetectObjectsEvent and verifies it can receive
     * the event.
     *
     * @pre: cameraService is an initialized microservice.
     *       messageBus.getMessagesToMicroServicesMap() contains a map that can
     *       store events.
     * @post: cameraService is subscribed to DetectObjectsEvent.
     */

    // Subscribe Event Tests:
    @Test
    public void CreateMessageQueue() {
        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);

        try {
            assertEquals(cameraService,
                    messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).take());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    @Test
    public void AddMicroServiceToMessageWhenKeyExistsInMap() {
        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);
        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService2);

        try {
            assertEquals(2, messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).size());
            assertEquals(cameraService,
                    messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).take());
            assertEquals(cameraService2,
                    messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).take());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    // Subscribe Broadcast Tests:
    @Test
    public void CreateBroadcastQueue() {
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);

        try {
            assertEquals(cameraService, messageBus.getMessagesToMicroServicesMap().get(TickBroadcast.class).take());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    @Test
    public void AddMicroServiceToBroadcastWhenKeyExistsInMap() {
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService2);

        try {
            assertEquals(2, messageBus.getMessagesToMicroServicesMap().get(TickBroadcast.class).size());
            assertEquals(cameraService, messageBus.getMessagesToMicroServicesMap().get(TickBroadcast.class).take());
            assertEquals(cameraService2, messageBus.getMessagesToMicroServicesMap().get(TickBroadcast.class).take());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    // Send Broadcast Tests:
    @Test
    public void SendBroadcastToAllSubscribedServices() {
        messageBus.register(cameraService);
        messageBus.register(cameraService2);
        messageBus.register(liDarService);

        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService2);

        TickBroadcast tickBroadcast = new TickBroadcast(1);
        messageBus.sendBroadcast(tickBroadcast);

        try {
            assertEquals(tickBroadcast, messageBus.getMicroServicesQueueMap().get(cameraService).take());
            assertEquals(tickBroadcast, messageBus.getMicroServicesQueueMap().get(cameraService2).take());
            assertEquals(0, messageBus.getMicroServicesQueueMap().get(liDarService).size());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    @Test
    public void SendMultipleBroadcastToSubscribedMicroService() {
        messageBus.register(cameraService);
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);
        messageBus.subscribeBroadcast(TerminatedBroadcast.class, cameraService);

        TickBroadcast tickBroadcast = new TickBroadcast(1);
        TerminatedBroadcast terminatedBroadcast = new TerminatedBroadcast(null);
        messageBus.sendBroadcast(tickBroadcast);
        messageBus.sendBroadcast(terminatedBroadcast);

        try {
            assertEquals(2, messageBus.getMicroServicesQueueMap().get(cameraService).size());
            assertEquals(tickBroadcast, messageBus.getMicroServicesQueueMap().get(cameraService).take());
            assertEquals(terminatedBroadcast, messageBus.getMicroServicesQueueMap().get(cameraService).take());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    // Send Event Tests:
    @Test
    public void SendEventToFirstMicroServiceInQueue() {
        List<DetectedObject> detectedObjectsList = new LinkedList<>();
        DetectedObject newDetectedObject1 = new DetectedObject("Chair_Base_1", "ChairBase");
        detectedObjectsList.add(newDetectedObject1);
        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(3,
                detectedObjectsList);
        DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent("camera1",
                stampedDetectedObjects);

        messageBus.register(cameraService);
        messageBus.register(cameraService2);

        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);
        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService2);

        Future<Boolean> future = messageBus.sendEvent(detectObjectsEvent);

        try {
            // checking to see if only the first cameraService got the event
            assertEquals(detectObjectsEvent, messageBus.getMicroServicesQueueMap().get(cameraService).take());
            assertEquals(0, messageBus.getMicroServicesQueueMap().get(cameraService2).size());

            // checking to see if round robin worked and cameraService2 is first and if it
            // reinserted cameraService
            assertEquals(cameraService2,
                    messageBus.getMessagesToMicroServicesMap().get(detectObjectsEvent.getClass()).take());
            assertEquals(cameraService,
                    messageBus.getMessagesToMicroServicesMap().get(detectObjectsEvent.getClass()).take());

            // checking if the event was added to the event to future map
            assertEquals(future, messageBus.getEventsToFutureMap().get(detectObjectsEvent));
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    /**
     * complete should resolve the future and remove him from the messagesToFutures
     * map
     *
     * @pre: microService is subscribed to a message
     * @post: getEventsToFutureMap will not have the future and future.isDone() is
     *        true
     */
    @Test
    public void CompleteTest() {
        List<DetectedObject> detectedObjectsList = new LinkedList<>();
        DetectedObject newDetectedObject1 = new DetectedObject("Chair_Base_1", "ChairBase");
        detectedObjectsList.add(newDetectedObject1);
        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects(3,
                detectedObjectsList);
        DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent("camera1",
                stampedDetectedObjects);

        messageBus.register(cameraService);
        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);
        Future<Boolean> future = messageBus.sendEvent(detectObjectsEvent);
        // checking if the event was added to the event to future map
        assertEquals(future, messageBus.getEventsToFutureMap().get(detectObjectsEvent));

        try {
            messageBus.complete(detectObjectsEvent, true);
            assertNull(messageBus.getEventsToFutureMap().get(detectObjectsEvent));

            assertTrue(future.isDone());
            assertTrue(future.get());
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    /**
     * unregister should remove the micro service queue from the
     * microServicesQueueMap and remove itself from each message that he is
     * subscribed to in the map-
     * MessagesToMicroServicesMap
     *
     * @pre: cameraService is subscribed to DetectObjectsEvent and
     *       TerminatedBroadcast
     * @post: getMessagesToMicroServicesMap in keys
     *        DetectObjectsEvent,TerminatedBroadcast will have queue with size 0 (no
     *        service will be subscribed to them)
     */
    @Test
    public void UnregisterTest() {
        cameraService.getMessages().add(DetectObjectsEvent.class);
        cameraService.getMessages().add(TerminatedBroadcast.class);
        messageBus.getLockMap().putIfAbsent(DetectObjectsEvent.class, new ReentrantLock());
        messageBus.getLockMap().putIfAbsent(TerminatedBroadcast.class, new ReentrantLock());

        messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);
        messageBus.subscribeBroadcast(TerminatedBroadcast.class, cameraService);

        assertEquals(1, messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).size());
        assertEquals(1, messageBus.getMessagesToMicroServicesMap().get(TerminatedBroadcast.class).size());

        messageBus.unregister(cameraService);

        assertEquals(0, messageBus.getMessagesToMicroServicesMap().get(DetectObjectsEvent.class).size());
        assertEquals(0, messageBus.getMessagesToMicroServicesMap().get(TerminatedBroadcast.class).size());
        assertNull(messageBus.getMicroServicesQueueMap().get(cameraService));

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }

    // Await Message Tests:
    @Test
    public void AwaitMessage() {
        messageBus.register(cameraService);
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);

        TickBroadcast tickBroadcast = new TickBroadcast(1);
        messageBus.sendBroadcast(tickBroadcast);

        try {
            Message message = messageBus.awaitMessage(cameraService);
            assertEquals(tickBroadcast, message);
        } catch (InterruptedException e) {

        }

        // Cleanup:
        messageBus.getMicroServicesQueueMap().clear();
        messageBus.getMessagesToMicroServicesMap().clear();
    }
}