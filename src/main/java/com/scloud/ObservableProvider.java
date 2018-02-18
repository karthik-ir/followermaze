/**
 * 
 */
package com.scloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * @author karthik
 *
 */
public class ObservableProvider {

	private static final Logger logger = LogManager.getLogger(ObservableProvider.class);

	public Flowable<EventData> getEventsObservable(Socket eventSocket) throws IOException {
		return Flowable.create((source) -> {
			BufferedReader in = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
			while (!source.isCancelled()) {
				String inputLine = in.readLine();
				if (inputLine != null && !inputLine.isEmpty()) {
					EventData value = new EventData(inputLine);
					source.onNext(value);
					logger.debug("Created {} ", inputLine);
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
			while (true) {
				EventData peek = Constants.peek();
				if (peek != null
						&& Long.toString(peek.messageNumber).equals(Long.toString(Constants.count))) {
					EventData latestEvent = Constants.poll();
					x.onNext(latestEvent);
					Constants.incrementCount();
					logger.debug("Latest event {}", latestEvent.inputLine);
				}
			}
		}).observeOn(Constants.scheduler).share().map(raw -> {
			return (EventData) raw;
		});
	}
}
