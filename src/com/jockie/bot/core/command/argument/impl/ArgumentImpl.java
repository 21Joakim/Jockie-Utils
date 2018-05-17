package com.jockie.bot.core.command.argument.impl;

import com.jockie.bot.core.command.argument.IArgument;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class ArgumentImpl<Type> implements IArgument<Type> {
	
	private Type defaultValue;
	
	private boolean endless, empty, quote;
	
	private String description;
	
	public <BuilderType extends IArgument.Builder<Type, ?, ?>> ArgumentImpl(BuilderType builder) {
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.description = builder.getDescription();
		this.defaultValue = builder.getDefaultValue();
	}
	
	public boolean hasDefault() {
		return this.defaultValue != null;
	}
	
	public Type getDefault(MessageReceivedEvent event) {
		return this.defaultValue;
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
	
	public String getDescription() {
		return this.description;
	}
	
	public abstract VerifiedArgument<Type> verify(String value);
}