package com.jockie.bot.core.command.argument.impl;

import java.util.function.BiFunction;

import com.jockie.bot.core.command.argument.IArgument;

public class SimpleArgument<Type> extends ArgumentImpl<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, SimpleArgument<Type>, Builder<Type>> {
		
		private BiFunction<SimpleArgument<Type>, String, VerifiedArgument<Type>> function;
		
		public Builder<Type> setFunction(BiFunction<SimpleArgument<Type>, String, VerifiedArgument<Type>> function) {
			this.function = function;
			
			return this.self();
		}
		
		public BiFunction<SimpleArgument<Type>, String, VerifiedArgument<Type>> getFunction() {
			return this.function;
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public SimpleArgument<Type> build() {
			return new SimpleArgument<>(this);
		}
	}
	
	private BiFunction<SimpleArgument<Type>, String, VerifiedArgument<Type>> function;
	
	private SimpleArgument(Builder<Type> builder) {
		super(builder);
		
		this.function = builder.getFunction();
	}
	
	public VerifiedArgument<Type> verify(String value) {
		return this.function.apply(this, value);
	}
}