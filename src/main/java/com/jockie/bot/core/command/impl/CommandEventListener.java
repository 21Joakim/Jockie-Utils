package com.jockie.bot.core.command.impl;

import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

/* Add more events? */
public class CommandEventListener {
	
	/** This will be sent after the command has been called. Depending on how the executed command is made, async or blocking, the command might not have finished executed when this is called */
	public void onCommandExecuted(ICommand command, CommandEvent event) {}
	
	/** This will be sent after an exception passed by uncaught */
	public void onCommandExecutionException(ICommand command, CommandEvent event, Throwable e) {}
	
	/** This is only called if a PermissionException was thrown and not if it was handled correctly through getBotPermissions and getAuthorPermissions */
	public void onCommandMissingPermissions(ICommand command, CommandEvent event, PermissionException e) {}
	
	/** This will be sent if a message starts with a registered prefix, this is triggered no matter if a command was triggered or not */
	public void onPrefixedMessage(MessageReceivedEvent event, String prefix) {}
	
	/** This will be sent if a message starts with a registered prefix and passed by without any matches */
	public void onUnknownCommand(MessageReceivedEvent event, String prefix) {}
	
}