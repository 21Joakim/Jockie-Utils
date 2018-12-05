package com.jockie.bot.core.argument.impl;

import java.util.function.Function;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.impl.CommandEvent;

public abstract class ArgumentImpl<Type> implements IArgument<Type> {
	
	private final Function<CommandEvent, Type> defaultValueFunction;
	
	private final boolean endless, empty, quote;
	
	private final String name;
	
	public <BuilderType extends IArgument.Builder<Type, ?, ?>> ArgumentImpl(BuilderType builder) {
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.name = builder.getName();
		this.defaultValueFunction = builder.getDefaultValueFunction();
	}
	
	public boolean hasDefault() {
		return this.defaultValueFunction != null;
	}
	
	public Type getDefault(CommandEvent commandEvent) {
		if(this.defaultValueFunction != null) {
			return this.defaultValueFunction.apply(commandEvent);
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
}