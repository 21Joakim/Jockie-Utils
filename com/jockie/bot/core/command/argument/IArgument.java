package com.jockie.bot.core.command.argument;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface IArgument<Type> {
	
	public abstract class Builder<RT, A extends IArgument<RT>, BT extends Builder<RT, A, BT>> {
		
		protected boolean endless, empty, quote;
		
		protected String description;
		
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
		
		public BT setDescription(String description) {
			this.description = description;
			
			return this.self();
		}
		
		public BT setDefaultValue(RT defaultValue) {
			this.defaultValueFunction = (event, commandEvent) -> {
				return defaultValue;
			};
			
			return this.self();
		}
		
		public BT setDefaultValue(Function<MessageReceivedEvent, RT> defaultValueFunction) {
			this.defaultValueFunction = (event, commandEvent) -> {
				return defaultValueFunction.apply(event);
			};
			
			return this.self();
		}
		
		public BT setDefaultValue(BiFunction<MessageReceivedEvent, CommandEvent, RT> defaultValueFunction) {
			this.defaultValueFunction = defaultValueFunction;
			
			return this.self();
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
		
		public String getDescription() {
			return this.description;
		}
		
		public BiFunction<MessageReceivedEvent, CommandEvent, RT> getDefaultValueFunction() {
			return this.defaultValueFunction;
		}
		
		public abstract BT self();
		
		public abstract A build();
	}
	
	public class VerifiedArgument<Type> {
		
		public enum VerifiedType {
			INVALID,
			VALID,
			VALID_END_NOW;
		}
		
		private VerifiedType type;
		private Type object;
		
		public VerifiedArgument(VerifiedType type, Type object) {
			this.type = type;
			this.object = object;
		}
		
		public VerifiedType getVerifiedType() {
			return this.type;
		}
		
		public Type getObject() {
			return this.object;
		}
	}
	
	public boolean isEndless();
	
	public boolean acceptQuote();
	
	public boolean acceptEmpty();
	
	public boolean hasDefault();
	
	public String getDescription();
	
	public Type getDefault(MessageReceivedEvent event, CommandEvent commandEvent);
	
	public VerifiedArgument<Type> verify(MessageReceivedEvent event, String value);
}