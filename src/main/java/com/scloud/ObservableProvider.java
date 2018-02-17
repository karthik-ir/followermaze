/**
 * 
 */
package com.scloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * @author karthik
 *
 */
public class ObservableProvider {

	public Flowable<EventData> getEventsObservable(Socket eventSocket) throws IOException {
		return Flowable.create((source) -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (!source.isCancelled()) {
				String inputLine = in.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value = new EventData(inputLine);
					source.onNext(value);
					// System.out.println("Created " + inputLine + " at " + " " +
					// Thread.currentThread().getName());
				}
			}
			in.close();
			source.onComplete();
		}, BackpressureStrategy.BUFFER).subscribeOn(Constants.scheduler).observeOn(Constants.scheduler).map(model -> {
			new Helper().processInputLine((EventData) model);
			return (EventData) model;
		});
	}
	
	public Observable<EventData> getQueueObservable() {
		return Observable.create((x) -> {
//			 System.out.println("Looking for queue on " +
//			 Thread.currentThread().getName());
			while (true) {
				if (!Constants.sortedEvents.isEmpty() && Constants.peek() != null
						&& Long.toString(Constants.peek().messageNumber).equals(Long.toString(Constants.count))) {
					x.onNext(Constants.poll());
					Constants.incrementCount();
				}
			}
		}).observeOn(Constants.scheduler).share().map(raw->{return (EventData) raw;});
	}
}
