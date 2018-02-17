package com.scloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class App {
	private static ServerSocket clientServerSocket;
	private static ServerSocket eventServerSocket;

	public static void main(String[] args) throws IOException {
		clientServerSocket = new ServerSocket(9099);
		eventServerSocket = new ServerSocket(9090);
		new Thread(() -> {
			while (true)
				try {
					begin(clientServerSocket, eventServerSocket.accept());
				} catch (IOException e) {
					e.printStackTrace();
				}
		}).start();
	}
	
	private static void begin(ServerSocket clientServerSocket , Socket eventSocket) throws IOException {
		ObservableProvider provider = new ObservableProvider();
		ObserverProvider follower = new ObserverProvider();
		//Observables
		Observable<EventData> queueObservable = provider.getQueueObservable();
		Flowable<EventData> eventsObservable = provider.getEventsObservable(eventSocket);
		
		
		//Observers
		follower.subscribeWithEventProvider(eventsObservable);
		follower.watchForClientAndSubscribeWithQueue(clientServerSocket, queueObservable );

	}
}
