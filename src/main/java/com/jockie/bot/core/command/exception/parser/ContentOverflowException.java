package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This Exception indicates that there was more content given to the command
 * than it could handle
 */
public class ContentOverflowException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private final String additionalContent;
	
	public ContentOverflowException(ParseContext context, String additionalContent) {
		super(context, "The parser was given more content than it could handle for the command");
		
		this.additionalContent = additionalContent;
	}
	
	/**
	 * @return the additional content which could not be parsed
	 */
	public String getAdditionalContent() {
		return this.additionalContent;
	}
}