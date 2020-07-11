package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This Exception indicates that there was no more content for the argument to parse
 */
public class OutOfContentException extends ArgumentParseException {
	
	private static final long serialVersionUID = 1L;

	public OutOfContentException(ParseContext context, IArgument<?> argument) {
		super(context, argument, "", "The parser is out of content to parse for the command");
	}
}