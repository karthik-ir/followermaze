/**
 * 
 */
package com.scloud;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author karthik
 *
 */
public class UserData {
	String userId;
	Socket socket;
	long messageNumber = 0L;
	Set<String> follows = new HashSet<>();

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

}