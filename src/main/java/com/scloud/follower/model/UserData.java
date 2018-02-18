/**
 * 
 */
package com.scloud.follower.model;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author karthik
 *
 */
public class UserData {
	private String userId;
	private Socket socket;
	private long messageNumber = 0L;
	private Set<String> follows = new HashSet<>();

	public UserData(Socket socket) {
		super();
		this.socket = socket;
	}

	public long getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(long messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Set<String> getFollows() {
		return follows;
	}

	public void setFollows(Set<String> follows) {
		this.follows = follows;
	}

}