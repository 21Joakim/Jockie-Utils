package com.jockie.bot.core.argument.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.command.impl.CommandEvent;

import net.dv8tion.jda.core.entities.Message;

public class ArgumentImpl<Type> implements IArgument<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, ArgumentImpl<Type>, Builder<Type>> {
		
		public Builder(Class<Type> type) {
			super(type);
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public ArgumentImpl<Type> build() {
			return new ArgumentImpl<>(this);
		}
	}
	
	private final Class<Type> type;
	
	private final Function<CommandEvent, Type> defaultValueFunction;
	
	private final boolean endless, empty, quote;
	
	private final String name;
	
	private final BiConsumer<Message, String> errorConsumer;
	
	private final IArgumentParser<Type> parser;
	
	protected <BuilderType extends IArgument.Builder<Type, ?, ?>> ArgumentImpl(BuilderType builder) {
		this.type = builder.getType();
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.name = builder.getName();
		this.errorConsumer = builder.getErrorConsumer();
		this.defaultValueFunction = builder.getDefaultValueFunction();
		this.parser = builder.getParser();
	}
	
	public Class<Type> getType() {
		return this.type;
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
	
	public BiConsumer<Message, String> getErrorConsumer() {
		return this.errorConsumer;
	}
	
	public IArgumentParser<Type> getParser() {
		return this.parser;
	}
}