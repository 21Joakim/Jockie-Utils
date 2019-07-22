package com.jockie.bot.core.command.parser;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.core.entities.Message;

public class ParseContext {
	
	private CommandListener commandListener;
	private ICommandParser commandParser;
	private ICommand command;
	
	private Message message;
	
	private String trigger;
	private String prefix;
	private String contentToParse;
	
	private long timeStarted;
	
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
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	/** @return the {@link ICommandParser} which is responsible for parsing the message received */
	public ICommandParser getCommandParser() {
		return this.commandParser;
	}
	
	/** @return the {@link ICommand} which the message is being parsed as */
	public ICommand getCommand() {
		return this.command;
	}
	
	/** @return the prefix which was used for this message */
	public String getPrefix() {
		return this.prefix;
	}
	
	/** @return the trigger used to find the {@link ICommand}, this could be the command itself or an alias*/
	public String getTrigger() {
		return this.trigger;
	}
	
	/** @return the content which the {@link ICommandParser} has to parse */
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
	public Message getMessage() {
		return this.message;
	}
}