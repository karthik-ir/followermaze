/**
 * 
 */
package com.scloud.followermaze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.exception.BadInputException;
import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.model.EventTypes;
import com.scloud.followermaze.model.UserData;

/**
 * @author karthik
 *
 */
public class Helper {

	private static final Logger logger = LogManager.getLogger(Helper.class);

	public EventData processInputLine(String inputLine) throws BadInputException {

		if (inputLine == null || inputLine == null || inputLine.isEmpty())
			throw new BadInputException("input value is null", null);

		String[] split = inputLine.split("\\|");
		if (split.length < 2)
			throw new BadInputException("Input " + inputLine + " Is Bad ", null);
		EventTypes eventType = EventTypes.fromString(split[1]);

		if (eventType == null)
			throw new BadInputException("Event doesnot exisit for Input " + inputLine, null);

		EventData model = new EventData(inputLine);
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
			model.setFromUserId(split[2]);
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
			break;
		}
		return model;
	}

	public String readValueFromInputStream(InputStream x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x));
		String inputLine = in.readLine();
		return inputLine;
	}

	public Boolean send(EventData event, UserData user) throws IOException {
		PrintWriter out;
		try {
			logger.info("Sending {} to {} ", event.getInputLine(), user.getUserId());
			out = new PrintWriter(user.getSocket().getOutputStream(), true);
			out.println(event.getInputLine());
			return true;
		} catch (IOException e) {
			logger.error(" Error getting Output stream while sending {} ", event.getInputLine(), e);
			throw e;
		}
	}

	public boolean notifyClient(UserData ud, EventData event) throws IOException {
		String userId = ud.getUserId();
		boolean sent = false;
		switch (event.getEventType()) {
		case BROADCAST:
			sent = send(event, ud);
			break;
		case FOLLOW:
			if (event.getFromUserId().equals(userId)) {
				ud.getFollows().add(event.getToUserId());
			} else if (event.getToUserId().equals(userId)) {
				sent = send(event, ud);
			}
			break;
		case PRIVATE:
			if (event.getToUserId().equals(userId)) {
				sent = send(event, ud);
			}
			break;
		case STATUS_UPDATE:
			if (ud.getFollows().contains(event.getFromUserId())) {
				sent = send(event, ud);
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
		return sent;
	}
}
