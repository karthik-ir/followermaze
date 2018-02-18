package com.scloud.followermaze;

public abstract class Observer {

	protected Observable subject;
	public abstract void subscribe();
}
