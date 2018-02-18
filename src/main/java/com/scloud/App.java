package com.scloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class App {
	private static ServerSocket clientServerSocket;
	private static ServerSocket eventServerSocket;

	private static final Logger logger = LogManager.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		logger.info("Waiting for client to connect...");
		clientServerSocket = new ServerSocket(9099);
		eventServerSocket = new ServerSocket(9090);
		new Thread(() -> {
			while (true)
				try {
					begin(clientServerSocket, eventServerSocket.accept());
				} catch (IOException e) {
					logger.error("ERROR Downstream ", e);
				}
		}).start();
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
