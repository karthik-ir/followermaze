/**
 * 
 */
package com.scloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * @author karthik
 *
 */
public class Follower {

	int threadCount = Runtime.getRuntime().availableProcessors();
	ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(100);
	Scheduler scheduler = Schedulers.from(threadPoolExecutor);

	private static PriorityQueue<EventData> sortedEvents = new PriorityQueue<>(new Comparator<EventData>() {

		@Override
		public int compare(EventData o1, EventData o2) {
			return (int) (o1.messageNumber - o1.messageNumber);
		}
	});
	private static int count = 1;
	private static final Object countLock = new Object();

	public void incrementCount() {
		synchronized (countLock) {
			count++;
		}
	}

	private void processInputLine(EventData model) throws InterruptedException {
		// System.out.println("processing " + model.inputLine + " " +
		// Thread.currentThread().getName());
		String[] split = model.inputLine.split("\\|");
		if (split.length >= 2 && EventTypes.fromString(split[1]) != null) {
			EventTypes eventType = EventTypes.fromString(split[1]);
			model.messageNumber = Long.parseLong(split[0]);
			switch (eventType) {
			case BROADCAST:
				model.eventType = EventTypes.BROADCAST;
				break;
			case FOLLOW:
				model.eventType = EventTypes.FOLLOW;
				model.toUserId = split[3];
				model.fromUserId = split[2];
				break;
			case PRIVATE:
				model.eventType = EventTypes.PRIVATE;
				model.toUserId = split[3];
				break;
			case STATUS_UPDATE:
				model.eventType = EventTypes.STATUS_UPDATE;
				// TODO: add all the followers to the userIds list
				model.toUserId = split[2];
				break;
			case UNFOLLOW:
				model.eventType = EventTypes.UNFOLLOW;
				break;
			default:
				System.out.println("SKIPPING");
				break;
			}
		} else {
			System.out.println("SOMETHING WRONG WITH INPUT");
		}
	}

	public Flowable<EventData> getEvents(Socket eventSocket) throws IOException {
		Flowable<EventData> flowable = Flowable.create((source) -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (!source.isCancelled()) {
				String inputLine = in.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					source.onNext(new EventData(inputLine));
					// System.out.println("Created " + inputLine + " at " + " " +
					// Thread.currentThread().getName());
				}
				// emitter.onError(new Throwable("SOMETHING WRONG"));
			}
			source.onComplete();
		}, BackpressureStrategy.BUFFER).map(model -> {
			processInputLine((EventData) model);
			return (EventData) model;
		}).filter(model -> {
			return (model.eventType != null);
		}).map((model) -> {
			sortedEvents.offer(model);
			return model;
		}).observeOn(scheduler).share();
		return flowable;
	}

	public void getClientConnections(ServerSocket clientSocket, Flowable<EventData> events) {
		while (true) {
			try {
				Socket socket = clientSocket.accept();
				// System.out.println("INITIAl" + " " + Thread.currentThread().getName());
				Observable.just(socket).subscribeOn(scheduler).subscribe((x) -> {
					String userId = readValueFromInputStream(socket);
					UserData ud = new UserData(socket);
					ud.userId = userId;
					System.out.println(
							socket.getPort() + " " + userId + " connected" + " " + Thread.currentThread().getName());
					events.observeOn(scheduler).subscribe((event) -> {
						processMessageAtClient(ud, event);
					});
					// observeOn(scheduler).subscribeOn(scheduler)
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processMessageAtClient(UserData ud, EventData event) {

		Socket socket = ud.socket;
		String userId = ud.userId;

		System.out.println(userId + " Received " + event.inputLine + " " + Thread.currentThread().getName());

		
//		while(event.messageNumber != sortedEvents.peek().messageNumber)
			
		if (event.eventType == EventTypes.FOLLOW && event.fromUserId.equals(userId)) {
			ud.follows.add(event.toUserId);
		}

		if (event.eventType != EventTypes.UNFOLLOW 
				&& (event.eventType == EventTypes.BROADCAST
						|| (event.eventType == EventTypes.STATUS_UPDATE && ud.follows.contains(event.fromUserId))
						|| event.toUserId.equals(userId))) {
			PrintWriter out;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				sortedEvents.poll();
				incrementCount();
				out.println(event.inputLine);
				System.out.println("Sending " + event.inputLine);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	String readValueFromInputStream(Socket x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x.getInputStream()));
		String inputLine = in.readLine();
		return inputLine;
	}
}
