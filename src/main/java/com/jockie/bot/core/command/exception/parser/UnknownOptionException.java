package com.jockie.bot.core.command.exception.parser;

/*
 * This Exception indicates that there was an unknown option given in the command
 */
public class UnknownOptionException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String optionString;
	
	public UnknownOptionException(String optionString) {
		super(optionString + " is not a valid option");
		
		this.optionString = optionString;
	}
	
	/**
	 * @return the name of the given unknown option
	 */
	public String getOptionString() {
		return this.optionString;
	}
}