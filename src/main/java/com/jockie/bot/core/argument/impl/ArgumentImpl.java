package com.jockie.bot.core.argument.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.parser.IParser;

import net.dv8tion.jda.api.entities.Message;

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
	
	private final IParser<Type, IArgument<Type>> parser;
	
	private final Map<String, Object> properties;
	
	protected <BuilderType extends IArgument.Builder<Type, ?, ?>> ArgumentImpl(BuilderType builder) {
		this.type = builder.getType();
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.name = builder.getName();
		this.errorConsumer = builder.getErrorConsumer();
		this.defaultValueFunction = builder.getDefaultValueFunction();
		this.parser = builder.getParser();
		this.properties = new HashMap<>(builder.getProperties());
	}
	
	@Override
	public Class<Type> getType() {
		return this.type;
	}
	
	@Override
	public boolean hasDefault() {
		return this.defaultValueFunction != null;
	}
	
	@Override
	public Type getDefault(CommandEvent commandEvent) {
		if(this.defaultValueFunction != null) {
			return this.defaultValueFunction.apply(commandEvent);
		}
		
		return null;
	}
	
	@Override
	public boolean isEndless() {
		return this.endless;
	}
	
	@Override
	public boolean acceptQuote() {
		return this.quote;
	}
	
	@Override
	public boolean acceptEmpty() {
		return this.empty;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public BiConsumer<Message, String> getErrorConsumer() {
		return this.errorConsumer;
	}
	
	@Override
	public IParser<Type, IArgument<Type>> getParser() {
		return this.parser;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getProperty(@Nonnull String key, @Nullable T defaultValue) {
		return (T) this.properties.getOrDefault(key, defaultValue);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
}