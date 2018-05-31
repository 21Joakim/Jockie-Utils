package com.jockie.bot.core.await;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.jockie.bot.core.timeout.Timeoutable;
import net.dv8tion.jda.core.events.Event;

public class AwaitEvent<T extends Event> extends Timeoutable<AwaitEvent<T>> {
	
	private Predicate<T> predicate;
	
	private Consumer<T> eventHandler;
	
	public AwaitEvent(Predicate<T> predicate, Consumer<T> eventHandler) {
		this.predicate = predicate;
		this.eventHandler = eventHandler;
		
		/* Default */
		this.timeout = true;
		this.timeoutTime = 30;
		this.timeoutUnit = TimeUnit.SECONDS;
	}
	
	@SuppressWarnings("unchecked")
	public void call(Event event) {
		try {
			T type = (T) event;
			
			if(this.predicate.test(type)) {
				new Thread(() -> this.eventHandler.accept(type)).start();
				
				this.timeoutFinish.run();
			}
		}catch(ClassCastException e) {}
	}
}