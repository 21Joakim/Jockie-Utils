package com.jockie.bot.core.command.argument.impl;

import java.util.function.BiFunction;

import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.VerifiedArgument;
import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class ArgumentImpl<Type> implements IArgument<Type> {
	
	private BiFunction<MessageReceivedEvent, CommandEvent, Type> defaultValueFunction;
	
	private boolean endless, empty, quote;
	
	private String name, error;
	
	public <BuilderType extends IArgument.Builder<Type, ?, ?>> ArgumentImpl(BuilderType builder) {
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.name = builder.getName();
		this.error = builder.getError();
		this.defaultValueFunction = builder.getDefaultValueFunction();
	}
	
	public boolean hasDefault() {
		return this.defaultValueFunction != null;
	}
	
	public Type getDefault(MessageReceivedEvent event, CommandEvent commandEvent) {
		if(this.defaultValueFunction != null) {
			return this.defaultValueFunction.apply(event, commandEvent);
		}
		
		return null;
	}
	
	public boolean isEndless() {
		return this.endless;
	}
	
	public boolean acceptQuote() {
		return this.quote;
	}
	
	public boolean acceptEmpty() {
		return this.empty;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getError() {
		return this.error;
	}
	
	public abstract VerifiedArgument<Type> verify(MessageReceivedEvent event, String value);
}