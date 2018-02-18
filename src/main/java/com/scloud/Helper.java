/**
 * 
 */
package com.scloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author karthik
 *
 */
public class Helper {

	private static final Logger logger = LogManager.getLogger(Helper.class);

	public void processInputLine(EventData model) throws InterruptedException {
		logger.debug("Processing {} ", model.inputLine);
		String[] split = model.inputLine.split("\\|");
		if (split.length >= 2 && EventTypes.fromString(split[1]) != null) {
			EventTypes eventType = EventTypes.fromString(split[1]);
			model.messageNumber = Long.parseLong(split[0]);
			switch (eventType) {
			case BROADCAST:
				model.eventType = EventTypes.BROADCAST;
				break;
			case FOLLOW:
				model.eventType = EventTypes.FOLLOW;
				model.toUserId = split[3];
				model.fromUserId = split[2];
				break;
			case PRIVATE:
				model.eventType = EventTypes.PRIVATE;
				model.toUserId = split[3];
				break;
			case STATUS_UPDATE:
				model.eventType = EventTypes.STATUS_UPDATE;
				model.fromUserId = split[2];
				break;
			case UNFOLLOW:
				model.eventType = EventTypes.UNFOLLOW;
				model.toUserId = split[3];
				model.fromUserId = split[2];
				break;
			default:
				logger.error("Wrong Input {} ", split);
				break;
			}
		}
	}

	public String readValueFromInputStream(Socket x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x.getInputStream()));
		String inputLine = in.readLine();
		return inputLine;
	}

	public void send(EventData event, Socket socket) {
		PrintWriter out;
		try {
			logger.info("Sending {}", event.inputLine);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(event.inputLine);
		} catch (IOException e) {
			logger.error(" Error sending {} ", event.inputLine, e);
		}
	}

	public void checkIfEventValidAndNotify(UserData ud, EventData event, Helper helper, Socket socket, String userId) {
		switch (event.eventType) {
		case BROADCAST:
			helper.send(event, socket);
			break;
		case FOLLOW:
			if (event.fromUserId.equals(userId)) {
				ud.follows.add(event.toUserId);
			} else if (event.toUserId.equals(userId)) {
				helper.send(event, socket);
			}
			break;
		case PRIVATE:
			if (event.toUserId.equals(userId)) {
				helper.send(event, socket);
			}
			break;
		case STATUS_UPDATE:
			if (ud.follows.contains(event.fromUserId)) {
				helper.send(event, socket);
			}
			break;
		case UNFOLLOW:
			if (event.fromUserId.equals(userId)) {
				ud.follows.remove(event.toUserId);
			}
			break;
		default:
			break;

		}
	}
}
