package com.jockie.bot.core.timeout;

import java.util.concurrent.TimeUnit;

public class TimeoutEvent<T> {
	
	public T calledFrom;
	
	public long timeoutTime;
	public TimeUnit timeoutUnit;
	
}