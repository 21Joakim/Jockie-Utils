package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.parser.ParseContext;

/**
 * This is thrown when a passive command {@link ICommand#isPassive()} goes
 * through parsing because they can't be executed.
 */
public class PassiveCommandException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	public PassiveCommandException(ParseContext context) {
		super(context, "Passive commands can not be parsed");
	}
}