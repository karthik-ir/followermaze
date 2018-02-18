package com.scloud.followermaze;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		logger.info("Starting....");
		String clientPort = System.getenv("clientListenerPort");
		String eventPort = System.getenv("eventListenerPort");

		ServerSocket clientServerSocket = new ServerSocket(clientPort != null ? Integer.parseInt(clientPort) : 9099);
		ServerSocket eventServerSocket = new ServerSocket(eventPort != null ? Integer.parseInt(eventPort) : 9090);

		// RUN the server
		FollowerMaze mazeRunner = new FollowerMaze(clientServerSocket,eventServerSocket);
		try {
			mazeRunner.begin(clientServerSocket, eventServerSocket.accept());
		} catch (IOException e) {
			logger.error("ERROR Downstream ", e);
		}
	}

	
}
