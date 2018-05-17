package com.jockie.bot.core.command.argument;

public interface IEndlessArgument<Type> extends IArgument<Type[]> {
	
	public abstract class Builder<RT, A extends IEndlessArgument<RT>, BT extends Builder<RT, A, BT>> extends IArgument.Builder<RT[], A, BT> {
		
		private int minArguments = 1, maxArguments = 0;
		
		public Builder() {
			this.endless = true;
		}
		
		public BT setEndless(boolean endless) {
			throw new IllegalArgumentException("You many not change this property");
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
	
	public int getMinArguments();
	
	public int getMaxArguments();
	
}