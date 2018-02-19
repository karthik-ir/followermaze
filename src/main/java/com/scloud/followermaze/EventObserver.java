package com.scloud.followermaze;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.scloud.followermaze.exception.BadInputException;
import com.scloud.followermaze.model.UserData;

public class EventObserver extends Observer {
	private static final Logger logger = LogManager.getLogger(EventObserver.class);

	private UserData userData;

	public EventObserver(Observable subject, UserData ud) {
		this.subject = subject;
		this.userData = ud;
		this.subject.attach(this);
	}

	@Override
	public void subscribe() throws IOException, BadInputException {
		logger.debug("{} Received {} ", userData.getUserId(), subject.getData().getInputLine());
		new Helper().notifyClient(userData, subject.getData());
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

}
