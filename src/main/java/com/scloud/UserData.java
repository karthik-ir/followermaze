/**
 * 
 */
package com.scloud;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author karthik
 *
 */
public class UserData {
	String userId;
	Socket socket;
	long messageNumber = 0L;
	List<String> follows = new ArrayList<>();

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