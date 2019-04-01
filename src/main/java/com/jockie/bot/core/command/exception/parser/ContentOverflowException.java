package com.jockie.bot.core.command.exception.parser;

/**
 * This Exception indicates that there was more content given to the command
 * than it could handle
 */
public class ContentOverflowException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String additionalContent;
	
	public ContentOverflowException(String additionalContent) {
		super("More content than could be handled was given");
		
		this.additionalContent = additionalContent;
	}
	
	/**
	 * @return the additional content which could not be parsed
	 */
	public String getAdditionalContent() {
		return this.additionalContent;
	}
}