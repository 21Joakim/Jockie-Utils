package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;

/**
 * This Exception indicates that a required argument was not provided, 
 * this is most often in the context of the command being parsed as {@link ArgumentParsingType#NAMED}
 */
public class MissingRequiredArgumentException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?> argument;
	
	public MissingRequiredArgumentException(IArgument<?> argument) {
		super("Missing required argument " + argument.getName());
		
		this.argument = argument;
	}
	
	/**
	 * @return the required argument which was missing
	 */
	public IArgument<?> getArgument() {
		return this.argument;
	}
}