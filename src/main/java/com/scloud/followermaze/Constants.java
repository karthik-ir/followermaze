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

	private  int threadCount = Runtime.getRuntime().availableProcessors();
	public  ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);

	private  Long messageSendNumber = 1L;
	private  final Object countLock = new Object();
	private  final Object queueLock = new Object();
	private  final Object serverLock = new Object();
	private  Boolean complete = false;

	public  Boolean isComplete() {
		synchronized (serverLock) {
			return complete;
		}
	}

	public  void setComplete(Boolean value) {
		synchronized (serverLock) {
			complete = value;
		}
	}

	public  Long getMessageCounter() {
		synchronized (countLock) {
			return messageSendNumber;
		}
		
	}
	public  void incrementCount() {
		synchronized (countLock) {
			messageSendNumber++;
		}
	}

	public  EventData poll() {
		synchronized (queueLock) {
			return sortedEvents.poll();
		}
	}

	public  void offer(EventData data) {
		synchronized (queueLock) {
			sortedEvents.offer(data);
		}
	}

	public  boolean isEmpty() {
		synchronized (queueLock) {
			return sortedEvents.isEmpty();
		}
	}

	public  EventData peek() {
		synchronized (queueLock) {
			return sortedEvents.peek();
		}
	}

	private  PriorityQueue<EventData> sortedEvents = new PriorityQueue<>(new Comparator<EventData>() {
		@Override
		public int compare(EventData o1, EventData o2) {
			return (int) (o1.getMessageNumber() - o2.getMessageNumber());
		}
	});
}
