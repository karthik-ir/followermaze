package com.scloud.followermaze;

import java.io.IOException;

public abstract class Observer {

	protected Observable subject;

	public abstract void subscribe() throws IOException;
}
