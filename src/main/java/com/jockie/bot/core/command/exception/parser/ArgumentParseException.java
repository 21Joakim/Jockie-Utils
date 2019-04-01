package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;

/**
 * This Exception indicates that the value provided to the 
 * argument could not be parsed correctly
 */
public class ArgumentParseException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?> argument;
	private final String value;
	
	public ArgumentParseException(IArgument<?> argument, String value) {
		this(argument, value, value + " is not valid for argument " + argument.getName());
	}
	
	public ArgumentParseException(IArgument<?> argument, String value, String message) {
		super(message);
		
		this.argument = argument;
		this.value = value;
	}
	
	/**
	 * @return the argument which could not parse the value correctly
	 */
	public IArgument<?> getArgument() {
		return this.argument;
	}
	
	/**
	 * @return the value which could not be parse correctly
	 */
	public String getValue() {
		return this.value;
	}
}