package com.jockie.bot.core.command.factory;

import java.util.Map;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.api.entities.Message;

public interface ICommandEventFactory {
	
	/**
	 * @param message the context for this; the message which was sent to trigger this command
	 * @param listener the command listener which the command is registered to
	 * @param command the command which was parsed
	 * @param arguments the parsed arguments
	 * @param rawArguments the raw arguments before they were processed
	 * @param prefix the prefix which was used to trigger this
	 * @param commandTrigger the String which was used to trigger this command, could be an alias
	 * @param options a map of the raw options and their values provided in this command
	 * @param parsingType the type of parsing which was used to parse this command
	 * @param contentOverflow any additional content 
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 */
	public CommandEvent create(Message message, CommandListener listener, ICommand command, 
		Object[] arguments, String[] rawArguments, String prefix, String commandTrigger, 
		Map<String, Object> options, ArgumentParsingType parsingType, String contentOverflow, long timeStarted);
	
}