package com.jockie.bot.core.command.exception;

import java.util.Arrays;

import com.jockie.bot.core.command.impl.CommandEvent;

public class CommandExecutionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private CommandEvent event;
	
	public CommandExecutionException(CommandEvent event, Throwable cause) {
		super("Attempted to execute command (" + event.getCommand().getCommandTrigger() + ") with arguments " + Arrays.deepToString(event.getArguments()) + " but failed", cause);
		
		this.event = event;
		
		this.setStackTrace(new StackTraceElement[0]);
	}
	
	public CommandEvent getEvent() {
		return this.event;
	}
}