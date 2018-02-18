package com.scloud.followermaze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.model.UserData;

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

		// RUN the server
		try {
			begin(clientServerSocket, eventServerSocket.accept());
		} catch (IOException e) {
			logger.error("ERROR Downstream ", e);
		}
	}

	private static void begin(ServerSocket clientServerSocket, Socket eventSocket) throws IOException {

		Observable subject = new Observable();

		Constants.threadPoolExecutor.execute(() -> {
			readInputstreamAndEnqueue(eventSocket);
		});

		Constants.threadPoolExecutor.execute(() -> {
			eventProducer(subject);
		});

		Constants.threadPoolExecutor.execute(() -> {
			waitForClientsAndSubscribe(clientServerSocket, subject);
		});

	}

	private static void waitForClientsAndSubscribe(ServerSocket clientServerSocket, Observable subject) {
		while (true) {
			try {
				Socket socket = clientServerSocket.accept();
				Constants.threadPoolExecutor.execute(() -> {
					String userId;
					try {
						userId = new Helper().readValueFromInputStream(socket);
						UserData ud = new UserData(socket);
						ud.setUserId(userId);
						logger.info("Client {} Connected on port {} ", userId, socket.getPort());
						new EventObserver(subject, ud);
					} catch (IOException e) {
						logger.error("Error while reading data from stream ", e);
						throw new RuntimeException(e);
					}
				});
			} catch (IOException e) {
				logger.error("Error while waiting for clients ", e);
				throw new RuntimeException(e);
			}
		}
	}

	private static void eventProducer(Observable subject) {
		while (true) {
			EventData peek = Constants.peek();
			if (peek != null
					&& Long.toString(peek.getMessageNumber()).equals(Long.toString(Constants.messageSendNumber))) {
				EventData latestEvent = Constants.poll();
				subject.setData(latestEvent);
				Constants.incrementCount();
			}
		}

	}

	private static void readInputstreamAndEnqueue(Socket eventSocket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (true) {
				String inputLine = in.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value = new EventData(inputLine);
					new Helper().processInputLine(value);
					Constants.offer(value);
				}
			}
		} catch (IOException e) {
			logger.error("Error while reading Event Inputstream ", e);
		}
	}
}
