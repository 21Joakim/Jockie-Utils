package com.jockie.bot.core.command.exception.parser;

public class DuplicateOptionException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String optionKey;
	
	/* TODO: Is this relevant? */
	private final String optionValue;
	
	public DuplicateOptionException(String optionKey, String optionValue) {
		super("Option " + optionKey + " has already been defined");
		
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