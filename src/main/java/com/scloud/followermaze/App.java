package com.scloud.followermaze;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.service.ObservableProvider;
import com.scloud.followermaze.service.ObserverProvider;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class App {
	private static ServerSocket clientServerSocket;
	private static ServerSocket eventServerSocket;

	private static final Logger logger = LogManager.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		logger.info("Waiting for client to connect...");
		String clientPort = System.getenv("clientListenerPort");
		String eventPort = System.getenv("eventListenerPort");

		clientServerSocket = new ServerSocket(clientPort != null ? Integer.parseInt(clientPort) : 9099);
		eventServerSocket = new ServerSocket(eventPort != null ? Integer.parseInt(eventPort) : 9090);
		
		Constants.threadPoolExecutor.execute(() -> {
			while (true)
				try {
					begin(clientServerSocket, eventServerSocket.accept());
				} catch (IOException e) {
					logger.error("ERROR Downstream ", e);
				}
		});
	}

	private static void begin(ServerSocket clientServerSocket, Socket eventSocket) throws IOException {
		try {
			ObservableProvider observers = new ObservableProvider();
			ObserverProvider subscribers = new ObserverProvider();

			// Observables
			Observable<EventData> queueObservable = observers.getQueueObservable();
			Flowable<EventData> eventsObservable = observers.getEventsObservable(eventSocket);

			// Observers
			subscribers.subscribeWithEventProvider(eventsObservable);
			subscribers.watchForClientAndSubscribeWithQueue(clientServerSocket, queueObservable);
		} catch (IOException e) {
			throw e;
		}

	}
}
