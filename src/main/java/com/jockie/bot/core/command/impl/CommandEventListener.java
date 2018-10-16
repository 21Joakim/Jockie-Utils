package com.jockie.bot.core.command.impl;

import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

/* Add more events? */
public class CommandEventListener {
	
	/** This will be sent after the command has been called. Depending on how the executed command is made, async or blocking, the command might not have finished executed when this is called */
	public void onCommandExecuted(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent) {}
	
	/** This will be sent after an exception passed by uncaught */
	public void onCommandExecutionException(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent, Throwable e) {}
	
	public void onCommandMissingPermissions(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent, PermissionException e) {}
	
}