package com.jockie.bot.core.command.parser;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.api.entities.Message;

public class ParseContext {
	
	protected final CommandListener commandListener;
	protected final ICommandParser commandParser;
	protected final ICommand command;
	
	protected final Message message;
	
	protected final String trigger;
	protected final String prefix;
	protected final String contentToParse;
	
	protected final long timeStarted;
	
	public ParseContext(CommandListener commandListener, ICommandParser commandParser, 
			ICommand command, Message message, String prefix, String trigger, String contentToParse, 
			long timeStarted) {
		
		this.commandListener = commandListener;
		this.commandParser = commandParser;
		this.command = command;
		
		this.message = message;
		
		this.trigger = trigger;
		this.prefix = prefix;
		this.contentToParse = contentToParse;
		
		this.timeStarted = timeStarted;
	}
	
	/** @return the {@link CommandListener} which received the message to parse */
	@Nonnull
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	/** @return the {@link ICommandParser} which is responsible for parsing the message received */
	@Nonnull
	public ICommandParser getCommandParser() {
		return this.commandParser;
	}
	
	/** @return the {@link ICommand} which the message is being parsed as */
	@Nonnull
	public ICommand getCommand() {
		return this.command;
	}
	
	/** @return the prefix which was used for this message */
	@Nonnull
	public String getPrefix() {
		return this.prefix;
	}
	
	/** @return the trigger used to find the {@link ICommand}, this could be the command itself or an alias */
	@Nonnull
	public String getTrigger() {
		return this.trigger;
	}
	
	/** @return the content which the {@link ICommandParser} has to parse */
	@Nonnull
	public String getContentToParse() {
		return this.contentToParse;
	}
	
	/** @return the time in nanoseconds, {@link System#nanoTime()}, when this message started parsing */
	public long getTimeStarted() {
		return this.timeStarted;
	}
	
	/** @return the time in nanoseconds since the the message started parsing */
	public long getTimeSinceStarted() {
		return System.nanoTime() - this.timeStarted;
	}
	
	/** @return the {@link Message} which has to be parsed */
	@Nonnull
	public Message getMessage() {
		return this.message;
	}
}