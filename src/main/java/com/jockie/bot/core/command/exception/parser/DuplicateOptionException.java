package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.parser.ParseContext;

/*
 * This Exception indicates an option which was already
 * provided was given again
 */
public class DuplicateOptionException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String optionKey;
	
	/* TODO: Is this relevant? */
	private final String optionValue;
	
	public DuplicateOptionException(ParseContext context, String optionKey, String optionValue) {
		super(context, "Option: " + optionKey + " has already been defined");
		
		this.optionKey = optionKey;
		this.optionValue = optionValue;
	}
	
	/**
	 * @return the name of the given duplicate option
	 */
	public String getOptionKey() {
		return this.optionKey;
	}
	
	/**
	 * @return the value of the given duplicate option
	 */
	public String getOptionValue() {
		return this.optionValue;
	}
}