/**
 * 
 */
package com.scloud.follower.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.follower.Constants;
import com.scloud.follower.model.EventData;
import com.scloud.follower.model.UserData;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * @author karthik
 *
 */
public class ObserverProvider {

	private static final Logger logger = LogManager.getLogger(ObserverProvider.class);

	public void subscribeWithEventProvider(Flowable<EventData> eventsObservable) {
		eventsObservable.subscribe((x) -> {
			Constants.offer(x);
		});
	}

	public void watchForClientAndSubscribeWithQueue(ServerSocket clientSocket, Observable<EventData> events)
			throws IOException {
		logger.info("Registering connected clients");
		while (true) {
			try {
				Socket socket = clientSocket.accept();
				Observable.just(socket).observeOn(Constants.scheduler).subscribe((x) -> {
					String userId = new Helper().readValueFromInputStream(socket);
					UserData ud = new UserData(socket);
					ud.setUserId(userId);
					logger.info("Client {} Connected on port {} ", userId, socket.getPort());
					events.observeOn(Constants.scheduler).subscribe((event) -> {
						followEventObserver(ud, event);
					});
				});
			} catch (IOException e) {
				logger.error("Error while waiting for clients ", e);
				throw e;
			}
		}
	}

	private void followEventObserver(UserData ud, EventData event) {
		logger.debug("{} Received {} ", ud.getUserId(), event.getInputLine());
		new Helper().checkIfEventValidAndNotify(ud, event);
	}
}
