package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;

public class MissingRequiredArgumentException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final IArgument<?> argument;
	
	public MissingRequiredArgumentException(IArgument<?> argument) {
		super("Missing required argument " + argument.getName());
		
		this.argument = argument;
	}
	
	public IArgument<?> getArgument() {
		return this.argument;
	}
}