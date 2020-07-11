package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This Exception indicates that the value provided for the
 * argument could not be parsed correctly
 */
public class ArgumentParseException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?> argument;
	private final String value;
	
	public ArgumentParseException(ParseContext context, IArgument<?> argument, String value) {
		this(context, argument, value, "Argument: " + argument.getName() + " could not parse the provided value: " + value);
	}
	
	public ArgumentParseException(ParseContext context, IArgument<?> argument, String value, String message) {
		super(context, message);
		
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