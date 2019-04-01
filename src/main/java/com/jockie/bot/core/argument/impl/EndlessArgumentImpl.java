package com.jockie.bot.core.argument.impl;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;

public class EndlessArgumentImpl<Type> extends ArgumentImpl<Type[]> implements IEndlessArgument<Type> {
	
	private final IArgument<Type> argument;
	
	private final int minArguments, maxArguments;
	
	private final Class<Type> argumentType;
	
	public static class Builder<Type> extends IEndlessArgument.Builder<Type, IEndlessArgument<Type>, Builder<Type>> {
		
		private IArgument<Type> argument;
		
		private Class<Type> argumentType;
		
		public Builder(Class<Type> argumentType) {
			if(argumentType.isPrimitive()) {
				throw new IllegalArgumentException("Primitve types are currently not supported for endless arguments");
			}
			
			this.argumentType = argumentType;
			this.empty = true;
			this.parser = EndlessArgumentParser.getInstance();
		}
		
		public Builder<Type> setArgument(IArgument<Type> argument) {
			if(argument instanceof IEndlessArgument || argument.isEndless()) {
				throw new IllegalArgumentException("Not a valid candidate, argument may not be endless");
			}
			
			this.argument = argument;
			this.name = argument.getName();
			
			return this.self();
		}
		
		public Builder<Type> setAcceptEmpty(boolean empty) {
			throw new UnsupportedOperationException();
		}
		
		public Builder<Type> setParser(IArgumentParser<Type[]> parser) {
			throw new UnsupportedOperationException();
		}
		
		public IArgument<Type> getArgument() {
			return this.argument;
		}
		
		public Class<Type> getArgumentType() {
			return this.argumentType;
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public IEndlessArgument<Type> build() {
			return new EndlessArgumentImpl<>(this);
		}
	}
	
	private EndlessArgumentImpl(Builder<Type> builder) {
		super(builder);
		
		this.argumentType = builder.getArgumentType();
		this.argument = builder.getArgument();
		this.minArguments = builder.getMinArguments();
		this.maxArguments = builder.getMaxArguments();
	}
	
	public IArgument<Type> getArgument() {
		return this.argument;
	}
	
	public Class<Type> getArgumentType() {
		return this.argumentType;
	}
	
	public int getMinArguments() {
		return this.minArguments;
	}
	
	public int getMaxArguments() {
		return this.maxArguments;
	}
}