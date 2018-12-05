package com.jockie.bot.core.argument.impl;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.argument.impl.parser.IArgumentParser;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SimpleArgument<Type> extends ArgumentImpl<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, SimpleArgument<Type>, Builder<Type>> {
		
		private IArgumentParser<Type> parser;
		
		public Builder<Type> setParser(IArgumentParser<Type> parser) {
			this.parser = parser;
			
			return this.self();
		}
		
		public IArgumentParser<Type> getParser() {
			return this.parser;
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public SimpleArgument<Type> build() {
			return new SimpleArgument<>(this);
		}
	}
	
	private final IArgumentParser<Type> parser;
	
	private SimpleArgument(Builder<Type> builder) {
		super(builder);
		
		this.parser = builder.parser;
	}
	
	public VerifiedArgument<Type> verify(MessageReceivedEvent event, String value) {
		return this.parser.parse(event, this, value);
	}
}