/**
 * 
 */
package com.scloud;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * @author karthik
 *
 */
public class Constants {

	static int threadCount = Runtime.getRuntime().availableProcessors();
	static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);
	static Scheduler scheduler = Schedulers.from(threadPoolExecutor);
	

	static Long count = 1L;
	private static final Object countLock = new Object();
	private static final Object queueLock = new Object();
	

	public static void incrementCount() {
		synchronized (countLock) {
			count++;
		}
	}

	public static EventData poll() {
		synchronized (queueLock) {
			return sortedEvents.poll();
		}
	}

	public static void offer(EventData data) {
		synchronized (queueLock) {
			sortedEvents.offer(data);
		}
	}

	public static EventData peek() {
		synchronized (queueLock) {
			return sortedEvents.peek();
		}
	}

	static PriorityQueue<EventData> sortedEvents = new PriorityQueue<>(new Comparator<EventData>() {

		@Override
		public int compare(EventData o1, EventData o2) {
			return (int) (o1.messageNumber - o2.messageNumber);
		}
	});
}
