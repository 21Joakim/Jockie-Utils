package com.jockie.bot.core.argument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.utility.CommandUtility;

public interface IEndlessArgument<Type> extends IArgument<Type[]> {
	
	/**
	 * @return the minimum amount of provided arguments allowed for this argument
	 */
	public int getMinArguments();
	
	/**
	 * @return the maximum amount of provided arguments allowed for this argument, 
	 * if this is less or equal to 0 no maximum limit will be set
	 */
	public int getMaxArguments();
	
	/**
	 * @return the wrapped argument, this is the argument the content will be parsed as
	 */
	@Nonnull
	public IArgument<Type> getArgument();
	
	/**
	 * @return the component type of the array the content would be parsed as
	 */
	@Nonnull
	public Class<Type> getComponentType();
	
	public abstract class Builder<Type, ArgumentType extends IEndlessArgument<Type>, BuilderType extends Builder<Type, ArgumentType, BuilderType>> 
			extends IArgument.Builder<Type[], ArgumentType, BuilderType> {
		
		protected int minArguments = 1, maxArguments = 0;
		
		protected IArgument<Type> argument;
		
		protected Class<Type> componentType;
		
		protected Builder(@Nonnull Class<Type> componentType) {
			super(CommandUtility.getClassAsArray(componentType));
			
			this.quote = false;
			
			/* Endless by default since I am not sure if you want to have it inside of brackets and it is not fully implemented */
			this.endless = true;
			
			/* True by default */
			this.empty = true;
			
			this.componentType = componentType;
		}
		
		@Nonnull
		public BuilderType setMinArguments(int minArguments) {
			this.minArguments = minArguments;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setMaxArguments(int maxArguments) {
			this.maxArguments = maxArguments;
			
			return this.self();
		}
		
		public int getMinArguments() {
			return this.minArguments;
		}
		
		public int getMaxArguments() {
			return this.maxArguments;
		}
		
		@Nonnull
		public BuilderType setArgument(@Nonnull IArgument<Type> argument) {
			if(argument instanceof IEndlessArgument || argument.isEndless()) {
				throw new IllegalArgumentException("Not a valid candidate, argument may not be endless");
			}
			
			this.argument = argument;
			this.name = argument.getName();
			
			return this.self();
		}
		
		@Nullable
		public IArgument<Type> getArgument() {
			return this.argument;
		}
		
		@Nonnull
		public Class<Type> getComponentType() {
			return this.componentType;
		}
	}
}