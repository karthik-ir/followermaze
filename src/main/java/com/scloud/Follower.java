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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * @author karthik
 *
 */
public class Follower {

	private void processInputLine(EventData model) throws InterruptedException {
		String[] split = model.inputLine.split("\\|");
		if (split.length >= 2) {
			EventTypes eventType = EventTypes.fromString(split[1]);
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

	public Observable<EventData> getEvents(Socket eventSocket) throws IOException {
		Observable<EventData> create = Observable.create(new ObservableOnSubscribe<EventData>() {

			@Override
			public void subscribe(ObservableEmitter<EventData> emitter) throws Exception {
				while (true) {
					BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
					String inputLine = in.readLine();
					if (inputLine != null && !inputLine.isEmpty()) {
						emitter.onNext(new EventData(inputLine));
					}
					// emitter.onError(new Throwable("SOMETHING WRONG"));
				}
			}
		}).map(model -> {
			processInputLine(model);
			return model;
		});
		return create;
	}

	public void getClientConnections(ServerSocket clientSocket, Observable<EventData> events) {
		Observable.just(clientSocket).subscribeOn(Schedulers.newThread()).subscribe((cs) -> {
			while (true) {
				try {
					Socket socket = clientSocket.accept();
					Observable.just(socket).subscribeOn(Schedulers.newThread()).subscribe((x) -> {
						String userId = readValueFromInputStream(socket);
						UserData ud = new UserData(socket);
						ud.userId = userId;
						events.subscribeOn(Schedulers.newThread()).subscribe((event) -> {
							processMessageAtClient(ud, event);
						});
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void processMessageAtClient(UserData ud, EventData event) {

		Socket socket = ud.socket;
		String userId = ud.userId;

		System.out.println(userId + " Received " + event.inputLine);

		if (event.eventType == EventTypes.FOLLOW && event.fromUserId.equals(userId)) {
			ud.follows.add(event.toUserId);
		}
		if (event.eventType != EventTypes.UNFOLLOW && (event.eventType == EventTypes.BROADCAST
				|| (event.eventType == EventTypes.STATUS_UPDATE && ud.follows.contains(event.fromUserId))
				|| event.toUserId.equals(userId))) {
			PrintWriter out;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
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
