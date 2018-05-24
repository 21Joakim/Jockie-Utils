package com.jockie.bot.core.timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Ugh... Works for now I suppose
 * 
 * @param <T> The class the timeout is being used by
 */
public class Timeoutable<T extends Timeoutable<T>> {
	
	protected boolean timeout;
	protected long timeoutTime;
	protected TimeUnit timeoutUnit;
	
	private Consumer<TimeoutEvent<T>> timeoutHandler;
	
	protected Runnable timeoutFinish;
	private ScheduledFuture<?> timeoutCall;
	
	private static ExecutorService cached = Executors.newCachedThreadPool();
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	public void stopTimeout() {
		if(this.timeout) {
			if(this.timeoutCall != null) {
				this.timeoutCall.cancel(true);
			}
		}
	}
	
	public void restartTimeout() {
		if(this.timeout) {
			if(this.timeoutCall != null) {
				this.timeoutCall.cancel(false);
				this.timeoutCall = null;
				this.startTimeout();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void startTimeout() {
		if(this.timeout) {
			if(this.timeoutCall == null) {
				this.timeoutCall = Timeoutable.executor.schedule(() -> {
					Timeoutable.cached.execute(() -> {
						if(Timeoutable.this.timeoutFinish != null) {
							Timeoutable.this.timeoutFinish.run();
						}
						
						if(Timeoutable.this.timeoutHandler != null) {
							TimeoutEvent<T> event = new TimeoutEvent<>();
							event.calledFrom = (T) Timeoutable.this;
							event.timeoutTime = Timeoutable.this.timeoutTime;
							event.timeoutUnit = Timeoutable.this.timeoutUnit;
							
							Timeoutable.this.timeoutHandler.accept(event);
						}
					});
				}, this.timeoutTime, this.timeoutUnit);
			}
		}
	}
	
	public void onTimeout(Consumer<TimeoutEvent<T>> consumer) {
		this.timeoutHandler = consumer;
	}
	
	public void setTimeout(boolean enabled) {
		this.timeout = enabled;
	}
	
	public void setTimeout(long time, TimeUnit unit) {
		this.timeoutTime = time;
		this.timeoutUnit = unit;
	}
	
	/**
	 * <b style="color:red">Do not touch</b>
	 */
	public void onTimeoutFinish(Runnable runnable) {
		this.timeoutFinish = runnable;
	}
}