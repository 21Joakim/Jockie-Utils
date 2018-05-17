package com.jockie.bot.core.command.argument;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface IArgument<Type> {
	
	public abstract class Builder<RT, A extends IArgument<RT>, BT extends Builder<RT, A, BT>> {
		
		protected boolean endless, empty, quote;
		
		protected String description;
		
		protected RT defaultValue;
		
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
			this.defaultValue = defaultValue;
			
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
		
		public RT getDefaultValue() {
			return this.defaultValue;
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
	
	public Type getDefault(MessageReceivedEvent event);
	
	public VerifiedArgument<Type> verify(String value);
}