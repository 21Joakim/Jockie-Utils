package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.parser.ParseContext;

/*
 * This Exception indicates that there was an unknown option given in the command
 */
public class UnknownOptionException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String optionKey;
	
	public UnknownOptionException(ParseContext context, String optionKey) {
		super(context, "Option: " + optionKey + " is not a valid option");
		
		this.optionKey = optionKey;
	}
	
	/**
	 * @return the name of the given unknown option
	 */
	public String getOptionKey() {
		return this.optionKey;
	}
}