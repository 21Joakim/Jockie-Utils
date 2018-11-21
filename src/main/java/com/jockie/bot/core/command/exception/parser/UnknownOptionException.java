package com.jockie.bot.core.command.exception.parser;

public class UnknownOptionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final String optionString;
	
	public UnknownOptionException(String optionString) {
		super(optionString + " is not a valid option");
		
		this.optionString = optionString;
	}
	
	public String getOptionString() {
		return this.optionString;
	}
}