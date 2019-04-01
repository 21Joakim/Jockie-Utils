package com.jockie.bot.core.argument.impl;

import com.jockie.bot.core.argument.IArgument;

public class SimpleArgument<Type> extends ArgumentImpl<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, SimpleArgument<Type>, Builder<Type>> {
		
		public Builder<Type> self() {
			return this;
		}
		
		public SimpleArgument<Type> build() {
			return new SimpleArgument<>(this);
		}
	}
	
	private SimpleArgument(Builder<Type> builder) {
		super(builder);
	}
}