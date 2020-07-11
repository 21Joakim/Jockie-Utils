package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This Exception indicates that something went wrong in the parsing of a command
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private ParseContext context;
	
	public ParseException(ParseContext context) {
		super();
		
		this.context = context;
	}
	
	public ParseException(ParseContext context, String message) {
		super(message);
		
		this.context = context;
	}
	
	public ParseException(ParseContext context, Throwable cause) {
		super(cause);
		
		this.context = context;
	}
	
	public ParseException(ParseContext context, String message, Throwable cause) {
		super(message, cause);
		
		this.context = context;
	}
	
	public ParseContext getContext() {
		return this.context;
	}
}