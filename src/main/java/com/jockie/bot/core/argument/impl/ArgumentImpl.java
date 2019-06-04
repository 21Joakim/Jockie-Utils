package com.jockie.bot.core.argument.impl;

import com.jockie.bot.core.argument.IArgument;

public class ArgumentImpl<Type> extends AbstractArgument<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, ArgumentImpl<Type>, Builder<Type>> {
		
		public Builder<Type> self() {
			return this;
		}
		
		public ArgumentImpl<Type> build() {
			return new ArgumentImpl<>(this);
		}
	}
	
	private ArgumentImpl(Builder<Type> builder) {
		super(builder);
	}
}