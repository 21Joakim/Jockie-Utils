package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.option.IOption;

/**
 * This Exception indicates that the value provided for the 
 * option could not be parsed correctly
 */
public class OptionParseException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private IOption<?> option;
	private String value;
	
	public OptionParseException(ParseContext context, IOption<?> option, String value) {
		this(context, option, value, "Option: " + option.getName() + " could not parse the provided value: " + value);
	}
	
	public OptionParseException(ParseContext context, IOption<?> option, String value, String message) {
		super(context, message);
		
		this.option = option;
		this.value = value;
	}
	
	public IOption<?> getOption() {
		return this.option;
	}
	
	public String getValue() {
		return this.value;
	}
}