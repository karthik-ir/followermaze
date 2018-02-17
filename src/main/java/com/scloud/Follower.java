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
	ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1000);
	Scheduler scheduler = Schedulers.from(threadPoolExecutor);

	private static Long count = 1L;
	private static final Object countLock = new Object();
	private static final Object queueLock = new Object();

	public void incrementCount() {
		synchronized (countLock) {
			count++;
		}
	}

	public EventData poll() {
		synchronized (queueLock) {
			return sortedEvents.poll();
		}
	}

	public void offer(EventData data) {
		synchronized (queueLock) {
			sortedEvents.offer(data);
		}
	}

	public EventData peek() {
		synchronized (queueLock) {
			return sortedEvents.peek();
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
				model.fromUserId = split[2];
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

	static PriorityQueue<EventData> sortedEvents = new PriorityQueue<>(new Comparator<EventData>() {

		@Override
		public int compare(EventData o1, EventData o2) {
			if (o1 == null || o2 == null)
				System.out.println("PRINT ME");
			return (int) (o1.messageNumber - o2.messageNumber);
		}
	});

	public Observable events(Socket eventSocket) throws IOException {
		Observable loopObservable = Observable.create((x) -> {
			// System.out.println("Looking for queue on " +
			// Thread.currentThread().getName());
			while (true) {
				if (!sortedEvents.isEmpty() && peek() != null
						&& Long.toString(peek().messageNumber).equals(Long.toString(count))) {
					x.onNext(poll());
					incrementCount();
				}
			}
		}).observeOn(scheduler).share();

		getEvents(eventSocket);

		return loopObservable;
	}

	public void getEvents(Socket eventSocket) throws IOException {
		Flowable.create((source) -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (!source.isCancelled()) {
				String inputLine = in.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value = new EventData(inputLine);
					source.onNext(value);
					// System.out.println("Created " + inputLine + " at " + " " +
					// Thread.currentThread().getName());
				}
			}
			source.onComplete();
		}, BackpressureStrategy.BUFFER).observeOn(scheduler).subscribeOn(scheduler).map(model -> {
			processInputLine((EventData) model);
			return (EventData) model;
		}).subscribe((x) -> {
			// System.out.println("EVENT SUBSCRIBE " + x.messageNumber + " at " + " " +
			// Thread.currentThread().getName());
			offer(x);
		});
	}

	public void getClientConnections(ServerSocket clientSocket, Observable<EventData> events) {
		while (true) {
			try {
				Socket socket = clientSocket.accept();
				// System.out.println("INITIAl" + " " + Thread.currentThread().getName());
				Observable.just(socket).observeOn(scheduler).subscribe((x) -> {
					String userId = readValueFromInputStream(socket);
					UserData ud = new UserData(socket);
					ud.userId = userId;
					System.out.println(
							socket.getPort() + " " + userId + " connected" + " " + Thread.currentThread().getName());
					events.observeOn(scheduler).subscribeOn(scheduler).subscribe((event) -> {
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

//		System.out.println(userId + " Received " + event.inputLine + " " + Thread.currentThread().getName());

		if (event.eventType == EventTypes.UNFOLLOW)
			return;
		if (event.eventType == EventTypes.BROADCAST) {
			send(event, socket);
			return;
		}
		if (event.eventType == EventTypes.STATUS_UPDATE) {
//			if(Long.toString(event.messageNumber).equals("129"))
//				System.out.println(" POPOP "+event.fromUserId);
			if (ud.follows.contains(event.fromUserId)) {
				send(event, socket);
			}
			return;
		}
		if (event.eventType == EventTypes.FOLLOW && event.fromUserId.equals(userId)) {
			ud.follows.add(event.toUserId);
			 System.out.println(event.fromUserId + " is following " + ud.follows);
		}

		if (event.toUserId.equals(userId)) {
			send(event, socket);
			return;
		}

	}

	private void send(EventData event, Socket socket) {
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(event.inputLine);
			System.out.println("Sending " + event.inputLine + " " + Thread.currentThread().getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	String readValueFromInputStream(Socket x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x.getInputStream()));
		String inputLine = in.readLine();
		return inputLine;
	}
}
