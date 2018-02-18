/**
 * 
 */
package com.scloud.followermaze.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.model.EventTypes;
import com.scloud.followermaze.model.UserData;

/**
 * @author karthik
 *
 */
public class Helper {

	private static final Logger logger = LogManager.getLogger(Helper.class);

	public void processInputLine(EventData model) throws InterruptedException {
		String[] split = model.getInputLine().split("\\|");
		if (split.length >= 2 && EventTypes.fromString(split[1]) != null) {
			EventTypes eventType = EventTypes.fromString(split[1]);
			model.setMessageNumber(Long.parseLong(split[0]));
			switch (eventType) {
			case BROADCAST:
				model.setEventType(EventTypes.BROADCAST);
				break;
			case FOLLOW:
				model.setEventType(EventTypes.FOLLOW);
				model.setToUserId(split[3]);
				model.setFromUserId(split[2]);
				break;
			case PRIVATE:
				model.setEventType(EventTypes.PRIVATE);
				model.setToUserId(split[3]);
				break;
			case STATUS_UPDATE:
				model.setEventType(EventTypes.STATUS_UPDATE);
				model.setFromUserId(split[2]);
				break;
			case UNFOLLOW:
				model.setEventType(EventTypes.UNFOLLOW);
				model.setToUserId(split[3]);
				model.setFromUserId(split[2]);
				break;
			default:
				logger.error("Wrong Input {} ", split, null);
				break;
			}
		}
	}

	public String readValueFromInputStream(Socket x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x.getInputStream()));
		String inputLine = in.readLine();
		return inputLine;
	}

	public void send(EventData event, UserData user) {
		PrintWriter out;
		try {
			logger.info("Sending {} to {} ", event.getInputLine(), user.getUserId());
			out = new PrintWriter(user.getSocket().getOutputStream(), true);
			out.println(event.getInputLine());
		} catch (IOException e) {
			logger.error(" Error sending {} ", event.getInputLine(), e);
		}
	}

	public void checkIfEventValidAndNotify(UserData ud, EventData event) {
		String userId = ud.getUserId();
		switch (event.getEventType()) {
		case BROADCAST:
			send(event, ud);
			break;
		case FOLLOW:
			if (event.getFromUserId().equals(userId)) {
				ud.getFollows().add(event.getToUserId());
			} else if (event.getToUserId().equals(userId)) {
				send(event, ud);
			}
			break;
		case PRIVATE:
			if (event.getToUserId().equals(userId)) {
				send(event, ud);
			}
			break;
		case STATUS_UPDATE:
			if (ud.getFollows().contains(event.getFromUserId())) {
				send(event, ud);
			}
			break;
		case UNFOLLOW:
			if (event.getFromUserId().equals(userId)) {
				ud.getFollows().remove(event.getToUserId());
			}
			break;
		default:
			break;

		}
	}
}
