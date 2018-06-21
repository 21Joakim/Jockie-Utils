package com.jockie.bot.core.command.argument.impl;

import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.VerifiedArgument;
import com.jockie.bot.core.utility.TriFunction;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SimpleArgument<Type> extends ArgumentImpl<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, SimpleArgument<Type>, Builder<Type>> {
		
		private TriFunction<MessageReceivedEvent, SimpleArgument<Type>, String, VerifiedArgument<Type>> function;
		
		public Builder<Type> setFunction(TriFunction<MessageReceivedEvent, SimpleArgument<Type>, String, VerifiedArgument<Type>> function) {
			this.function = function;
			
			return this.self();
		}
		
		public TriFunction<MessageReceivedEvent, SimpleArgument<Type>, String, VerifiedArgument<Type>> getFunction() {
			return this.function;
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public SimpleArgument<Type> build() {
			return new SimpleArgument<>(this);
		}
	}
	
	private TriFunction<MessageReceivedEvent, SimpleArgument<Type>, String, VerifiedArgument<Type>> function;
	
	private SimpleArgument(Builder<Type> builder) {
		super(builder);
		
		this.function = builder.getFunction();
	}
	
	public VerifiedArgument<Type> verify(MessageReceivedEvent event, String value) {
		return this.function.apply(event, this, value);
	}
}