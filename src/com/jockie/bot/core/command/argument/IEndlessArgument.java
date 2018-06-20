package com.jockie.bot.core.command.argument;

public interface IEndlessArgument<Type> extends IArgument<Type[]> {
	
	public int getMinArguments();
	
	public int getMaxArguments();
	
	public abstract class Builder<RT, A extends IEndlessArgument<RT>, BT extends Builder<RT, A, BT>> extends IArgument.Builder<RT[], A, BT> {
		
		private int minArguments = 1, maxArguments = 0;
		
		public Builder() {
			this.quote = false;
			
			/* Endless by default since I am not sure if you want to have it inside of brackets and it is not fully implemented */
			this.endless = true;
		}
		
		/* I am not sure when you would want to have a quoted endless argument therefore this will be disabled for now */
		public BT setAcceptQuote(boolean quote) {
			throw new IllegalArgumentException("Endless arguments can not be quoted");
		}
		
		public BT setMinArguments(int minArguments) {
			this.minArguments = minArguments;
			
			return this.self();
		}
		
		public BT setMaxArguments(int maxArguments) {
			this.maxArguments = maxArguments;
			
			return this.self();
		}
		
		public int getMinArguments() {
			return this.minArguments;
		}
		
		public int getMaxArguments() {
			return this.maxArguments;
		}
	}
}