/**
 * 
 */
package com.scloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * @author karthik
 *
 */
public class ObserverProvider {

	public void subscribeWithEventProvider(Flowable<EventData> eventsObservable) {
		eventsObservable.subscribe((x) -> {
			// System.out.println("EVENT SUBSCRIBE " + x.messageNumber + " at " + " " +
			// Thread.currentThread().getName());
			Constants.offer(x);
		});
	}

	public void watchForClientAndSubscribeWithQueue(ServerSocket clientSocket, Observable<EventData> events) {
		while (true) {
			try {
				Socket socket = clientSocket.accept();
				// System.out.println("INITIAl" + " " + Thread.currentThread().getName());
				Observable.just(socket).observeOn(Constants.scheduler).subscribe((x) -> {
					String userId = new Helper().readValueFromInputStream(socket);
					UserData ud = new UserData(socket);
					ud.userId = userId;
					System.out.println(
							socket.getPort() + " " + userId + " connected" + " " + Thread.currentThread().getName());
					events.observeOn(Constants.scheduler).subscribe((event) -> {
						followEventObserver(ud, event);
					});
					// observeOn(scheduler).subscribeOn(scheduler)
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void followEventObserver(UserData ud, EventData event) {
		Helper helper = new Helper();
		Socket socket = ud.socket;
		String userId = ud.userId;

		System.out.println(userId + " Received " + event.inputLine + " " + Thread.currentThread().getName());

		new Helper().checkIfEventValidAndProceed(ud, event, helper, socket, userId);

	}
}
