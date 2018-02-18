/**
 * 
 */
package com.scloud.followermaze;

import java.util.ArrayList;
import java.util.List;

import com.scloud.followermaze.model.EventData;

/**
 * @author karthik
 *
 */
public class Observable {

	private EventData data;

	private List<Observer> observers = new ArrayList<Observer>();

	public void execute(EventData data) {
	}

	public EventData getData() {
		return data;
	}

	public void setData(EventData data) {
		this.data = data;
		notifyAllObservers();
	}

	public List<Observer> getObservers() {
		return observers;
	}

	public void setObservers(List<Observer> observers) {
		this.observers = observers;
	}

	public void attach(Observer observer) {
		observers.add(observer);
	}

	public void notifyAllObservers() {
		for (Observer observer : observers) {
			observer.subscribe();
		}
	}
}
