package com.jockie.bot.core.command.parser;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.exception.parser.ParseException;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.core.entities.Message;

public interface ICommandParser {
	
	/**
	 * Parse the provided command
	 * 
	 * @param listener the command listener which the command is registered to
	 * @param command the command which should be parse
	 * @param message the context for the parsing of this command
	 * @param prefix the prefix which was used to trigger this
	 * @param trigger the trigger which matched the start of the message, could be an alias
	 * @param contentToParse the content to parse
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 * 
	 * @return the processed command as a CommandEvent, may be null if the command could not be parsed correctly
	 * 
	 * @throws ParseException if the parsing of the command fails in some way
	 */
	public CommandEvent parse(CommandListener listener, ICommand command, Message message, String prefix, String trigger, String contentToParse, long timeStarted) throws ParseException;
	
}