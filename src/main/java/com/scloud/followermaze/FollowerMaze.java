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
/**
 * @author karthik
 *
 */
public class FollowerMaze {

	private static final Logger logger = LogManager.getLogger(FollowerMaze.class);

	private ServerSocket clientServerSocket;
	private ServerSocket eventServerSocket;
	private List<Socket> clientSockets;
	private Constants constants;

	public FollowerMaze(ServerSocket clientServerSocket, ServerSocket eventServerSocket) {
		this.clientServerSocket = clientServerSocket;
		this.eventServerSocket = eventServerSocket;
		this.clientSockets = new ArrayList<>();
		constants = new Constants();
	}

	public void startUp(ServerSocket clientServerSocket, Socket eventSocket) throws IOException {

		Observable subject = new Observable();

		constants.threadPoolExecutor.execute(() -> {
			readInputstreamAndEnqueue(eventSocket);
		});

		constants.threadPoolExecutor.execute(() -> {
			eventProducer(subject);
		});

		constants.threadPoolExecutor.execute(() -> {
			waitForClientsAndSubscribe(clientServerSocket, subject);
		});
	}

	
	/**
	 * @param clientServerSocket
	 * @param subject
	 * 
	 * @method waitForClientsAndSubscribe
	 * 
	 * Waits for the clients to be connected and registers each with the observable. 
	 */
	private void waitForClientsAndSubscribe(ServerSocket clientServerSocket, Observable subject) {
		try {
			while (!constants.isEmpty() || !constants.isComplete()) {
				Socket socket = clientServerSocket.accept();
				clientSockets.add(socket);
				String userId;
				userId = new Helper().readValueFromInputStream(socket.getInputStream());
				UserData ud = new UserData(socket);
				ud.setUserId(userId);
				logger.info("Client {} Connected on port {} ", userId, socket.getPort());
				new EventObserver(subject, ud);
			}
		} catch (SocketException e) {
			logger.info("Stopped Subscribing for clients");
		} catch (IOException e) {
			logger.error("Error on client socket... Stopping Execution ", e);
			shutDown();
			throw new RuntimeException(e);
		} finally {
			shutDownExecutor();
		}
	}

	/**
	 * @param subject
	 * @method eventProducer
	 * 
	 * Watches the min priority queue for the next message to be processed.
	 * and increments the message count.  
	 */
	private void eventProducer(Observable subject) {
		logger.info("Started to watch queue for new messages...");
		try {
			while (!(constants.isEmpty() && constants.isComplete())) {
				EventData peek = constants.peek();
				if (peek != null && Long.toString(peek.getMessageNumber())
						.equals(Long.toString(constants.getMessageCounter()))) {
					EventData latestEvent = constants.poll();
					try {
						subject.setData(latestEvent);
					} catch (BadInputException e) {
						logger.error("Published bad event. Ignoring..", e);
					}
					constants.incrementCount();
				}
			}
		} catch (IOException e) {
			logger.error("Error on client socket... Stopping Execution ", e);
			shutDown();
			throw new RuntimeException(e);
		} finally {
			shutDownClientConnections();

		}
	}

	/**
	 * @param eventSocket
	 * @method readInputstreamAndEnqueue
	 * 
	 * Reads the stream incoming from the event socket and on input, 
	 * Processes the input and enqueues. 
	 */
	private void readInputstreamAndEnqueue(Socket eventSocket) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));) {
			logger.info("Started to look for incoming events.");
			while (true) {
				String inputLine = in.readLine();
				if (inputLine == null)
					break;
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value;
					try {
						value = new Helper().processInputLine(inputLine);
						constants.offer(value);
					} catch (BadInputException e) {
						logger.error("Wrong kind of Input {} from the event. Ignoring and Proceeding...", inputLine, e);
					}
				}
			}

		} catch (IOException e) {
			logger.error("Error while reading Event Inputstream ", e);
		} finally {
			constants.setComplete(true);
			shutDownEventConnection();
		}
	}

	private void shutDownEventConnection() {
		try {
			eventServerSocket.close();
			logger.info("Stopped reading of events");
		} catch (IOException e) {
			throw new RuntimeException("Exception on shut down");
		}
	}

	private void shutDownClientConnections() {
		try {
			clientServerSocket.close();
			logger.info("Stopped listening for events");
		} catch (IOException e) {
			throw new RuntimeException("Exception on shut down");
		}
	}

	private void shutDownExecutor() {
		constants.threadPoolExecutor.shutdown();
		logger.info("Shutting Down. Bye!");
	}

	public void shutDown() {
		shutDownEventConnection();
		shutDownClientConnections();
		shutDownExecutor();

	}
}
