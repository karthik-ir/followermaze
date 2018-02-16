/**
 * 
 */
package com.scloud;

/**
 * @author karthik
 *
 */
public enum EventTypes {
	FOLLOW("F"), UNFOLLOW("U"), BROADCAST("B"), PRIVATE("P"), STATUS_UPDATE("S");

	private final String name;

	private EventTypes(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		// (otherName == null) check is not needed because name.equals(null) returns
		// false
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}

	public static EventTypes fromString(String text) {
		for (EventTypes b : EventTypes.values()) {
			if (b.name.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
