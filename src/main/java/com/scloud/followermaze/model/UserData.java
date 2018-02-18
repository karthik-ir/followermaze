/**
 * 
 */
package com.scloud.followermaze.model;

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
	private Set<String> follows = new HashSet<>();

	public UserData(Socket socket) {
		super();
		this.socket = socket;
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