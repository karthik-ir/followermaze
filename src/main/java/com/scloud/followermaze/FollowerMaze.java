/**
 * 
 */
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

/**
 * @author karthik
 *
 */
public class FollowerMaze {

	private static final Logger logger = LogManager.getLogger(FollowerMaze.class);

	private ServerSocket clientServerSocket;
	private ServerSocket eventServerSocket;
	List<Socket> clientSockets;

	public FollowerMaze(ServerSocket clientServerSocket, ServerSocket eventServerSocket) {
		this.clientServerSocket = clientServerSocket;
		this.eventServerSocket = eventServerSocket;
		this.clientSockets = new ArrayList<>();
	}

	public void begin(ServerSocket clientServerSocket, Socket eventSocket) {

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

	private void waitForClientsAndSubscribe(ServerSocket clientServerSocket, Observable subject)
			throws IOException, InterruptedException {
		try {
			while (!Constants.isEmpty() || !Constants.isComplete()) {
				Socket socket = clientServerSocket.accept();
				clientSockets.add(socket);
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

	private void eventProducer(Observable subject) throws IOException {
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
		clientSockets.stream().parallel().forEach(x -> {
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

	private void readInputstreamAndEnqueue(Socket eventSocket) {
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