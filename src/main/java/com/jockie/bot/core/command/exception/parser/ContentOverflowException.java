package com.jockie.bot.core.command.exception.parser;

public class ContentOverflowException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final String additionalContent;
	
	public ContentOverflowException(String additionalContent) {
		super("More content than could be handled was given");
		
		this.additionalContent = additionalContent;
	}
	
	public String getAdditionalContent() {
		return this.additionalContent;
	}
}