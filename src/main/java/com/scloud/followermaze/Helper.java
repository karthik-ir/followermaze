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

	public void processInputLine(EventData model) throws BadInputException {
		if (model == null || model.getInputLine() == null)
			throw new BadInputException("input value is null", null);
		String[] split = model.getInputLine().split("\\|");
		EventTypes eventType = EventTypes.fromString(split[1]);
		if (split.length < 2 || eventType == null)
			throw new BadInputException("Input " + model.getInputLine() + " Is Bad ", null);

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
	}

	public String readValueFromInputStream(InputStream x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x));
		String inputLine = in.readLine();
		return inputLine;
	}

	public void send(EventData event, UserData user) throws IOException {
		PrintWriter out;
		try {
			logger.info("Sending {} to {} ", event.getInputLine(), user.getUserId());
			out = new PrintWriter(user.getSocket().getOutputStream(), true);
			out.println(event.getInputLine());
		} catch (IOException e) {
			logger.error(" Error getting Output stream while sending {} ", event.getInputLine(), e);
			throw e;
		}
	}

	public void checkIfEventValidAndNotify(UserData ud, EventData event) throws IOException {
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
