package com.scloud.followermaze;

import java.io.IOException;

import com.scloud.followermaze.exception.BadInputException;

public abstract class Observer {

	protected Observable subject;

	public abstract void subscribe() throws IOException, BadInputException;
}
