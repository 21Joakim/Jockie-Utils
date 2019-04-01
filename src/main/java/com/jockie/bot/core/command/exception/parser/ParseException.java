package com.jockie.bot.core.command.exception.parser;

/**
 * This Exception indicates that something went wrong in the parsing of a command
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
    public ParseException() {
        super();
    }
    
    public ParseException(String message) {
        super(message);
    }
    
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ParseException(Throwable cause) {
        super(cause);
    }
}