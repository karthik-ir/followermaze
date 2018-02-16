package com.scloud;

public class EventData {
	String inputLine;
	String fromUserId;
	String toUserId;
	Long messageNumber;
	EventTypes eventType = null;

	public EventData(String inputLine) {
		super();
		this.inputLine = inputLine;
	}

}