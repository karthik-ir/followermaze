/**
 * 
 */
package com.scloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author karthik
 *
 */
public class Helper {

	public void processInputLine(EventData model) throws InterruptedException {
		// System.out.println("processing " + model.inputLine + " " +
		// Thread.currentThread().getName());
		String[] split = model.inputLine.split("\\|");
		if (split.length >= 2 && EventTypes.fromString(split[1]) != null) {
			EventTypes eventType = EventTypes.fromString(split[1]);
			model.messageNumber = Long.parseLong(split[0]);
			switch (eventType) {
			case BROADCAST:
				model.eventType = EventTypes.BROADCAST;
				break;
			case FOLLOW:
				model.eventType = EventTypes.FOLLOW;
				model.toUserId = split[3];
				model.fromUserId = split[2];
				break;
			case PRIVATE:
				model.eventType = EventTypes.PRIVATE;
				model.toUserId = split[3];
				break;
			case STATUS_UPDATE:
				model.eventType = EventTypes.STATUS_UPDATE;
				// TODO: add all the followers to the userIds list
				model.fromUserId = split[2];
				break;
			case UNFOLLOW:
				model.eventType = EventTypes.UNFOLLOW;
				model.toUserId = split[3];
				model.fromUserId = split[2];
				break;
			default:
				System.out.println("SKIPPING");
				break;
			}
		} else {
			System.out.println("SOMETHING WRONG WITH INPUT");
		}
	}
	
	public String readValueFromInputStream(Socket x) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(x.getInputStream()));
		String inputLine = in.readLine();
		return inputLine;
	}
	
	public void send(EventData event, Socket socket) {
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(event.inputLine);
			System.out.println("Sending " + event.inputLine + " " + Thread.currentThread().getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkIfEventValidAndProceed(UserData ud, EventData event, Helper helper, Socket socket,
			String userId) {
		if (event.eventType == EventTypes.UNFOLLOW) {
			if(event.fromUserId.equals(userId)) {
				ud.follows.remove(event.toUserId);
			}
			return;
		}
		if (event.eventType == EventTypes.BROADCAST) {
			helper.send(event, socket);
			return;
		}
		if (event.eventType == EventTypes.STATUS_UPDATE) {
//			if(Long.toString(event.messageNumber).equals("129"))
//				System.out.println(" POPOP "+event.fromUserId);
			//&& (ud.notified.get(event.fromUserId)==null|| ud.notified.get(event.fromUserId)==false)
			if (ud.follows.contains(event.fromUserId) ) {
				helper.send(event, socket);
				ud.notified.put(event.fromUserId,true);
			}
			return;
		}
		if (event.eventType == EventTypes.FOLLOW) {
			if(event.fromUserId.equals(userId)) {
				//If he is a fresh follower
				ud.follows.add(event.toUserId);
//				System.out.println(event.fromUserId + " is following " + ud.follows);
//			} else if(ud.follows.contains(event.toUserId)) {
				//Reset the notified field on new follower addition.
//				ud.notified.put(event.toUserId, false);
			} else if (event.toUserId.equals(userId)) {
				helper.send(event, socket);
			}
			return;
		}

		if (event.toUserId.equals(userId)) {
			helper.send(event, socket);
			return;
		}
	}
}
