package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;

/**
 * This Exception indicates that there was no more content for the argument to parse
 */
public class OutOfContentException extends ArgumentParseException {
	
	private static final long serialVersionUID = 1L;

	public OutOfContentException(IArgument<?> argument) {
		super(argument, "", "There is no more content to parse");
	}
}