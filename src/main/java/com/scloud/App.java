package com.scloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Flowable;

public class App {
	private static ServerSocket clientServerSocket;
	private static ServerSocket eventServerSocket;

	public static void main(String[] args) throws IOException {
		clientServerSocket = new ServerSocket(9099);
		eventServerSocket = new ServerSocket(9090);
		new Thread(() -> {
			while (true)
				try {
					startServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}).start();
	}

	private static void startServer() throws IOException {
		Follower follower = new Follower();
		Socket eventSocket = eventServerSocket.accept();

		Flowable<EventData> events = follower.getEvents(eventSocket);

		follower.getClientConnections(clientServerSocket, events);
	}
}
