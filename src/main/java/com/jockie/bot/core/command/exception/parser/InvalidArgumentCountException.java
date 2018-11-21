package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;

public class InvalidArgumentCountException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?>[] arguments;
	
	private final Object[] parsedArguments;
	
	public InvalidArgumentCountException(IArgument<?>[] arguments, Object[] parsedArguments) {
		super("Invalid argument count, requires " + arguments.length + " but got " + parsedArguments.length);
		
		this.arguments = arguments;
		
		this.parsedArguments = parsedArguments;
	}
	
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	public Object[] getParsedArguments() {
		return this.parsedArguments;
	}
}