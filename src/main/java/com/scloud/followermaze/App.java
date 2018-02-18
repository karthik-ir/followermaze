package com.scloud.followermaze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.exception.BadInputException;
import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.model.UserData;

public class App {
	private static ServerSocket clientServerSocket;
	private static ServerSocket eventServerSocket;

	private static final Logger logger = LogManager.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		logger.info("Starting....");
		String clientPort = System.getenv("clientListenerPort");
		String eventPort = System.getenv("eventListenerPort");

		clientServerSocket = new ServerSocket(clientPort != null ? Integer.parseInt(clientPort) : 9099);
		eventServerSocket = new ServerSocket(eventPort != null ? Integer.parseInt(eventPort) : 9090);

		// RUN the server
		try {
			begin(clientServerSocket, eventServerSocket.accept());
		} catch (IOException | InterruptedException e) {
			logger.error("ERROR Downstream ", e);
		}
	}

	private static void begin(ServerSocket clientServerSocket, Socket eventSocket)
			throws IOException, InterruptedException {

		Observable subject = new Observable();

		Constants.threadPoolExecutor.submit(() -> {
			readInputstreamAndEnqueue(eventSocket);
		});

		Constants.threadPoolExecutor.execute(() -> {
			try {
				eventProducer(subject);
			} catch (IOException e) {
				logger.error("FATAL!! Error while closing the socket. ", e);
			}
		});

		Constants.threadPoolExecutor.execute(() -> {
			try {
				waitForClientsAndSubscribe(clientServerSocket, subject);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});

	}

	static List<Socket> sockets = new ArrayList<>();

	private static void waitForClientsAndSubscribe(ServerSocket clientServerSocket, Observable subject)
			throws IOException, InterruptedException {
		try {
			while (!Constants.isEmpty() || !Constants.isComplete()) {

				Socket socket = clientServerSocket.accept();
				sockets.add(socket);
				Constants.threadPoolExecutor.execute(() -> {
					try {
						String userId;
						userId = new Helper().readValueFromInputStream(socket.getInputStream());
						UserData ud = new UserData(socket);
						ud.setUserId(userId);
						logger.info("Client {} Connected on port {} ", userId, socket.getPort());
						new EventObserver(subject, ud);
					} catch (IOException e) {
						logger.error("Error while reading data from stream... Stopping Execution ", e);
						throw new RuntimeException(e);
					}
				});
			}

		} catch (SocketException e) {
			logger.info("Stopped Subscribing for clients");
		} catch (IOException e) {
			logger.error("Error while waiting for clients.. Stopping Execution ", e);
			throw new RuntimeException(e);
		} finally {
			logger.info("Shutting Down. Bye!");
			Constants.threadPoolExecutor.shutdown();
		}
	}

	private static void eventProducer(Observable subject) throws IOException {
		logger.info("Started to watch queue for new messages...");
		while (!(Constants.isEmpty() && Constants.isComplete())) {
			EventData peek = Constants.peek();
			if (peek != null
					&& Long.toString(peek.getMessageNumber()).equals(Long.toString(Constants.getMessageCounter()))) {
				EventData latestEvent = Constants.poll();
				subject.setData(latestEvent);
				Constants.incrementCount();
			}
		}

		logger.debug("Closing Sockets");
		sockets.stream().parallel().forEach(x -> {
			try {
				x.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		logger.debug("closing client server socket");
		clientServerSocket.close();
		logger.info("Stopped listening to Queue");
	}

	private static void readInputstreamAndEnqueue(Socket eventSocket) {
		try {
			logger.info("Started to look for incoming events.");
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (true) {
				String inputLine = in.readLine();
				if (inputLine == null)
					break;
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value = new EventData(inputLine);
					new Helper().processInputLine(value);
					Constants.offer(value);
				}
			}
			in.close();
			eventSocket.close();
			Constants.setComplete(true);
			eventServerSocket.close();
		} catch (IOException | BadInputException e) {
			logger.error("Error while reading Event Inputstream ", e);
		} finally {
			logger.info("Stopped reading of events");
		}
	}
}
