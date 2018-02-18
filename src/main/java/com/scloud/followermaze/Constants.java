/**
 * 
 */
package com.scloud.followermaze;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.scloud.followermaze.model.EventData;

/**
 * @author karthik
 *
 */
public class Constants {

	private static int threadCount = Runtime.getRuntime().availableProcessors();
	public static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);

	private static Long messageSendNumber = 1L;
	private static final Object countLock = new Object();
	private static final Object queueLock = new Object();
	private static final Object serverLock = new Object();
	private static Boolean complete = false;

	public static Boolean isComplete() {
		synchronized (serverLock) {
			return complete;
		}
	}

	public static void setComplete(Boolean value) {
		synchronized (serverLock) {
			complete = value;
		}
	}

	public static Long getMessageCounter() {
		synchronized (countLock) {
			return messageSendNumber;
		}
		
	}
	public static void incrementCount() {
		synchronized (countLock) {
			messageSendNumber++;
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

	public static boolean isEmpty() {
		synchronized (queueLock) {
			return sortedEvents.isEmpty();
		}
	}

	public static EventData peek() {
		synchronized (queueLock) {
			return sortedEvents.peek();
		}
	}

	private static PriorityQueue<EventData> sortedEvents = new PriorityQueue<>(new Comparator<EventData>() {
		@Override
		public int compare(EventData o1, EventData o2) {
			return (int) (o1.getMessageNumber() - o2.getMessageNumber());
		}
	});
}
