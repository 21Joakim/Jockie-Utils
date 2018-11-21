package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;

public class ArgumentParseException extends Throwable {
	
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
	
	public IArgument<?> getArgument() {
		return this.argument;
	}
	
	public String getValue() {
		return this.value;
	}
}