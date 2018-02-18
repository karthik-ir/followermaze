package com.scloud.followermaze.model;

public class EventData {
	private String inputLine;
	private String fromUserId;
	private String toUserId;
	private Long messageNumber;
	private EventTypes eventType = null;

	public String getInputLine() {
		return inputLine;
	}

	public void setInputLine(String inputLine) {
		this.inputLine = inputLine;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public Long getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(Long messageNumber) {
		this.messageNumber = messageNumber;
	}

	public EventTypes getEventType() {
		return eventType;
	}

	public void setEventType(EventTypes eventType) {
		this.eventType = eventType;
	}

	public EventData(String inputLine) {
		super();
		this.inputLine = inputLine;
	}

}