package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This Exception indicates that less arguments were parsed 
 * than the command required
 */
public class InvalidArgumentCountException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?>[] arguments;
	
	private final Object[] parsedArguments;
	
	public InvalidArgumentCountException(ParseContext context, IArgument<?>[] arguments, Object[] parsedArguments) {
		super(context, "Invalid argument count, requires: " + arguments.length + " but got: " + parsedArguments.length);
		
		this.arguments = arguments;
		this.parsedArguments = parsedArguments;
	}
	
	/**
	 * @return all the arguments which should have been parse, some of which could have been successfully parsed
	 */
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	/**
	 * @return all the parsed arguments
	 */
	public Object[] getParsedArguments() {
		return this.parsedArguments;
	}
}