package com.jockie.bot.core.argument.impl;

import javax.annotation.Nonnull;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.parser.IParser;

public class EndlessArgumentImpl<Type> extends ArgumentImpl<Type[]> implements IEndlessArgument<Type> {
	
	public static class Builder<Type> extends IEndlessArgument.Builder<Type, IEndlessArgument<Type>, Builder<Type>> {
		
		public Builder(@Nonnull Class<Type> componentType) {
			super(componentType);
			
			if(componentType.isPrimitive()) {
				throw new IllegalArgumentException("Primitve types are currently not supported for endless arguments");
			}
			
			this.parser = EndlessArgumentParser.getInstance();
		}
		
		/**
		 * @throws UnsupportedOperationException
		 */
		public Builder<Type> setAcceptQuote(boolean quote) {
			/* I am not sure when you would want to have a quoted endless argument therefore this will be disabled for now */
			throw new UnsupportedOperationException("Endless arguments can not be quoted");
		}
		
		/**
		 * @throws UnsupportedOperationException
		 */
		public Builder<Type> setParser(IParser<Type[], IArgument<Type[]>> parser) {
			throw new UnsupportedOperationException();
		}
		
		@Nonnull
		public Builder<Type> self() {
			return this;
		}
		
		@Nonnull
		public IEndlessArgument<Type> build() {
			return new EndlessArgumentImpl<>(this);
		}
	}
	
	private final IArgument<Type> argument;
	
	private final int minArguments, maxArguments;
	
	private final Class<Type> componentType;
	
	private EndlessArgumentImpl(Builder<Type> builder) {
		super(builder);
		
		this.componentType = builder.getComponentType();
		this.argument = builder.getArgument();
		this.minArguments = builder.getMinArguments();
		this.maxArguments = builder.getMaxArguments();
	}
	
	public IArgument<Type> getArgument() {
		return this.argument;
	}
	
	public Class<Type> getComponentType() {
		return this.componentType;
	}
	
	public int getMinArguments() {
		return this.minArguments;
	}
	
	public int getMaxArguments() {
		return this.maxArguments;
	}
}