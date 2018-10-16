package com.jockie.bot.core.argument;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface IArgument<Type> {
	
	public boolean isEndless();
	
	public boolean acceptQuote();
	
	public boolean acceptEmpty();
	
	public boolean hasDefault();
	
	public String getName();
	
	public Type getDefault(MessageReceivedEvent event, CommandEvent commandEvent);
	
	public VerifiedArgument<Type> verify(MessageReceivedEvent event, String value);
	
	public abstract class Builder<RT, A extends IArgument<RT>, BT extends Builder<RT, A, BT>> {
		
		/* I see no reason not to allow quoted by default */
		protected boolean endless, empty, quote = true;
		
		protected String name, error;
		
		protected BiFunction<MessageReceivedEvent, CommandEvent, RT> defaultValueFunction;
		
		public BT setEndless(boolean endless) {
			this.endless = endless;
			
			return this.self();
		}
		
		public BT setAcceptEmpty(boolean empty) {
			this.empty = empty;
			
			return this.self();
		}
		
		public BT setAcceptQuote(boolean quote) {
			this.quote = quote;
			
			return this.self();
		}
		
		public BT setName(String name) {
			this.name = name;
			
			return this.self();
		}
		
		public BT setDefaultValue(BiFunction<MessageReceivedEvent, CommandEvent, RT> defaultValueFunction) {
			this.defaultValueFunction = defaultValueFunction;
			
			return this.self();
		}
		
		public BT setDefaultValue(RT defaultValue) {
			return this.setDefaultValue((event, commandEvent) -> {
				return defaultValue;
			});
		}
		
		public BT setDefaultValue(Function<MessageReceivedEvent, RT> defaultValueFunction) {
			return this.setDefaultValue((event, commandEvent) -> {
				return defaultValueFunction.apply(event);
			});
		}
		
		public BT setDefaultAsNull() {			
			return this.setDefaultValue((a, b) -> null);
		}
		
		public boolean isEndless() {
			return this.endless;
		}
		
		public boolean isAcceptEmpty() {
			return this.empty;
		}
		
		public boolean isAcceptQuote() {
			return this.quote;
		}
		
		public String getName() {
			return this.name;
		}
		
		public BiFunction<MessageReceivedEvent, CommandEvent, RT> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		public abstract BT self();
		
		public abstract A build();
	}
}