package bgu.spl.mics;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	public ConcurrentHashMap<Class<? extends Message>, BlockingQueue<MicroService>> messagesToMicroServices = new ConcurrentHashMap<>();
	public ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServicesQueues = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Event<?>, Future<?>> eventsToFuture = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Message>, ReentrantLock> lockMap = new ConcurrentHashMap<>();

	// Getter's for unit testing:
	public ConcurrentHashMap<Class<? extends Message>, BlockingQueue<MicroService>> getMessagesToMicroServicesMap() {
		return this.messagesToMicroServices;
	}

	public ConcurrentHashMap<MicroService, BlockingQueue<Message>> getMicroServicesQueueMap() {
		return this.microServicesQueues;
	}

	public ConcurrentHashMap<Event<?>, Future<?>> getEventsToFutureMap() {
		return this.eventsToFuture;
	}

	public ConcurrentHashMap<Class<? extends Message>, ReentrantLock> getLockMap() {
		return this.lockMap;
	}

	private static class SingletonMessageBus {
		private static final MessageBus INSTANCE = new MessageBusImpl();
	}

	// Public method to get the singleton instance
	public static MessageBus getInstance() {
		return SingletonMessageBus.INSTANCE;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		messagesToMicroServices.computeIfAbsent(type, key -> new LinkedBlockingQueue<>())
				.add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		messagesToMicroServices.computeIfAbsent(type, key -> new LinkedBlockingQueue<>())
				.add(m);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<?> future = eventsToFuture.get(e);
		((Future<T>) future).resolve(result);
		eventsToFuture.remove(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		BlockingQueue<MicroService> queue = messagesToMicroServices.get(b.getClass());
		Class<? extends Message> messageType = b.getClass();
		lockMap.putIfAbsent(messageType, new ReentrantLock());

		for (MicroService m : queue) {
			BlockingQueue<Message> queueM = microServicesQueues.get(m);
			if(queueM != null) {
				queueM.add(b);
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = new Future<>();
		eventsToFuture.put(e, future);

		Class<? extends Message> messageType = e.getClass();
		lockMap.putIfAbsent(messageType, new ReentrantLock());
		ReentrantLock lock = lockMap.get(messageType);

		lock.lock();
		try {
			Queue<MicroService> eventQueue = messagesToMicroServices.get(messageType);
			if (eventQueue == null || eventQueue.isEmpty()) {
				return null;
			}

			MicroService currService = eventQueue.poll();
			if (currService != null && microServicesQueues.containsKey(currService)) {
				microServicesQueues.get(currService).add(e);
				eventQueue.add(currService);
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}

		return future;
	}

	@Override
	public void register(MicroService m) {
		BlockingQueue<Message> events = new LinkedBlockingQueue<>();
		microServicesQueues.put(m, events);
	}

	@Override
	public void unregister(MicroService m) {
		List<Class<?>> messages = m.getMessages();

		for (Class<?> message : messages) {
			ReentrantLock lock = lockMap.get(message);
			if (lock != null) {
				lock.lock();
				try {
					BlockingQueue<MicroService> queue = messagesToMicroServices.get(message);
					if (queue != null) {
						if (queue.isEmpty()) {
							lockMap.remove(message); 
						}
						else{
							queue.remove(m);
						}
					}
				} finally {
					lock.unlock();
				}
			}
		}

		microServicesQueues.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message message = null;

		try {
			message = microServicesQueues.get(m).take();
			return message;
		} catch (InterruptedException e) {
			throw e;
		}
	}
}