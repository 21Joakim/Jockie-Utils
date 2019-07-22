package com.jockie.bot.core.command.exception.parser;

import com.jockie.bot.core.command.ICommand;

/**
 * This is thrown when a passive command {@link ICommand#isPassive()} goes
 * through parsing because they can't be executed.
 */
public class PassiveCommandException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
}